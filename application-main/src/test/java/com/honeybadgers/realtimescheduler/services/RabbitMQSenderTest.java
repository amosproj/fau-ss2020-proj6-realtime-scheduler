package com.honeybadgers.realtimescheduler.services;

import com.honeybadgers.realtimescheduler.services.impl.RabbitMQSender;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
public class RabbitMQSenderTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private RabbitMQSender sender;

    @Before
    public void beforeEach() {
        //MockitoAnnotations.initMocks(this);
        sender = new RabbitMQSender(rabbitTemplate);
    }


    @Test
    public void testSendTaskToDispatcher() {
        RabbitMQSender spy = Mockito.spy(sender);
        spy.sendTaskToDispatcher("task");
        Mockito.verify(rabbitTemplate).convertAndSend(Mockito.any(), Mockito.any(), (Object) Mockito.any());
    }

    @Test
    public void testSendFeedbackToScheduler() {
        RabbitMQSender spy = Mockito.spy(sender);
        spy.sendFeedbackToScheduler("feedback");
        Mockito.verify(rabbitTemplate).convertAndSend(Mockito.any(), Mockito.any(), (Object) Mockito.any());
    }

    @Test
    public void sendTaskToTaskQueue() {
        RabbitMQSender spy =  Mockito.spy(sender);
        spy.sendTaskToTaskQueue("tasks");
        Mockito.verify(rabbitTemplate).convertAndSend(Mockito.any(), Mockito.any(), (Object) Mockito.any());
    }
}