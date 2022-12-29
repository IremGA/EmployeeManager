package com.reactive.employee.manager.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Service
public class ApplicationErrorHandler {
    
    public Mono<ServerResponse> handleDownStreamError(EmployeeDirectorException e){
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setCause("Error happened while calling downstream component");
        errorResponse.setUri(e.getUrl());
        errorResponse.setErrorCode(HttpStatus.BAD_REQUEST.toString());
        
        
        return  ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(APPLICATION_JSON)
                .body(Mono.just(errorResponse), ErrorResponse.class);
    }

}
