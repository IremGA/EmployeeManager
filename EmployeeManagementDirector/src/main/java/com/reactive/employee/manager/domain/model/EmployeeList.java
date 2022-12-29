package com.reactive.employee.manager.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Data
@Document
public class EmployeeList {
    private List<Employee> employees = new ArrayList<>();

}
