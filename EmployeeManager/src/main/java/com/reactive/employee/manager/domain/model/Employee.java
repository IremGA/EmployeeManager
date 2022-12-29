package com.reactive.employee.manager.domain.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Document
public class Employee {

    @Id
    @NotNull
    @JsonProperty("username")
    private String username;

    @JsonProperty("name")
    private String name;

    @JsonProperty("surname")
    private String surname;

    @JsonProperty("role")
    private Role role;

    @JsonProperty("reportsTo")
    private List<String> reportsTo;

    @JsonProperty("vacationDayLeft")
    private Float vacationDayLeft;

    @JsonProperty("salary")
    private Amount salary;

    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @JsonProperty("dateOfBirth")
    private Date dateOfBirth;

    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @JsonProperty("dateOfEmployment")
    private Date dateOfEmployment;

    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @JsonProperty("vacationRenewalDate")
    private Date vacationRenewalDate;

    @JsonProperty("vacations")
    private List<Vacation> vacations;

    @JsonProperty("email")
    private  String email;

    @JsonProperty("groups")
    private  List<String> groups;

    @JsonProperty("approvalList")
    private List<Vacation> approvalList;
}
