package com.reactive.employee.manager.domain.service.southbound.impl;

import com.reactive.employee.manager.domain.constants.EmployeeDirectorConstants;
import com.reactive.employee.manager.domain.exception.EmployeeDirectorException;
import com.reactive.employee.manager.domain.metrics.ExternalSystemMetrics;
import com.reactive.employee.manager.domain.metrics.ExternalSystemMetricsEntity;
import com.reactive.employee.manager.domain.model.Employee;
import com.reactive.employee.manager.domain.service.southbound.EmployeeManagerNonBlockingService;
import com.reactive.employee.manager.domain.stub.keycloak.model.KeycloakUser;
import com.reactive.employee.manager.domain.utility.SystemTokenStore;
import com.reactive.employee.manager.domain.utility.UriAnonymizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class EmployeeManagerNonBlockingServiceImpl implements EmployeeManagerNonBlockingService {

    @Autowired
    @Qualifier("systemTokenStore")
    private SystemTokenStore systemTokenStore;

    @Value("${employee.manager.base.uri}")
    private String employeeManagerBaseURI;

    @Value("${keycloak.user.base.uri}")
    private String keycloakUserBaseURI;

    @Autowired
    private ExternalSystemMetrics externalSystemMetrics;

    @Autowired
    private UriAnonymizer uriAnonymizer;

    @Autowired
    private WebClient webClient;

    public static final Logger Employee_Management_NonBlocking_Director_Logger = LoggerFactory.getLogger(EmployeeManagerNonBlockingServiceImpl.class);

    @Override
    public Mono<Employee> getEmployee(String username, KeycloakUser system_user) throws EmployeeDirectorException {
        final String url = employeeManagerBaseURI + "/" +username;
        String accessToken = systemTokenStore.getAccessToken( system_user);
        final long startTime = System.currentTimeMillis();
        try{
            Employee_Management_NonBlocking_Director_Logger.info("Starting NON-BLOCKING GET Controller!");
            Mono<Employee> employeeMono = webClient
                    .get()
                    .uri(url)
                    .header(EmployeeDirectorConstants.HEADER_KEY_AUTH,EmployeeDirectorConstants.AUTH_SCHEME + EmployeeDirectorConstants.SEPARATOR + accessToken)
                    .retrieve()
                    .bodyToMono(Employee.class)
                    .doOnNext(employee1 -> {
                        final long endTime = System.currentTimeMillis();
                        externalSystemMetrics.createExternalMetrics(createExternalMetricsData(endTime-startTime, EmployeeDirectorConstants.EMPLOYEE_MANAGER, HttpStatus.OK.toString(), uriAnonymizer.maskIdentifier(employeeManagerBaseURI), HttpMethod.GET.toString()));

                    });
            Employee_Management_NonBlocking_Director_Logger.info("Exiting NON-BLOCKING GET Controller!");

            return employeeMono;
        }catch (Exception e){
            if(e instanceof HttpClientErrorException){
                HttpStatus httpStatus = ((HttpClientErrorException) e).getStatusCode();
                refreshAccessTokenIfUnauthorized(httpStatus);
                throw new EmployeeDirectorException(HttpStatus.INTERNAL_SERVER_ERROR.toString(),e.getMessage(),e,url);
            }else if(e instanceof ResourceAccessException){
                throw new EmployeeDirectorException(HttpStatus.INTERNAL_SERVER_ERROR.toString(),e.getMessage(),e,url);
            }else{
                throw new EmployeeDirectorException(HttpStatus.INTERNAL_SERVER_ERROR.toString(),e.getMessage(),e,url);
            }
        }
    }

    @Override
    public Mono<Employee> createEmployee(Employee employee, KeycloakUser system_user) throws EmployeeDirectorException {
        String uri = employeeManagerBaseURI;
        String accessToken = systemTokenStore.getAccessToken(system_user);
        final long startTime = System.currentTimeMillis();
            Mono<Employee> employeeMono = webClient
                    .post()
                    .uri(uri)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .header(EmployeeDirectorConstants.HEADER_KEY_AUTH,EmployeeDirectorConstants.AUTH_SCHEME + EmployeeDirectorConstants.SEPARATOR + accessToken)
                    .body(Mono.just(employee), Employee.class)
                    .retrieve()
                    .onStatus(HttpStatus::is4xxClientError, clientResponse -> {

                            HttpStatus httpStatus = clientResponse.statusCode();
                            refreshAccessTokenIfUnauthorized(httpStatus);
                            throw new EmployeeDirectorException(HttpStatus.INTERNAL_SERVER_ERROR.toString(), clientResponse.toString(), clientResponse.createException().block(),  uri);
                    })
                    .bodyToMono(Employee.class)
                    .doOnNext(employee1 -> {
                        final long endTime = System.currentTimeMillis();
                        externalSystemMetrics.createExternalMetrics(createExternalMetricsData(endTime-startTime, EmployeeDirectorConstants.EMPLOYEE_MANAGER, HttpStatus.CREATED.toString(), uriAnonymizer.maskIdentifier(employeeManagerBaseURI), HttpMethod.POST.toString()));
                    });
            return employeeMono;
    }

    @Override
    public void deleteEmployee(String username, KeycloakUser system_user) throws EmployeeDirectorException {
        final String uri = employeeManagerBaseURI + "/" +username;
        String accessToken = systemTokenStore.getAccessToken(system_user);
        final long startTime = System.currentTimeMillis();
        webClient
                .delete()
                .uri(uri)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .header(EmployeeDirectorConstants.HEADER_KEY_AUTH,EmployeeDirectorConstants.AUTH_SCHEME + EmployeeDirectorConstants.SEPARATOR + accessToken)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                    HttpStatus httpStatus = clientResponse.statusCode();
                    refreshAccessTokenIfUnauthorized(httpStatus);
                    throw new EmployeeDirectorException(HttpStatus.INTERNAL_SERVER_ERROR.toString(), clientResponse.toString(), clientResponse.createException().block(),  uri);
                });
        final long endTime = System.currentTimeMillis();
        externalSystemMetrics.createExternalMetrics(createExternalMetricsData(endTime-startTime, EmployeeDirectorConstants.EMPLOYEE_MANAGER, HttpStatus.CREATED.toString(), uriAnonymizer.maskIdentifier(employeeManagerBaseURI), HttpMethod.DELETE.toString()));
        Employee_Management_NonBlocking_Director_Logger.info("Employee has been deleted with DELETE operation PID : {}", username);


    }

    @Override
    public Mono<Employee> patchEmployee(String username, Employee employee, KeycloakUser system_user) throws EmployeeDirectorException {
        final String uri = employeeManagerBaseURI + "/" +username;
        String accessToken = systemTokenStore.getAccessToken(system_user);
        final long startTime = System.currentTimeMillis();
            Mono<Employee> employeeMono = webClient
                    .patch()
                    .uri(uri)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .header(EmployeeDirectorConstants.HEADER_KEY_AUTH,EmployeeDirectorConstants.AUTH_SCHEME + EmployeeDirectorConstants.SEPARATOR + accessToken)
                    .body(Mono.just(employee), Employee.class)
                    .retrieve()
                    .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                        if(clientResponse.rawStatusCode() == 401 || clientResponse.rawStatusCode() == 403){
                            HttpStatus httpStatus = clientResponse.statusCode();
                            refreshAccessTokenIfUnauthorized(httpStatus);
                            throw new EmployeeDirectorException(HttpStatus.INTERNAL_SERVER_ERROR.toString(), clientResponse.toString(), clientResponse.createException().block(),  uri);
                        }else if(clientResponse.rawStatusCode() == 404){
                            throw new EmployeeDirectorException(HttpStatus.INTERNAL_SERVER_ERROR.toString(), "Not Found", clientResponse.createException().block(),  uri);
                        }else{
                            throw new EmployeeDirectorException(HttpStatus.INTERNAL_SERVER_ERROR.toString(), "Something went wrong", clientResponse.createException().block(),  uri);
                        }
                    })
                    .bodyToMono(Employee.class)
                    .doOnNext(employee1 -> {
                        final long endTime = System.currentTimeMillis();
                        externalSystemMetrics.createExternalMetrics(createExternalMetricsData(endTime-startTime, EmployeeDirectorConstants.EMPLOYEE_MANAGER, HttpStatus.CREATED.toString(), uriAnonymizer.maskIdentifier(employeeManagerBaseURI), HttpMethod.POST.toString()));
                    });
            Employee_Management_NonBlocking_Director_Logger.info("Employee has been changed with PATCH operation PID : {}", username);
            return employeeMono;


    }

    @Override
    public Mono<KeycloakUser> getSystemUser(String username) throws EmployeeDirectorException {
        return null;
    }

    @Override
    public Flux<Employee> getAllEmployee(Employee employee, KeycloakUser system_user) {
        String accessToken = systemTokenStore.getAccessToken(system_user);

        final long startTime = System.currentTimeMillis();
            Employee_Management_NonBlocking_Director_Logger.info("Starting NON-BLOCKING Controller!");
            Flux<Employee> employeeFlux = webClient
                    .get()
                    .uri(employeeManagerBaseURI)
                    .header(EmployeeDirectorConstants.HEADER_KEY_AUTH,EmployeeDirectorConstants.AUTH_SCHEME + EmployeeDirectorConstants.SEPARATOR + accessToken)
                    .retrieve()
                    .onStatus(HttpStatus::is4xxClientError, clientResponse -> {

                        HttpStatus httpStatus = clientResponse.statusCode();
                        refreshAccessTokenIfUnauthorized(httpStatus);
                        throw new EmployeeDirectorException(HttpStatus.INTERNAL_SERVER_ERROR.toString(), clientResponse.toString(), clientResponse.createException().block(),  employeeManagerBaseURI);
                    })
                    .bodyToFlux(Employee.class)
                    .doOnNext(employee1 -> {
                        final long endTime = System.currentTimeMillis();
                        externalSystemMetrics.createExternalMetrics(createExternalMetricsData(endTime-startTime, EmployeeDirectorConstants.EMPLOYEE_MANAGER, HttpStatus.OK.toString(), uriAnonymizer.maskIdentifier(employeeManagerBaseURI), HttpMethod.GET.toString()));
                        Employee_Management_NonBlocking_Director_Logger.info(employee1.toString());
                    });

            Employee_Management_NonBlocking_Director_Logger.info("Exiting NON-BLOCKING Controller!");

            return employeeFlux;


    }

    @Override
    public Flux<Employee> getEmployeeByCriteria(Employee employee, KeycloakUser system_user) throws EmployeeDirectorException {
        return null;
    }

    private void refreshAccessTokenIfUnauthorized(HttpStatus httpStatus) {
        if (httpStatus == HttpStatus.UNAUTHORIZED) {
            Employee_Management_NonBlocking_Director_Logger.info("AccessToken will be refreshed");
            // systemTokenStore.refreshAccessToken();
        }
    }

    private ExternalSystemMetricsEntity createExternalMetricsData(long elapsedTime, String externalSystem, String status, String uri, String httpMethod){

        ExternalSystemMetricsEntity externalSystemMetricsEntity =  new ExternalSystemMetricsEntity();
        externalSystemMetricsEntity.setElapsedTime(elapsedTime);
        externalSystemMetricsEntity.setExternalSystem(externalSystem);
        externalSystemMetricsEntity.setUri(uri);
        externalSystemMetricsEntity.setStatusCode(status);
        externalSystemMetricsEntity.setHttpMethod(httpMethod);
        return externalSystemMetricsEntity;
    }
}
