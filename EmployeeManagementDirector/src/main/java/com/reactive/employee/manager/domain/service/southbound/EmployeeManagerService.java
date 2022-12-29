package com.reactive.employee.manager.domain.service.southbound;

import com.reactive.employee.manager.domain.exception.EmployeeDirectorException;
import com.reactive.employee.manager.domain.model.Employee;
import com.reactive.employee.manager.domain.stub.keycloak.model.KeycloakUser;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

public interface EmployeeManagerService {

    @Retryable(value = {HttpClientErrorException.class}, maxAttempts = 3, backoff = @Backoff(200))
    public Employee getEmployee(String username, KeycloakUser system_user) throws EmployeeDirectorException;

    @Retryable(value = {HttpClientErrorException.class}, maxAttempts = 3, backoff = @Backoff(200))
    public Employee createEmployee(Employee employee, KeycloakUser system_user) throws EmployeeDirectorException;
    @Retryable(value = {HttpClientErrorException.class}, maxAttempts = 3, backoff = @Backoff(200))
    public void deleteEmployee(String username, KeycloakUser system_user) throws EmployeeDirectorException;
    @Retryable(value = {HttpClientErrorException.class}, maxAttempts = 3, backoff = @Backoff(200))
    public Employee patchEmployee(String username, Employee employee, KeycloakUser system_user) throws EmployeeDirectorException;
    @Retryable(value = {HttpClientErrorException.class}, maxAttempts = 3, backoff = @Backoff(200))
    public KeycloakUser getSystemUser(String username) throws EmployeeDirectorException;

    @Retryable(value = {HttpClientErrorException.class}, maxAttempts = 3, backoff = @Backoff(200))
    public List<Employee> getAllEmployee(KeycloakUser keycloakUser);
    @Retryable(value = {HttpClientErrorException.class}, maxAttempts = 3, backoff = @Backoff(200))
    public List<Employee> getEmployeeByCriteria(Employee employee, KeycloakUser system_user) throws EmployeeDirectorException;
}
