/**
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech) (4.3.1).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
package com.honeybadgers.managementapi.controllers;

import com.honeybadgers.managementapi.models.DateTimeBody;
import com.honeybadgers.managementapi.models.ErrorModel;
import com.honeybadgers.managementapi.models.ResponseModel;
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
@Api(value = "scheduler", description = "the scheduler API")
public interface SchedulerApi {

    default Optional<NativeWebRequest> getRequest() {
        return Optional.empty();
    }

    /**
     * PUT /scheduler/start
     * Start whole scheduler (all tasks)
     *
     * @return Scheduler successfully started (status code 200)
     *         or Unauthorized (status code 401)
     */
    @ApiOperation(value = "", nickname = "schedulerStartPut", notes = "Start whole scheduler (all tasks)", response = ResponseModel.class, authorizations = {
        @Authorization(value = "basicAuth")
    }, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Scheduler successfully started", response = ResponseModel.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorModel.class) })
    @RequestMapping(value = "/scheduler/start",
        produces = { "application/json" }, 
        method = RequestMethod.PUT)
    default ResponseEntity<ResponseModel> schedulerStartPut() {
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
     * PUT /scheduler/stop
     * Stop scheduler
     *
     * @param dateTimeBody DateTime body which indicates, when to resume scheduling (optional)
     * @return Scheduler successfully stopped (status code 200)
     *         or Unauthorized (status code 401)
     */
    @ApiOperation(value = "", nickname = "schedulerStopPut", notes = "Stop scheduler", response = ResponseModel.class, authorizations = {
        @Authorization(value = "basicAuth")
    }, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Scheduler successfully stopped", response = ResponseModel.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorModel.class) })
    @RequestMapping(value = "/scheduler/stop",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.PUT)
    default ResponseEntity<ResponseModel> schedulerStopPut(@ApiParam(value = "DateTime body which indicates, when to resume scheduling"  )  @Valid @RequestBody(required = false) DateTimeBody dateTimeBody) {
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
