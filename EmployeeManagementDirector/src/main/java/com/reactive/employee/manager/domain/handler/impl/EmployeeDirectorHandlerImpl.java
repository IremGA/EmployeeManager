package com.reactive.employee.manager.domain.handler.impl;

import com.reactive.employee.manager.domain.exception.ApplicationErrorHandler;
import com.reactive.employee.manager.domain.exception.EmployeeDirectorException;
import com.reactive.employee.manager.domain.handler.EmployeeDirectorHandler;
import com.reactive.employee.manager.domain.model.Employee;
import com.reactive.employee.manager.domain.model.Vacation;
import com.reactive.employee.manager.domain.service.northbound.EmployeeDirectorNonBlockingService;
import com.reactive.employee.manager.domain.service.northbound.EmployeeDirectorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@Component
public class EmployeeDirectorHandlerImpl implements EmployeeDirectorHandler {

    public static final Logger Employee_Director_Logger = LoggerFactory.getLogger(EmployeeDirectorHandlerImpl.class);

    @Autowired
    private EmployeeDirectorService employeeDirectorService;

    @Autowired
    private ApplicationErrorHandler applicationErrorHandler;

    @Autowired
    private EmployeeDirectorNonBlockingService employeeDirectorNonBlockingService;

    @Override
    public Mono<ServerResponse> createVacationRequest(ServerRequest serverRequest) {
        try{
            Mono<Vacation> vacationMono = serverRequest.bodyToMono(Vacation.class);
            String username = serverRequest.pathVariable("username");
            Employee employee = employeeDirectorService.getEmployeeFromManager(username);
            List<String> reportsTos = employee.getReportsTo();
            List<Employee> reportsToEmployee = new ArrayList<>();
            for(String reportsTo : reportsTos){
                Employee employeeManager = employeeDirectorService.getEmployeeFromManager(reportsTo);
                reportsToEmployee.add(employeeManager);
            }
            return vacationMono
                    .flatMap(vacationRequest -> {
                        employeeDirectorService.validateVacation(employee, vacationRequest);
                        vacationRequest.setRequester(username);
                        vacationRequest.setRequestedDate(new Date());
                        vacationRequest.setStatus("NEW");
                        return employeeDirectorService.addVacationToApprovalList(reportsToEmployee, vacationRequest, employee);
                    });
        }catch (EmployeeDirectorException e){

            return applicationErrorHandler.handleDownStreamError(e);
        }
    }

    @Override
    public Mono<ServerResponse> approveVacationRequest(ServerRequest serverRequest) {
        try{
        Mono<Vacation> vacationMono = serverRequest.bodyToMono(Vacation.class);
        String username_approver = serverRequest.pathVariable("username");
        Employee employee_approver = employeeDirectorNonBlockingService.getEmployeeFromManager(username_approver).block();
            return vacationMono
                    .flatMap(vacationRequest -> {
                        return employeeDirectorService.approveVacationRequest(employee_approver, vacationRequest);
                    });

        }catch (EmployeeDirectorException e){

            return applicationErrorHandler.handleDownStreamError(e);
        }
    }

    @Override
    public Mono<ServerResponse> rejectVacationRequest(ServerRequest serverRequest) {
        //TODO Functionalities for reject should be implemented as per described in LLD
        try{
            Mono<Vacation> vacationMono = serverRequest.bodyToMono(Vacation.class);
            String username_approver = serverRequest.pathVariable("username");
            Mono<Employee> employee_approver = employeeDirectorNonBlockingService.getEmployeeFromManager(username_approver);
            return vacationMono
                    .flatMap(vacationRequest -> {
                        vacationRequest.setStatus("REJECTED");
                        return ServerResponse.status(HttpStatus.OK)
                                .contentType(APPLICATION_JSON)
                                .body(Mono.just(vacationRequest), Vacation.class);
                    });

        }catch (EmployeeDirectorException e){

            return applicationErrorHandler.handleDownStreamError(e);
        }
    }

    @Override
    public Mono<ServerResponse> getApprovalList(ServerRequest serverRequest) {
        //TODO Functionalities for reject should be implemented as per described in LLD, Dynamic Search will be added
            String username_approver = serverRequest.pathVariable("username");
            Mono<Employee> employee_approver = employeeDirectorNonBlockingService.getEmployeeFromManager(username_approver);
            Mono<ServerResponse> notFound = ServerResponse.notFound().build();
            return employee_approver.flatMap( e -> ServerResponse.ok()
                    .contentType(APPLICATION_JSON)
                    .body(fromValue(e.getApprovalList()))).switchIfEmpty(notFound);

    }

    @Override
    public Mono<ServerResponse> getUserVacation(ServerRequest serverRequest) {
        //TODO Functionalities for reject should be implemented as per described in LLD, Dynamic Search will be added
            String username = serverRequest.pathVariable("username");
            Mono<Employee> employee = employeeDirectorNonBlockingService.getEmployeeFromManager(username);
            Mono<ServerResponse> notFound = ServerResponse.notFound().build();
            return employee.flatMap( e -> ServerResponse.ok()
                    .contentType(APPLICATION_JSON)
                    .body(fromValue(e.getVacations()))).switchIfEmpty(notFound);

    }

    @Override
    public Mono<ServerResponse> getEmployee(ServerRequest request) {
        String id = request.pathVariable("username");
        Employee_Director_Logger.debug("Get Employee for PID : {} ", id);
            Mono<Employee> employee = employeeDirectorNonBlockingService.getEmployeeFromManager(id);
            Mono<ServerResponse> notFound = ServerResponse.notFound().build();
            return employee.flatMap( e -> ServerResponse.ok()
                    .contentType(APPLICATION_JSON)
                    .body(fromValue(e))).switchIfEmpty(notFound);

    }

    @Override
    public Mono<ServerResponse> patchEmployee(ServerRequest serverRequest) {
        try{
            Mono<Employee> employeeMono = serverRequest.bodyToMono(Employee.class);
            String system_username = serverRequest.pathVariable("systemUser");
            String employeeUserName = serverRequest.pathVariable("username");
            return employeeMono
                    .flatMap(employeePatched -> {
                        employeePatched.setUsername(employeeUserName);
                        return employeeDirectorNonBlockingService.patchEmployee(system_username, employeePatched);
                    });

        }catch (EmployeeDirectorException e){

            return applicationErrorHandler.handleDownStreamError(e);
        }
    }

    @Override
    public Mono<ServerResponse> saveEmployee(ServerRequest serverRequest) {
            Mono<Employee> employeeMono = serverRequest.bodyToMono(Employee.class);
            String system_username = serverRequest.pathVariable("systemUser");
            return employeeMono
                    .flatMap(employee -> {
                        return employeeDirectorNonBlockingService.createEmployee(system_username, employee);
                    });

    }

    @Override
    public Mono<ServerResponse> deleteEmployee(ServerRequest serverRequest) {
        try{
            String system_username = serverRequest.pathVariable("systemUser");
            String employeeUserName = serverRequest.pathVariable("username");
            return employeeDirectorService.deleteEmployee(system_username, employeeUserName);

        }catch (EmployeeDirectorException e){

            return applicationErrorHandler.handleDownStreamError(e);
        }
    }

    @Override
    public Mono<ServerResponse> getAllApprovalList(ServerRequest serverRequest) {
        try{
            String system_username = serverRequest.pathVariable("systemUser");
            Mono<Employee> employeeMono = serverRequest.bodyToMono(Employee.class);
            return employeeMono
                    .flatMap(employee -> {
                            return employeeDirectorNonBlockingService.getAllEmployees(employee, system_username);
                    });

        }catch (EmployeeDirectorException e){

            return applicationErrorHandler.handleDownStreamError(e);
        }
    }

}
