package com.reactive.employee.manager.domain.handler.impl;

import com.reactive.employee.manager.domain.exception.ApplicationErrorHandler;
import com.reactive.employee.manager.domain.exception.EmployeeManagerException;
import com.reactive.employee.manager.domain.handler.EmployeeManagerHandler;
import com.reactive.employee.manager.domain.model.Employee;
import com.reactive.employee.manager.domain.repository.EmployeeManagerRepository;
import com.reactive.employee.manager.domain.service.EmployeeManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@Component
public class EmployeeManagerHandlerImpl implements EmployeeManagerHandler {
    private EmployeeManagerRepository repository;

    public static final Logger Employee_Manager_Logger = LoggerFactory.getLogger(EmployeeManagerHandlerImpl.class);
    
    @Autowired
    private EmployeeManagerService employeeManagerService;
    
    @Autowired
    private ApplicationErrorHandler applicationErrorHandler;

    public EmployeeManagerHandlerImpl(EmployeeManagerRepository repository) {

        this.repository = repository;
    }

    @Override
    public Mono<ServerResponse> getAllEmployees(ServerRequest request) {

        Mono<Employee> employeeMono = request.bodyToMono(Employee.class).switchIfEmpty(Mono.just(new Employee()));

        Mono<ServerResponse> serverResponseMono = employeeMono.flatMap( employee -> {
            Employee_Manager_Logger.info("Search criteria has been applied as : {} ", employee.toString());
            Flux<Employee> employeeMono1 = null;
            try{
                employeeMono1 = employeeManagerService.getAllEmployees(employee);
                Employee_Manager_Logger.info("Employees are get : {} ", employeeMono1.toString());
                return ServerResponse.status(HttpStatus.OK)
                        .contentType(APPLICATION_JSON)
                        .body(employeeMono1, Employee.class);
            }catch (EmployeeManagerException e){
                return applicationErrorHandler.handleDownStreamError(e);
            }

        });

        return serverResponseMono;

    }

    @Override
    public Mono<ServerResponse> getEmployee(ServerRequest request) {
        String id = request.pathVariable("username");
        Employee_Manager_Logger.debug("Get Employee for PID : {} ", id);
        try{
            Mono<ServerResponse> notFound = ServerResponse.notFound().build();

            Mono<Employee> employeeMono = employeeManagerService.getEmployee(id);

            return employeeMono
                    .flatMap(employee ->
                            ServerResponse.ok()
                                    .contentType(APPLICATION_JSON)
                                    .body(fromValue(employee)))
                    .switchIfEmpty(notFound);
        }catch (EmployeeManagerException e){
            
            Employee_Manager_Logger.debug(" Exception handled for Get Employee for PID : {} ", id);
            return applicationErrorHandler.handleDownStreamError(e);
        }
    }

    public Mono<ServerResponse> saveEmployee(ServerRequest request) {
        Employee_Manager_Logger.debug("Inside save Employee ");
        Mono<Employee> employeeMono = request.bodyToMono(Employee.class);
        Mono<ServerResponse> serverResponseMono = employeeMono.flatMap( employee -> {
            Mono<Employee> employeeMono1 = null;
            try{
                employeeMono1 = employeeManagerService.createEmployee(employee);
                return ServerResponse.status(HttpStatus.CREATED)
                        .contentType(APPLICATION_JSON)
                        .body(employeeMono1, Employee.class);
            }catch (EmployeeManagerException e){
                return applicationErrorHandler.handleDownStreamError(e);
            }

        });

         return serverResponseMono;
    }

    public Mono<ServerResponse> updateEmployee(ServerRequest request) {
        Employee_Manager_Logger.debug("Inside update Employee ");
        String id = request.pathVariable("username");
        Mono<Employee> existingEmployeeMono = this.repository.findById(id);
        Mono<Employee> employeeMono = request.bodyToMono(Employee.class);

        Mono<ServerResponse> notFound = ServerResponse.notFound().build();

        return employeeMono.zipWith(existingEmployeeMono,
                (employee, existingEmployee) ->{
                       employee.setUsername(existingEmployee.getUsername());
                       return employee;
                }
                        
        ).flatMap(employee ->
                ServerResponse.ok()
                        .contentType(APPLICATION_JSON)
                        .body(repository.save(employee), Employee.class)
        ).switchIfEmpty(notFound);
    }

    public Mono<ServerResponse> deleteEmployee(ServerRequest request) {
        Employee_Manager_Logger.debug("Inside delete Employee ");
        String id = request.pathVariable("username");
        Mono<ServerResponse> status_ok = ServerResponse.noContent().build();
        try{
            employeeManagerService.deleteEmployee(id);
            repository.deleteById(id).subscribe();
            return status_ok;
        }catch (EmployeeManagerException e){
            return applicationErrorHandler.handleDownStreamError(e);
        }
        
        
    }

    public Mono<ServerResponse> deleteAllEmployees(ServerRequest request) {
        return ServerResponse.ok()
                .build(repository.deleteAll());
    }

    public Mono<ServerResponse> patchEmployee(ServerRequest request) {
        Employee_Manager_Logger.debug("Inside patch Employee ");
        String id = request.pathVariable("username");
        Mono<Employee> employeeMono = request.bodyToMono(Employee.class);
        Mono<ServerResponse> notFound = ServerResponse.notFound().build();

        Mono<ServerResponse> serverResponseMono = employeeMono.flatMap( employee1 -> {
            try{
                return employeeManagerService.patchEmployee(id,employee1).flatMap(employee ->
                        ServerResponse.ok()
                                .contentType(APPLICATION_JSON)
                                .body(repository.save(employee), Employee.class)
                ).switchIfEmpty(notFound);

            }catch (EmployeeManagerException e){
                return applicationErrorHandler.handleDownStreamError(e);
            }

        });

        return serverResponseMono;
    }
}
