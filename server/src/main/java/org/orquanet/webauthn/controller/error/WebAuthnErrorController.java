/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.orquanet.webauthn.controller.error;

import org.orquanet.webauthn.webauthn.attestation.exception.RegistrationException;
import org.orquanet.webauthn.webauthn.exception.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.*;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Date;
import java.util.Optional;

@ControllerAdvice
@RestControllerAdvice
public class WebAuthnErrorController {

    private static Logger LOGGER = LoggerFactory.getLogger(WebAuthnErrorController.class);

    @ExceptionHandler(NumberFormatException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorDetails handleMissingParameters(NumberFormatException e){

        if(LOGGER.isDebugEnabled()){
            LOGGER.debug(e.getMessage());
        }
        return sendError(HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorDetails handleConstraintViolationException(ConstraintViolationException e){

        Optional<ConstraintViolation<?>> opt = e.getConstraintViolations().stream().findFirst();
        String message = opt.isPresent()?opt.get().getMessage(): "Unknown Error";
        ErrorDetails errorDetails = ErrorDetails.builder()
                .timeStamp(new Date())
                .message("Bad Request")
                .details(message)
                .httpStatus(HttpStatus.METHOD_NOT_ALLOWED.value())
                .build();

        e.getConstraintViolations().forEach(c -> System.out.println(c.getMessage()));
        if(LOGGER.isDebugEnabled()){
            LOGGER.debug(e.getMessage());
        }
        return errorDetails;
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ErrorDetails userNotFoundException(UserNotFoundException e){

       // String message = opt.isPresent()?opt.get().getMessage(): "Unknown Error";
        ErrorDetails errorDetails = ErrorDetails.builder()
                .timeStamp(new Date())
                .message("User Not Found")
                .details("User Not Found")
                .httpStatus(HttpStatus.NOT_FOUND.value())
                .build();

        if(LOGGER.isDebugEnabled()){
            LOGGER.debug(e.getMessage());
        }
        return errorDetails;
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ResponseBody
    public ErrorDetails handleMethodNotSupported(HttpRequestMethodNotSupportedException e){

        if(LOGGER.isDebugEnabled()){
            LOGGER.debug(e.getMessage());
        }
        return sendError(HttpStatus.METHOD_NOT_ALLOWED);
    }


    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorDetails handleIllegalArgumentException(IllegalArgumentException e){

        if(LOGGER.isDebugEnabled()){
            LOGGER.debug(e.getMessage());
        }
        return sendError(HttpStatus.BAD_REQUEST);

    }

    @ExceptionHandler(RegistrationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorDetails handleRegistrationException(RegistrationException  e){
        if(LOGGER.isDebugEnabled()){
            e.printStackTrace();
            LOGGER.debug(e.getMessage());
        }
        e.printStackTrace();
        return sendError(HttpStatus.BAD_REQUEST);
    }

    private ErrorDetails sendError(HttpStatus httpStatus){
        return ErrorDetails.builder()
                .timeStamp(new Date())
                .message("Bad Request")
                .details("Bad request")
                .httpStatus(httpStatus.value())
                .build();
    }

}
