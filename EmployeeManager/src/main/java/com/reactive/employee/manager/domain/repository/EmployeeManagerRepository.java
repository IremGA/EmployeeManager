package com.reactive.employee.manager.domain.repository;

import com.reactive.employee.manager.domain.model.Employee;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface EmployeeManagerRepository
        extends ReactiveMongoRepository<Employee, String> {
}
