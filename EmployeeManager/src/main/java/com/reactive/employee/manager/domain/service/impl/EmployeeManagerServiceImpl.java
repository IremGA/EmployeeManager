package com.reactive.employee.manager.domain.service.impl;

import com.reactive.employee.manager.domain.constants.EmployeeManagerConstants;
import com.reactive.employee.manager.domain.exception.EmployeeManagerException;
import com.reactive.employee.manager.domain.mapstruct.EmployeeMapper;
import com.reactive.employee.manager.domain.model.Employee;
import com.reactive.employee.manager.domain.repository.EmployeeManagerRepository;
import com.reactive.employee.manager.domain.service.EmployeeManagerService;
import com.reactive.employee.manager.domain.stub.keycloak.model.CredentialReporesentation;
import com.reactive.employee.manager.domain.stub.keycloak.model.KeycloakUser;
import com.reactive.employee.manager.domain.stub.keycloak.repository.KeycloakStubRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON;


@Service
public class EmployeeManagerServiceImpl implements EmployeeManagerService {

    @Autowired
    private EmployeeManagerRepository repository;

    @Autowired
    private KeycloakStubRepository keycloakStubRepository;

    @Resource
    private EmployeeMapper employeeMapper;

    public static final Logger Employee_Manager_Logger = LoggerFactory.getLogger(EmployeeManagerServiceImpl.class);

    public Mono<Employee> patchEmployee(String employeeId, Employee employee) throws EmployeeManagerException {

        Mono<Employee> employeeMono = getEmployee(employeeId);
        Mono<Employee> patchedEmployeeMono = employeeMono.flatMap(employeeExisting -> {

            Employee patchedEmployee = employeeMapper.patchEmployeeToExistingEmployee(employee, employeeExisting);
            Mono<Employee> employeeMono1 = Mono.just(patchedEmployee);
            Employee_Manager_Logger.info("Entity has been patched to cache repo: {}", patchedEmployee);
                return employeeMono1;

        });

        return patchedEmployeeMono;
    }
    
    @Override
    public Mono<Employee> createEmployee(Employee employee) throws EmployeeManagerException {

        //TODO Verify User
        Mono<KeycloakUser> keycloakUserMono = keycloakStubRepository.findById(employee.getUsername());
        keycloakUserMono.flatMap(keycloakUser -> {
            if(keycloakUser != null){
                EmployeeManagerException employeeManagerException = new EmployeeManagerException();
                employeeManagerException.setReason(keycloakUser.getUsername() + "user already exists !");
                employeeManagerException.setCode(HttpStatus.BAD_REQUEST.toString());
                return ServerResponse.status(HttpStatus.BAD_REQUEST)
                        .contentType(APPLICATION_JSON)
                        .body(Mono.just(employeeManagerException), EmployeeManagerException.class);
            }
            return Mono.just(keycloakUser);
        });
        //TODO Set initial vacation day while creating user
        employee.setVacationDayLeft(EmployeeManagerConstants.DEFAULT_VACATION_DAY);
        employee.setVacationRenewalDate(new Date());
        //TODO set InitialPassword in Keycloak (inorder to send a POST request to create keycloak user, stub is created)
        //TODO Set default password
        KeycloakUser keycloakUser = employeeMapper.mapEmployeetoKeycloakUser(employee);
        CredentialReporesentation credentialReporesentation = new CredentialReporesentation();
        credentialReporesentation.setType("password");
        credentialReporesentation.setValue(EmployeeManagerConstants.DEFAULT_PASSWORD);
        credentialReporesentation.setTemporary(true);
        credentialReporesentation.setCreatedDate(new Date());
        keycloakUser.setCredentials(List.of(credentialReporesentation));
        keycloakUser.setEnabled(true);
        keycloakStubRepository.save(keycloakUser).subscribe(s->Employee_Manager_Logger.info("KeycloakUser has been saved to cache repo: {}", s));

        // TODO Save Employee to Mongo repo
        Mono<Employee> employeeMono = repository.save(employee);
        Employee_Manager_Logger.info("Entity has been saved to cache repo: {}", employee);
        return employeeMono;
    }

    @Override
    public Mono<Employee> getEmployee(String id) throws EmployeeManagerException {
        Mono<Employee>  employeeMono = repository.findById(id);

        return employeeMono;
    }

    @Override
    public Flux<Employee> getAllEmployees(Employee employee) {

            return repository.findAll(Example.of(employee));
    }

    @Override
    public void deleteEmployee(String id) throws EmployeeManagerException {

    }

}
