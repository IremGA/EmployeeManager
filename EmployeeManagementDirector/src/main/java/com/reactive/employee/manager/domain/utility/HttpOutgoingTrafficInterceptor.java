package com.reactive.employee.manager.domain.utility;

import io.vavr.CheckedFunction0;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

public class HttpOutgoingTrafficInterceptor implements ClientHttpRequestInterceptor {
    
    public static final Logger Employee_Director_Logger = LoggerFactory.getLogger(HttpOutgoingTrafficInterceptor.class);
    
    private int count;
    private ResilienceManager resilienceManager = null;
    public HttpOutgoingTrafficInterceptor(ResilienceManager manager){
        this.resilienceManager = manager;
    }

    private boolean isDebugEnabled(){
        return Employee_Director_Logger.isDebugEnabled();
    }

    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        count = 0;
        CheckedFunction0<ClientHttpResponse> decoratedSupplier = resilienceManager.getCaller(() -> {
            Employee_Director_Logger.debug( "checked sending new one " + incrementAndGetCount());
            return interceptInner(request, body, execution);
        }, request.getURI().getHost());
        try {
            return decoratedSupplier.apply();
        }
        catch (Throwable throwable) {
            printError( throwable);
            if(throwable instanceof IOException){
                throw (IOException) throwable;
            }else{
                Employee_Director_Logger.debug( throwable.getClass() + " " + throwable.getMessage());
                throw new IOException(throwable.getMessage(), throwable);
            }
        }
    }

    int incrementAndGetCount(){
        return ++count;
    }

    public ClientHttpResponse interceptInner(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        Employee_Director_Logger.debug("requesting ...");
        logRequest(request, body);
        ClientHttpResponse response = null;
        try{
            response = execution.execute(request, body);
        }catch (IOException e){
            printError(e);
            throw e;
        }

        logResponse(response);
        Employee_Director_Logger.debug( "end of requesting ...");
        return response;
    }

    private void logRequest(HttpRequest request, byte[] body) throws IOException {
        if (isDebugEnabled()) {
            Employee_Director_Logger.debug("===========================request begin================================================");
            if(!request.getURI().toString().contains("CommunicationId"))
            {
                Employee_Director_Logger.debug("URI         : {}", request.getURI());
            }
            Employee_Director_Logger.debug("Method      : {}", request.getMethod());
            Employee_Director_Logger.debug("Headers     : {}", request.getHeaders());
            Employee_Director_Logger.debug("Request body: {}", new String(body, "UTF-8"));
            Employee_Director_Logger.debug("==========================request end================================================");
        }
    }

    private void logResponse(ClientHttpResponse response) throws IOException {
        if (isDebugEnabled()) {
            Employee_Director_Logger.debug( "============================response begin==========================================");
            Employee_Director_Logger.debug( "Status code  : {}", response.getStatusCode());
            Employee_Director_Logger.debug( "Status text  : {}", response.getStatusText());
            Employee_Director_Logger.debug( "Headers      : {}", response.getHeaders());
            Employee_Director_Logger.debug( "=======================response end=================================================");
        }
    }

    public void printError(Throwable t){
        if(isDebugEnabled()){
            Employee_Director_Logger.debug(t.getClass() + " - " + t.getMessage());
            Throwable cause = t.getCause();
            if( cause != null){
                Employee_Director_Logger.debug( cause.getClass() + " - " + cause.getMessage());
            }
        }
    }
}
