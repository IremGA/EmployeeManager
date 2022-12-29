package com.reactive.employee.manager.domain.service;

import com.reactive.employee.manager.domain.exception.EmployeeManagerException;
import com.reactive.employee.manager.domain.model.Employee;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EmployeeManagerService {
    public Mono<Employee> patchEmployee(String employeeId, Employee employee) throws EmployeeManagerException;

    public Mono<Employee> createEmployee(Employee employee) throws EmployeeManagerException;

    public Mono<Employee> getEmployee(String id) throws EmployeeManagerException;

    public Flux<Employee> getAllEmployees(Employee employee);

    public void deleteEmployee(String id) throws EmployeeManagerException;
}
