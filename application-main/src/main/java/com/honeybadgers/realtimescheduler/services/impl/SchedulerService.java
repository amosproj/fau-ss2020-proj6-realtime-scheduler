package com.honeybadgers.realtimescheduler.services.impl;

import com.honeybadgers.communication.ICommunication;
import com.honeybadgers.models.model.*;
import com.honeybadgers.postgre.repository.GroupRepository;
import com.honeybadgers.postgre.repository.PausedRepository;
import com.honeybadgers.postgre.repository.TaskRepository;
import com.honeybadgers.realtimescheduler.services.IGroupService;
import com.honeybadgers.realtimescheduler.services.ISchedulerService;
import com.honeybadgers.realtimescheduler.services.ITaskService;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.TransactionException;
import org.hibernate.exception.LockAcquisitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.honeybadgers.models.model.Constants.*;
import static com.honeybadgers.models.model.ModeEnum.Sequential;

@Service
public class SchedulerService implements ISchedulerService {

    static final Logger logger = LogManager.getLogger(SchedulerService.class);
    public static volatile boolean stopSchedulerDueToLockAcquisitionException = false;
    @Value("${scheduler.trigger}")
    String scheduler_trigger;

    @Autowired
    SchedulerService _self;

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    PausedRepository pausedRepository;

    @Autowired
    ITaskService taskService;

    @Autowired
    ICommunication sender;

    @Autowired
    IGroupService groupService;

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    RestTemplate restTemplate;

    public int getLimitFromGroup(List<String> groupsOfTask, String grpId) {
        int minLimit = Integer.MAX_VALUE;

        for (String groupId : groupsOfTask) {
            Group currentGroup = groupService.getGroupById(groupId);
            if (currentGroup == null || currentGroup.getParallelismDegree() == null)
                continue;

            minLimit = Math.min(minLimit, currentGroup.getParallelismDegree());
        }
        logger.debug("limit for groupid: " + grpId + "is now at: " + minLimit);
        return minLimit;
    }

    @Override
    public boolean isTaskPaused(String taskId) {
        if (taskId == null)
            throw new IllegalArgumentException("Method isTaskLocked: given taskId is null!");

        String pausedId = PAUSED_TASK_PREFIX + taskId;
        Paused paused = pausedRepository.findById(pausedId).orElse(null);
        return paused != null;
    }

    @Override
    public boolean isGroupPaused(String groupId) {
        if (groupId == null)
            throw new IllegalArgumentException("Method isGroupLocked: given groupId is null!");

        String pausedId = PAUSED_GROUP_PREFIX + groupId;
        Paused paused = pausedRepository.findById(pausedId).orElse(null);
        return paused != null;
    }

    @Override
    public boolean isSchedulerPaused() {
        Paused paused = pausedRepository.findById(PAUSED_SCHEDULER_ALIAS).orElse(null);
        return paused != null;
    }

    @Override
    public void scheduleTaskWrapper(String trigger) {
        stopSchedulerDueToLockAcquisitionException = false;
        Thread lockrefresherThread = null;
        try {

            // try to acquire scheduling lock from Lock Application
            LockResponse lockResponse = checkIfAllowedtoSchedule();

            // create and start lock-refresh-thread
            lockrefresherThread = new LockRefresherThread(lockResponse, restTemplate);
            lockrefresherThread.start();

            // get all tasks
            List<Task> waitingTasks;
            if (trigger.equals(scheduler_trigger))
                waitingTasks = taskRepository.findAllScheduledTasksSorted();
            else
                waitingTasks = taskRepository.findAllWaitingTasks();

            // schedule tasks
            logger.info("Step 2: scheduling " + waitingTasks.size() + " tasks");
            for (Task task : waitingTasks ) {
                try {
                    if (stopSchedulerDueToLockAcquisitionException)
                        return;
                    _self.scheduleTask(task);
                } catch (CannotAcquireLockException | LockAcquisitionException exception) {
                    logger.warn("Task " + task.getId() + " Scheduling LockAcquisitionException!");
                } catch (JpaSystemException | TransactionException exception) {
                    logger.warn("Task " + task.getId() + " Scheduling TransactionException!");
                }
            }

            // dispatch tasks
            List<Task> tasks = taskRepository.findAllScheduledTasksSorted();
            if (!isSchedulerPaused()) {
                logger.info("Step 3: dispatching " + tasks.size() + " tasks");
                for (Task task : tasks) {
                    try {
                        if (stopSchedulerDueToLockAcquisitionException)
                            return;

                        if(_self.checkTaskForDispatchingAndUpdate(task)) {
                            // TODO document: if scheduler crashes here -> task could be dispatched twice
                            // dispatch here because this only gets executed if transaction succeeds
                            sender.sendTaskToDispatcher(task.getId());
                            logger.info("Task " + task.getId() + " was sent to dispatcher queue and status was set to 'Dispatched'");
                        }

                    } catch (CannotAcquireLockException | LockAcquisitionException exception) {
                        logger.warn("Task " + task.getId() + " Dispatching LockAcquisitionException!");
                    } catch (JpaSystemException | TransactionException exception) {
                        logger.warn("Task " + task.getId() + " Dispatching TransactionException!");
                    }
                }
            } else
                logger.info("Scheduler is locked!");

            // stop lock-refresh-thread
            lockrefresherThread.interrupt();
        } catch (Exception e) {
            logger.error("unexpected exception in scheduling " + e.getMessage());
        } finally {
            if (lockrefresherThread != null)
                lockrefresherThread.interrupt();
        }
    }

    public LockResponse checkIfAllowedtoSchedule() throws LockException {
        String url = "https://lockservice-amos.cfapps.io/";
        final String scheduler = "SCHEDULER";

        // create headers
        HttpEntity<Object> entity = getObjectHttpEntity();

        // send POST request
        ResponseEntity<LockResponse> response = restTemplate.postForEntity(url + scheduler, entity, LockResponse.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            logger.info("lock for scheduler already acquired");
            throw new LockException("Failed to acquire lock for Lock Application");
        }


        logger.info("acquired lock for: " + response.getBody().getValue());
        return response.getBody();
    }

    private static HttpEntity<Object> getObjectHttpEntity() {
        // create headers
        HttpHeaders headers = new HttpHeaders();
        // set `accept` header
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        // build the request
        return new HttpEntity<>(null, headers);
    }

    /**
     * Schedule given task and update in DB (Running as transaction)
     * @param task task to be scheduled
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void scheduleTask(Task task) {
        task.setTotalPriority(taskService.calculatePriority(task));
        logger.info("Task " + task.getId() + " calculated total priority: " + task.getTotalPriority());
        taskService.updateTaskStatus(task, TaskStatusEnum.Scheduled);
        taskRepository.save(task);
    }

    /**
     * Tries to send given task to the dispatcher if the conditions for sending are met.
     * @param currentTask task to be send to the dispatcher
     * @return return true if status was updated and dispatch is wanted
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public boolean checkTaskForDispatchingAndUpdate(Task currentTask) {
        if (isTaskPaused(currentTask.getId())) {
            logger.info("Task " + currentTask.getId() + " is currently paused!");
            return false;
        }

        List<String> groupsOfTask = taskService.getRecursiveGroupsOfTask(currentTask.getId());

        if (checkGroupOrAncesterGroupIsOnPause(groupsOfTask, currentTask.getId()))
            return false;

        if (!checkIfTaskIsInActiveTime(currentTask) || !checkIfTaskIsInWorkingDays(currentTask) || sequentialHasToWait(currentTask))
            return false;

        // Get Parlellism Current Task Amount from group of task (this also includes tasks of )
        Group parentGroup = currentTask.getGroup();

        int limit = getLimitFromGroup(groupsOfTask, parentGroup.getId());
        // TODO bug User Story 84 (documents, as mentioned in US, in documents channel of discord)
        if (parentGroup.getCurrentParallelismDegree() >= limit) {
            logger.info("Task " + currentTask.getId() + " was not sned due to parallelism limit for Group " + parentGroup.getId() + " is now at: " + parentGroup.getCurrentParallelismDegree());
            return false;
        }
        // TODO unnötig -> groupRepository.incrementCurrentParallelismDegree(parentGroup.getId()) reicht, solange currentTask.group danach nicht benutzt wird
        currentTask.setGroup(groupRepository.incrementCurrentParallelismDegree(parentGroup.getId()));

        // TODO custom query
        taskService.updateTaskStatus(currentTask, TaskStatusEnum.Dispatched);
        taskRepository.save(currentTask);

        return true;
    }

    /**
     * Checks groups with id in given list on paused
     * @param groupsOfTask List of groupIds to be checked on paused
     * @param taskid taskId of logging
     * @return true if one group is paused
     */
    public boolean checkGroupOrAncesterGroupIsOnPause(List<String> groupsOfTask, String taskid) {
        for (String groupId : groupsOfTask) {
            // check if group is paused (IllegalArgExc should not happen, because groupsOfTask was check on containing null values)
            if (isGroupPaused(groupId)) {
                logger.info("Task " + taskid + " is paused by Group " + groupId);
                return true;
            }
        }
        return false;
    }

    /**
     * Check if given task has to wait due to its sequence number
     * @param task task to be checked
     * @return true if task has to wait
     */
    public boolean sequentialHasToWait(Task task) {
        if (task.getModeEnum() == Sequential) {
            logger.debug("task getIndexNumber " + task.getIndexNumber());
            Group parentgroup = task.getGroup();
            logger.debug("parentgroup lastindexnumber " + parentgroup.getLastIndexNumber());

            if (task.getIndexNumber() == parentgroup.getLastIndexNumber() + 1)
                return false;
            else {
                logger.info("Task " + task.getId() + " is not sent due to Sequential");
                return true;
            }
        }
        return false;
    }

    public boolean checkIfTaskIsInWorkingDays(Task task) {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        int dayofweek = calendar.get(Calendar.DAY_OF_WEEK);
        int[] workingdays = getActualWorkingDaysForTask(task);
        ConvertUtils convertUtils = new ConvertUtils();
        List<Boolean> workingdaybools = convertUtils.intArrayToBoolList(workingdays);

        if (workingdaybools.get(convertUtils.fitDayOfWeekToWorkingDayBooleans(dayofweek)))
            return true;

        logger.info("Task " + task.getId() + " is not sent due to workingDays");
        return false;
    }

    public int[] getActualWorkingDaysForTask(Task task) {
        int[] workingDays = task.getWorkingDays();

        if (workingDays != null)
            return workingDays;

        if (task.getGroup() == null)
            throw new RuntimeException("parentgroup from " + task.getId() + " is null");

        Group parentGroup = groupService.getGroupById(task.getGroup().getId());

        while (parentGroup != null) {
            if (parentGroup.getWorkingDays() != null)
                return parentGroup.getWorkingDays();

            if (parentGroup.getParentGroup() == null)
                break;

            parentGroup = groupService.getGroupById(parentGroup.getParentGroup().getId());
        }

        return new int[]{1, 1, 1, 1, 1, 1, 1};
    }

    public boolean checkIfTaskIsInActiveTime(Task task) {
        Date current = new Date();
        Date from = new Date();
        Date to = new Date();
        List<ActiveTimes> activeTimes = new ArrayList<ActiveTimes>();
        SimpleDateFormat parser = new SimpleDateFormat("HH:mm:ss");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        try {
            current = parser.parse(dateTimeFormatter.format(LocalDateTime.now()));

            activeTimes = getActiveTimesForTask(task);

            if (activeTimes == null || activeTimes.isEmpty())
                return true;

            for (ActiveTimes activeTime : activeTimes) {
                from = parser.parse(activeTime.getFrom().toString());
                to = parser.parse(activeTime.getTo().toString());
                if (current.before(to) && current.after(from)) {
                    return true;
                }
            }
        } catch (ParseException pe) {
            logger.error("active times exception: " + pe.getMessage());
        }
        logger.info("Task " + task.getId() + " is not sent due to ActiveTimes");
        return false;
    }

    public List<ActiveTimes> getActiveTimesForTask(Task task) {
        List<ActiveTimes> activeTimes = task.getActiveTimeFrames();
        if (activeTimes != null)
            return activeTimes;

        if (task.getGroup() == null)
            throw new RuntimeException("parentgroup from " + task.getId() + " is null");

        Group parentGroup = groupService.getGroupById(task.getGroup().getId());

        while (parentGroup != null) {
            if (parentGroup.getActiveTimeFrames() != null)
                return parentGroup.getActiveTimeFrames();

            if (parentGroup.getParentGroup() == null)
                break;

            parentGroup = groupService.getGroupById(parentGroup.getParentGroup().getId());
        }

        return new ArrayList<>();
    }


    public static class LockRefresherThread extends Thread {
        LockResponse lockresponse;
        RestTemplate restTemplate;
        final String name;
        final String value;

        public LockRefresherThread(LockResponse resp, RestTemplate template) {
            lockresponse = resp;
            restTemplate = template;
            name = lockresponse.getName();
            value = lockresponse.getValue();
        }

        @SneakyThrows
        public void run() {
            try {
                HttpEntity<Object> entity = getObjectHttpEntity();
                String url = "https://lockservice-amos.cfapps.io/" + name + "/" + value;
                while (true) {
                    // send Put request
                    ResponseEntity<LockResponse> response = restTemplate.exchange(url, HttpMethod.PUT, entity, LockResponse.class);
                    logger.info(" Statuscode put response" + response.getStatusCode());
                    if (response.getStatusCode() != HttpStatus.OK) {
                        logger.error("could not refresh lock");
                        throw new LockException("could not refresh lock");
                    }

                    Thread.sleep(15000);

                }
            } catch (Exception e) {
                if (e.getClass().equals(InterruptedException.class)) {
                    releaseLock();
                    logger.info(lockresponse.getValue() + " releasing lock cause thread was interrupted by scheduler");
                } else {
                    logger.error("error by refreshing lock for lock " + name + "with value " + value + " " + e.getMessage());
                    SchedulerService.stopSchedulerDueToLockAcquisitionException = true;
                }
            }
        }

        public void releaseLock() {
            HttpEntity<Object> entity = getObjectHttpEntity();
            String url = "https://lockservice-amos.cfapps.io/" + name + "/" + value;
            ResponseEntity<LockResponse> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, LockResponse.class);
        }
    }
}
