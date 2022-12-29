package com.reactive.employee.manager.domain.service.northbound.impl;

import com.reactive.employee.manager.domain.constants.EmployeeDirectorConstants;
import com.reactive.employee.manager.domain.exception.EmployeeDirectorException;
import com.reactive.employee.manager.domain.model.Employee;
import com.reactive.employee.manager.domain.model.Vacation;
import com.reactive.employee.manager.domain.service.northbound.EmployeeDirectorNonBlockingService;
import com.reactive.employee.manager.domain.service.southbound.EmployeeManagerNonBlockingService;
import com.reactive.employee.manager.domain.service.southbound.EmployeeManagerService;
import com.reactive.employee.manager.domain.stub.keycloak.model.KeycloakUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@Service
public class EmployeeDirectorNonBlockingServiceImpl implements EmployeeDirectorNonBlockingService {

    @Autowired
    private EmployeeManagerService employeeManagerService;

    @Autowired
    private EmployeeManagerNonBlockingService employeeManagerNonBlockingService;


    public static final Logger Employee_Director_NonBlocking_Logger = LoggerFactory.getLogger(EmployeeDirectorNonBlockingServiceImpl.class);

    @Override
    public Mono<Employee> getEmployee(String id) throws EmployeeDirectorException {
        return null;
    }

    @Override
    public Mono<Employee> getEmployeeFromManager(String id) throws EmployeeDirectorException {
        KeycloakUser keycloakUser = employeeManagerService.getSystemUser(id);
        keycloakUser.setClient_id("getEmployee");
        Mono<Employee> employee = employeeManagerNonBlockingService.getEmployee(id, keycloakUser);
        return employee;
    }

    @Override
    public Mono<ServerResponse> validateVacation(Employee employee, Vacation vacationRequest) throws EmployeeDirectorException {
        Date vacationRenewalDate = employee.getVacationRenewalDate();
        Date todayDate = new Date();
        long diffInMillies = Math.abs(todayDate.getTime() - vacationRenewalDate.getTime());
        long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

        float days = employee.getVacationDayLeft();
        //TODO : If  vacation renewal day is bigger than 1 year; first vacation day will be renewed
        if(diff > 360){
            Employee_Director_NonBlocking_Logger.info("Employee {} has right to renew vacation days", employee.getUsername());
            //TODO RENEW the Employee Days Left
            days = days + EmployeeDirectorConstants.DEFAULT_VACATION_DAY;
            employee.setVacationRenewalDate(new Date());
            employee.setVacationDayLeft(days);

            KeycloakUser keycloakUser = employeeManagerService.getSystemUser(vacationRequest.getRequester());
            keycloakUser.setClient_id("requestVacation");
            employeeManagerNonBlockingService.patchEmployee(employee.getUsername(),employee,keycloakUser);
            Employee_Director_NonBlocking_Logger.info("Employee {} remaining vacation days is renewed to {}", employee.getUsername(), Float.toString(days));
        }

        //TODO: Extract Days-Excluding holidays and weekends will be planned later
        Date vacationStartDate = vacationRequest.getVacationDay().getVacationStartDate();
        Date vacationEndDate = vacationRequest.getVacationDay().getVacationEndDate();
        long diffInMilliesVacation = Math.abs(vacationEndDate.getTime() - vacationStartDate.getTime());
        long diffVacation = TimeUnit.DAYS.convert(diffInMilliesVacation, TimeUnit.MILLISECONDS);

        //TODO Validate Sufficient Days left for request
        if(days < diffVacation){
            EmployeeDirectorException employeeDirectorException = new EmployeeDirectorException();
            employeeDirectorException.setReason("There is no more day left to request vacation ");
            employeeDirectorException.setCode(HttpStatus.BAD_REQUEST.toString());
            return ServerResponse.status(HttpStatus.BAD_REQUEST)
                    .contentType(APPLICATION_JSON)
                    .body(Mono.just(employeeDirectorException), EmployeeDirectorException.class);
        }
        //Set calculated days
        String diff1 = Long.toString(diffVacation);
        vacationRequest.setDaysUsed(Float.parseFloat(diff1));

        return null;
    }

    @Override
    public Mono<ServerResponse> addVacationToApprovalList(List<Employee> reportsToEmployee, Vacation vacationRequest, Employee employee) throws EmployeeDirectorException {
        KeycloakUser keycloakUser = employeeManagerService.getSystemUser(vacationRequest.getRequester());
        keycloakUser.setClient_id("requestVacation");

        Employee patchEmployee = new Employee();
        patchEmployee.setApprovalList(List.of(vacationRequest));
        for(Employee manager : reportsToEmployee){
            manager.setApprovalList(List.of(vacationRequest));
            //TODO add approval to approvals of Manager
            employeeManagerNonBlockingService.patchEmployee(manager.getUsername(),patchEmployee,keycloakUser);
            Employee_Director_NonBlocking_Logger.info("Vacation Request is added to approval list");
        }
        employee.setVacations(List.of(vacationRequest));
        //TODO add vacation to vacationlist of Employee
        Employee_Director_NonBlocking_Logger.info("Vacation Request is added to employee vacation list");
        employeeManagerNonBlockingService.patchEmployee(employee.getUsername(),employee,keycloakUser);
        return ServerResponse.status(HttpStatus.OK)
                .contentType(APPLICATION_JSON)
                .body(Mono.just(vacationRequest), EmployeeDirectorException.class);
    }

    @Override
    public Mono<ServerResponse> approveVacationRequest(Employee employee_approver, Vacation vacationRequest) throws EmployeeDirectorException {
        //TODO approveVacation
        KeycloakUser keycloakUser = employeeManagerService.getSystemUser(employee_approver.getUsername());
        keycloakUser.setClient_id("manageVacation");
        Employee requesterEmployee = employeeManagerService.getEmployee(vacationRequest.getRequester(), keycloakUser);
        vacationRequest.setApproveDate(new Date());
        vacationRequest.setStatus("APPROVED");

        //SET Left Days of Employee
        Employee employee = new Employee();
        employee.setVacations(List.of(vacationRequest));
        float vacationDayLeft = requesterEmployee.getVacationDayLeft();
        float usedDay = vacationRequest.getDaysUsed();
        float result = vacationDayLeft - usedDay;
        employee.setVacationDayLeft(result);

        Employee manager = new Employee();
        manager.setApprovalList(List.of(vacationRequest));

        employeeManagerNonBlockingService.patchEmployee(requesterEmployee.getUsername(),employee,keycloakUser);
        employeeManagerNonBlockingService.patchEmployee(employee_approver.getUsername(),manager, keycloakUser);
        Employee_Director_NonBlocking_Logger.info("Vacation Request is approved for user : {}", requesterEmployee.getUsername());
        return ServerResponse.status(HttpStatus.OK)
                .contentType(APPLICATION_JSON)
                .body(Mono.just(vacationRequest), Employee.class);
    }

    @Override
    public Mono<ServerResponse> patchEmployee(String system_username, Employee employeePatched) throws EmployeeDirectorException {
        KeycloakUser keycloakUser = employeeManagerService.getSystemUser(system_username);
        keycloakUser.setClient_id("manageEmployee");
        Mono<Employee> employee = employeeManagerNonBlockingService.patchEmployee(employeePatched.getUsername(),employeePatched, keycloakUser);
        return ServerResponse.status(HttpStatus.OK)
                .contentType(APPLICATION_JSON)
                .body(Mono.just(employee), Employee.class);
    }

    @Override
    public Mono<ServerResponse> createEmployee(String system_username, Employee employee) throws EmployeeDirectorException {
        KeycloakUser keycloakUser = employeeManagerService.getSystemUser(system_username);
        keycloakUser.setClient_id("manageEmployee");
        Mono<Employee> employeeCreated = employeeManagerNonBlockingService.createEmployee(employee, keycloakUser);
        return ServerResponse.status(HttpStatus.OK)
                .contentType(APPLICATION_JSON)
                .body(employeeCreated, Employee.class);
    }

    @Override
    public Mono<ServerResponse> deleteEmployee(String system_username, String employeeUserName) throws EmployeeDirectorException {
        KeycloakUser keycloakUser = employeeManagerService.getSystemUser(system_username);
        keycloakUser.setClient_id("manageEmployee");
        employeeManagerNonBlockingService.deleteEmployee(employeeUserName, keycloakUser);
        return ServerResponse.status(HttpStatus.OK)
                .contentType(APPLICATION_JSON)
                .body(fromValue(employeeUserName));
    }

    @Override
    public Mono<ServerResponse> getAllEmployeesByCriteria(Employee employee, String system_username) throws EmployeeDirectorException {
        KeycloakUser keycloakUser = employeeManagerService.getSystemUser(system_username);
        keycloakUser.setClient_id("manageEmployee");
        Flux<Employee> employeeList = employeeManagerNonBlockingService.getEmployeeByCriteria(employee,keycloakUser);
        return ServerResponse.status(HttpStatus.OK)
                .contentType(APPLICATION_JSON)
                .body(employeeList,Employee.class);
    }

    @Override
    public Mono<ServerResponse> getAllEmployees(Employee employee, String system_username) throws EmployeeDirectorException {
        KeycloakUser keycloakUser = employeeManagerService.getSystemUser(system_username);
        keycloakUser.setClient_id("manageEmployee");
        Flux<Employee> employeeList = employeeManagerNonBlockingService.getAllEmployee(employee,keycloakUser);
        return ServerResponse.status(HttpStatus.OK)
                .contentType(APPLICATION_JSON)
                .body(employeeList, Employee.class);
    }
}
