package com.reactive.employee.manager.domain.service.southbound;

import com.reactive.employee.manager.domain.exception.EmployeeDirectorException;
import com.reactive.employee.manager.domain.model.Employee;
import com.reactive.employee.manager.domain.stub.keycloak.model.KeycloakUser;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.client.HttpClientErrorException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EmployeeManagerNonBlockingService {

    @Retryable(value = {HttpClientErrorException.class}, maxAttempts = 3, backoff = @Backoff(200))
    public Mono<Employee> getEmployee(String username, KeycloakUser system_user) throws EmployeeDirectorException;

    @Retryable(value = {HttpClientErrorException.class}, maxAttempts = 3, backoff = @Backoff(200))
    public Mono<Employee> createEmployee(Employee employee, KeycloakUser system_user) throws EmployeeDirectorException;
    @Retryable(value = {HttpClientErrorException.class}, maxAttempts = 3, backoff = @Backoff(200))
    public void deleteEmployee(String username, KeycloakUser system_user) throws EmployeeDirectorException;
    @Retryable(value = {HttpClientErrorException.class}, maxAttempts = 3, backoff = @Backoff(200))
    public Mono<Employee> patchEmployee(String username, Employee employee, KeycloakUser system_user) throws EmployeeDirectorException;
    @Retryable(value = {HttpClientErrorException.class}, maxAttempts = 3, backoff = @Backoff(200))
    public Mono<KeycloakUser> getSystemUser(String username) throws EmployeeDirectorException;

    @Retryable(value = {HttpClientErrorException.class}, maxAttempts = 3, backoff = @Backoff(200))
    public Flux<Employee> getAllEmployee(Employee employee, KeycloakUser system_user);
    @Retryable(value = {HttpClientErrorException.class}, maxAttempts = 3, backoff = @Backoff(200))
    public Flux<Employee> getEmployeeByCriteria(Employee employee, KeycloakUser system_user) throws EmployeeDirectorException;
}
