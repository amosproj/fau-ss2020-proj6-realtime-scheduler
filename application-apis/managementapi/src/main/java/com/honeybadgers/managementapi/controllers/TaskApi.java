/**
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech) (4.3.1).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
package com.honeybadgers.managementapi.controllers;

import com.honeybadgers.managementapi.models.DateTimeBody;
import com.honeybadgers.managementapi.models.ErrorModel;
import com.honeybadgers.managementapi.models.ResponseModel;
import java.util.UUID;
import io.swagger.annotations.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2020-06-06T19:32:35.410+02:00[Europe/Berlin]")

@Validated
@Api(value = "task", description = "the task API")
public interface TaskApi {

    default Optional<NativeWebRequest> getRequest() {
        return Optional.empty();
    }

    /**
     * PUT /task/{task_Id}/start
     * Start scheduling task with given task_Id
     *
     * @param taskId  (required)
     * @return Task successfully queued for scheduling (status code 200)
     *         or Error - task_Id not found (status code 404)
     *         or Unauthorized (status code 401)
     */
    @ApiOperation(value = "", nickname = "taskTaskIdStartPut", notes = "Start scheduling task with given task_Id", response = ResponseModel.class, authorizations = {
        @Authorization(value = "basicAuth")
    }, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Task successfully queued for scheduling", response = ResponseModel.class),
        @ApiResponse(code = 404, message = "Error - task_Id not found", response = ErrorModel.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorModel.class) })
    @RequestMapping(value = "/task/{task_Id}/start",
        produces = { "application/json" }, 
        method = RequestMethod.PUT)
    default ResponseEntity<ResponseModel> taskTaskIdStartPut(@ApiParam(value = "",required=true) @PathVariable("task_Id") UUID taskId) {
        getRequest().ifPresent(request -> {
            for (MediaType mediaType: MediaType.parseMediaTypes(request.getHeader("Accept"))) {
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString = "{ \"code\" : \"code\", \"message\" : \"message\" }";
                    ApiUtil.setExampleResponse(request, "application/json", exampleString);
                    break;
                }
            }
        });
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    }


    /**
     * PUT /task/{task_Id}/stop
     * Stop scheduling task with given task_Id
     *
     * @param taskId  (required)
     * @param dateTimeBody DateTime body which indicates, when to resume scheduling (optional)
     * @return Task successfully stopped scheduling (status code 200)
     *         or Error - task_Id not found (status code 404)
     *         or Unauthorized (status code 401)
     */
    @ApiOperation(value = "", nickname = "taskTaskIdStopPut", notes = "Stop scheduling task with given task_Id", response = ResponseModel.class, authorizations = {
        @Authorization(value = "basicAuth")
    }, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Task successfully stopped scheduling", response = ResponseModel.class),
        @ApiResponse(code = 404, message = "Error - task_Id not found", response = ErrorModel.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorModel.class) })
    @RequestMapping(value = "/task/{task_Id}/stop",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.PUT)
    default ResponseEntity<ResponseModel> taskTaskIdStopPut(@ApiParam(value = "",required=true) @PathVariable("task_Id") UUID taskId,@ApiParam(value = "DateTime body which indicates, when to resume scheduling"  )  @Valid @RequestBody(required = false) DateTimeBody dateTimeBody) {
        getRequest().ifPresent(request -> {
            for (MediaType mediaType: MediaType.parseMediaTypes(request.getHeader("Accept"))) {
                if (mediaType.isCompatibleWith(MediaType.valueOf("application/json"))) {
                    String exampleString = "{ \"code\" : \"code\", \"message\" : \"message\" }";
                    ApiUtil.setExampleResponse(request, "application/json", exampleString);
                    break;
                }
            }
        });
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);

    }

}
