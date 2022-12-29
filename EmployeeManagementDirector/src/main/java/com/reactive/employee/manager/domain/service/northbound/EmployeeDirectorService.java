package com.reactive.employee.manager.domain.service.northbound;

import com.reactive.employee.manager.domain.exception.EmployeeDirectorException;
import com.reactive.employee.manager.domain.model.Employee;
import com.reactive.employee.manager.domain.model.Vacation;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.List;

public interface EmployeeDirectorService {

    public Mono<Employee> getEmployee(String id) throws EmployeeDirectorException;

    public Employee getEmployeeFromManager(String id) throws EmployeeDirectorException;

    Mono<ServerResponse> validateVacation(Employee employee, Vacation vacationRequest) throws EmployeeDirectorException;

    Mono<ServerResponse> addVacationToApprovalList(List<Employee> reportsToEmployee, Vacation vacationRequest, Employee employee) throws EmployeeDirectorException;;

    Mono<ServerResponse> approveVacationRequest(Employee employee_approver, Vacation vacationRequest) throws EmployeeDirectorException;

    Mono<ServerResponse> patchEmployee(String system_username, Employee employeePatched) throws EmployeeDirectorException;

    Mono<ServerResponse> createEmployee(String system_username, Employee employee) throws EmployeeDirectorException;

    Mono<ServerResponse> deleteEmployee(String system_username, String employeeUserName) throws EmployeeDirectorException;

    Mono<ServerResponse> getAllEmployeesByCriteria(Employee employee, String system_username) throws EmployeeDirectorException;

    Mono<ServerResponse> getAllEmployees(Employee employee, String system_username) throws EmployeeDirectorException;
}
