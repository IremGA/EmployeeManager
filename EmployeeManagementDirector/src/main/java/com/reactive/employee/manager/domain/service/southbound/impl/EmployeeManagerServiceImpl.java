package com.reactive.employee.manager.domain.service.southbound.impl;

import com.reactive.employee.manager.domain.constants.EmployeeDirectorConstants;
import com.reactive.employee.manager.domain.exception.EmployeeDirectorException;
import com.reactive.employee.manager.domain.metrics.ExternalSystemMetrics;
import com.reactive.employee.manager.domain.metrics.ExternalSystemMetricsEntity;
import com.reactive.employee.manager.domain.model.Employee;
import com.reactive.employee.manager.domain.model.EmployeeList;
import com.reactive.employee.manager.domain.service.southbound.EmployeeManagerService;
import com.reactive.employee.manager.domain.stub.keycloak.model.KeycloakUser;
import com.reactive.employee.manager.domain.utility.SystemTokenStore;
import com.reactive.employee.manager.domain.utility.UriAnonymizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmployeeManagerServiceImpl implements EmployeeManagerService {

    @Value("${employee.manager.base.path}")
    private String employeeManagerBasePath;

    @Value("${keycloak.user.base.path}")
    private String keycloakUserBasePath;
    @Autowired
    RestTemplate restTemplate;

    @Autowired
    private ExternalSystemMetrics externalSystemMetrics;

    @Autowired
    private UriAnonymizer uriAnonymizer;

    @Autowired
    @Qualifier("systemTokenStore")
    private SystemTokenStore systemTokenStore;

    public static final Logger Employee_Management_Director_Logger = LoggerFactory.getLogger(EmployeeManagerServiceImpl.class);

    @Override
    public Employee getEmployee(String username, KeycloakUser system_user) throws EmployeeDirectorException {

        final String url = employeeManagerBasePath + "/" +username;

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        Map<String,String> map = new HashMap<>();
        String accessToken = systemTokenStore.getAccessToken( system_user);
        if(accessToken != null && !accessToken.isEmpty()){
            map.put(EmployeeDirectorConstants.HEADER_KEY_AUTH, EmployeeDirectorConstants.AUTH_SCHEME + EmployeeDirectorConstants.SEPARATOR + accessToken);
        }

        headers.setAll(map);
        HttpEntity<?> request = new HttpEntity<>(headers);
        final long startTime = System.currentTimeMillis();
        ResponseEntity<Employee> response = null;
        try{
            response = restTemplate.exchange(url, HttpMethod.GET, request, Employee.class);
            if(response !=null ){
                Employee employee = response.getBody();
                Employee_Management_Director_Logger.info("Get Employee returned the result : {} ", employee.toString());
                final long endTime = System.currentTimeMillis();
                externalSystemMetrics.createExternalMetrics(createExternalMetricsData(endTime-startTime, EmployeeDirectorConstants.EMPLOYEE_MANAGER, response.getStatusCode().toString(), uriAnonymizer.maskIdentifier(url), HttpMethod.GET.toString()));
                return employee;
            }
        }catch (Exception e){
            if(e instanceof HttpClientErrorException){
                HttpStatus httpStatus = ((HttpClientErrorException) e).getStatusCode();
                refreshAccessTokenIfUnauthorized(httpStatus);
                throw new EmployeeDirectorException(HttpStatus.INTERNAL_SERVER_ERROR.toString(),e.getMessage(),e,request.toString());
            }else if(e instanceof ResourceAccessException){
                throw new EmployeeDirectorException(HttpStatus.INTERNAL_SERVER_ERROR.toString(),e.getMessage(),e,request.toString());
            }else{
                throw new EmployeeDirectorException(HttpStatus.INTERNAL_SERVER_ERROR.toString(),e.getMessage(),e,request.toString());
            }
        }

        return null;
    }

    @Override
    public List<Employee> getEmployeeByCriteria(Employee employee, KeycloakUser system_user) throws EmployeeDirectorException {

        String uri = employeeManagerBasePath + "/getByCriteria";
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        Map<String,String> map = new HashMap<>();
        map.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        map.put(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        String accessToken = systemTokenStore.getAccessToken(system_user);
        if(accessToken != null && !accessToken.isEmpty()){
            map.put(EmployeeDirectorConstants.HEADER_KEY_AUTH, EmployeeDirectorConstants.AUTH_SCHEME + EmployeeDirectorConstants.SEPARATOR + accessToken);
        }

        headers.setAll(map);

        HttpEntity<?> request = new HttpEntity<>(employee, headers);
        final long startTime = System.currentTimeMillis();
        try{
            ResponseEntity<EmployeeList> employeeRes = restTemplate.postForEntity(uri, request, EmployeeList.class);
            if(employeeRes !=null ){
                List<Employee> employeeList = employeeRes.getBody().getEmployees();
                Employee_Management_Director_Logger.info("Employee list for criteria: {} : {} ", employee.toString(), employeeList);
                final long endTime = System.currentTimeMillis();
                externalSystemMetrics.createExternalMetrics(createExternalMetricsData(endTime-startTime, EmployeeDirectorConstants.EMPLOYEE_MANAGER, employeeRes.getStatusCode().toString(), uriAnonymizer.maskIdentifier(uri), HttpMethod.POST.toString() ));
                return employeeList;
            }
        }catch (Exception e){
            if(e instanceof HttpClientErrorException){
                HttpStatus httpStatus = ((HttpClientErrorException) e).getStatusCode();
                refreshAccessTokenIfUnauthorized(httpStatus);
                throw new EmployeeDirectorException(HttpStatus.INTERNAL_SERVER_ERROR.toString(), e.getMessage(), e,  uri);
            }else if(e instanceof ResourceAccessException){
                throw new EmployeeDirectorException(HttpStatus.INTERNAL_SERVER_ERROR.toString(), e.getMessage(), e, uri);
            }else{
                throw new EmployeeDirectorException(HttpStatus.INTERNAL_SERVER_ERROR.toString(), e.getMessage(), e, uri);
            }
        }
        return null;
    }
    @Override
    public Employee createEmployee(Employee employee, KeycloakUser system_user) throws EmployeeDirectorException {

        String uri = employeeManagerBasePath;
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        Map<String,String> map = new HashMap<>();
        map.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        map.put(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        String accessToken = systemTokenStore.getAccessToken(system_user);
        if(accessToken != null && !accessToken.isEmpty()){
            map.put(EmployeeDirectorConstants.HEADER_KEY_AUTH, EmployeeDirectorConstants.AUTH_SCHEME + EmployeeDirectorConstants.SEPARATOR + accessToken);
        }

        headers.setAll(map);

        HttpEntity<?> request = new HttpEntity<>(employee, headers);
        final long startTime = System.currentTimeMillis();
        try{
            ResponseEntity<Employee> employeeRes = restTemplate.postForEntity(uri, request, Employee.class);
            if(employeeRes !=null ){
                Employee employeeCreated = employeeRes.getBody();
                Employee_Management_Director_Logger.info("Employee has been created  PID : {}", employeeCreated.getUsername());
                final long endTime = System.currentTimeMillis();
                externalSystemMetrics.createExternalMetrics(createExternalMetricsData(endTime-startTime, EmployeeDirectorConstants.EMPLOYEE_MANAGER, employeeRes.getStatusCode().toString(), uriAnonymizer.maskIdentifier(uri), HttpMethod.POST.toString() ));
                return employeeCreated;
            }
        }catch (Exception e){
            if(e instanceof HttpClientErrorException){
                HttpStatus httpStatus = ((HttpClientErrorException) e).getStatusCode();
                refreshAccessTokenIfUnauthorized(httpStatus);
                throw new EmployeeDirectorException(HttpStatus.INTERNAL_SERVER_ERROR.toString(), e.getMessage(), e,  uri);
            }else if(e instanceof ResourceAccessException){
                throw new EmployeeDirectorException(HttpStatus.INTERNAL_SERVER_ERROR.toString(), e.getMessage(), e, uri);
            }else{
                throw new EmployeeDirectorException(HttpStatus.INTERNAL_SERVER_ERROR.toString(), e.getMessage(), e, uri);
            }
        }
        return null;
    }

    private void refreshAccessTokenIfUnauthorized(HttpStatus httpStatus) {
        if(httpStatus == HttpStatus.UNAUTHORIZED){
            Employee_Management_Director_Logger.info("AccessToken will be refreshed");
           // systemTokenStore.refreshAccessToken();
        }
    }
    @Override
    public void deleteEmployee(String username, KeycloakUser system_user) throws EmployeeDirectorException{

        final String url = employeeManagerBasePath + "/" +username;

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        Map<String,String> map = new HashMap<>();
        String accessToken = systemTokenStore.getAccessToken(system_user);
        if(accessToken != null && !accessToken.isEmpty()){
            map.put(EmployeeDirectorConstants.HEADER_KEY_AUTH, EmployeeDirectorConstants.AUTH_SCHEME + EmployeeDirectorConstants.SEPARATOR + accessToken);
        }

        headers.setAll(map);
        HttpEntity<?> request = new HttpEntity<>(headers);
        final long startTime = System.currentTimeMillis();
        try{
            restTemplate.delete(url,request);
            Employee_Management_Director_Logger.info("Employee has been deleted PID : {}", username);
            final long endTime = System.currentTimeMillis();
            externalSystemMetrics.createExternalMetrics(createExternalMetricsData(endTime-startTime, EmployeeDirectorConstants.EMPLOYEE_MANAGER, HttpStatus.OK.toString(), uriAnonymizer.maskIdentifier(url), HttpMethod.DELETE.toString()));

        }catch (Exception e){
            if(e instanceof HttpClientErrorException){
                HttpStatus httpStatus = ((HttpClientErrorException) e).getStatusCode();
                refreshAccessTokenIfUnauthorized(httpStatus);
                throw new EmployeeDirectorException(HttpStatus.INTERNAL_SERVER_ERROR.toString(), e.getMessage(), e, url);
            }else if(e instanceof ResourceAccessException){
                throw new EmployeeDirectorException(HttpStatus.INTERNAL_SERVER_ERROR.toString(), e.getMessage(), e, url);
            }else{
                throw new EmployeeDirectorException(HttpStatus.INTERNAL_SERVER_ERROR.toString(), e.getMessage(), e, url);
            }
        }
    }
   @Override
    public Employee patchEmployee(String username, Employee employee,KeycloakUser systemUser) throws EmployeeDirectorException{

        final String url = employeeManagerBasePath + "/" +username;

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        Map<String,String> map = new HashMap<>();
        String accessToken = systemTokenStore.getAccessToken(systemUser);
        if(accessToken != null && !accessToken.isEmpty()){
            map.put(EmployeeDirectorConstants.HEADER_KEY_AUTH, EmployeeDirectorConstants.AUTH_SCHEME + EmployeeDirectorConstants.SEPARATOR + accessToken);
        }

        headers.setAll(map);
        HttpEntity<?> request = new HttpEntity<>(employee,headers);
        final long startTime = System.currentTimeMillis();
        try{
            ResponseEntity<Employee> employeeRes = restTemplate.exchange(url, HttpMethod.PATCH, request, Employee.class);
            Employee_Management_Director_Logger.info("Employee has been changed with PATCH operation PID : {}", username);
            if(employeeRes !=null ){
                Employee employeeCreated = employeeRes.getBody();
                final long endTime = System.currentTimeMillis();
                externalSystemMetrics.createExternalMetrics(createExternalMetricsData(endTime-startTime, EmployeeDirectorConstants.EMPLOYEE_MANAGER, employeeRes.getStatusCode().toString(), uriAnonymizer.maskIdentifier(url), HttpMethod.PATCH.toString()));
                return employeeCreated;
            }
        }catch (Exception e){
            if(e instanceof HttpClientErrorException){
                HttpStatus httpStatus = ((HttpClientErrorException) e).getStatusCode();
                refreshAccessTokenIfUnauthorized(httpStatus);
                throw new EmployeeDirectorException(HttpStatus.INTERNAL_SERVER_ERROR.toString(), e.getMessage(), e, url);
            }else if(e instanceof ResourceAccessException){
                throw new EmployeeDirectorException(HttpStatus.INTERNAL_SERVER_ERROR.toString(), e.getMessage(), e, url);
            }else{
                throw new EmployeeDirectorException(HttpStatus.INTERNAL_SERVER_ERROR.toString(), e.getMessage(), e,url);
            }
        }
        return null;
    }

    @Override
    public KeycloakUser getSystemUser(String username) throws EmployeeDirectorException {

        final String url = keycloakUserBasePath + "/" +username;

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        Map<String,String> map = new HashMap<>();

        map.put(EmployeeDirectorConstants.HEADER_KEY_AUTH, EmployeeDirectorConstants.AUTH_SCHEME + EmployeeDirectorConstants.SEPARATOR);


        headers.setAll(map);
        HttpEntity<?> request = new HttpEntity<>(headers);
        final long startTime = System.currentTimeMillis();
        ResponseEntity<KeycloakUser> response = null;
        try{
            response = restTemplate.exchange(url, HttpMethod.GET, request, KeycloakUser.class);
            if(response !=null ){
                KeycloakUser keycloakUser = response.getBody();
                Employee_Management_Director_Logger.info("Get Employee returned the result : {} ", keycloakUser.toString());
                final long endTime = System.currentTimeMillis();
                externalSystemMetrics.createExternalMetrics(createExternalMetricsData(endTime-startTime, EmployeeDirectorConstants.EMPLOYEE_MANAGER, response.getStatusCode().toString(), uriAnonymizer.maskIdentifier(url), HttpMethod.GET.toString()));
                return keycloakUser;
            }
        }catch (Exception e){
            if(e instanceof HttpClientErrorException){
                HttpStatus httpStatus = ((HttpClientErrorException) e).getStatusCode();
                refreshAccessTokenIfUnauthorized(httpStatus);
                throw new EmployeeDirectorException(HttpStatus.INTERNAL_SERVER_ERROR.toString(),e.getMessage(),e,request.toString());
            }else if(e instanceof ResourceAccessException){
                throw new EmployeeDirectorException(HttpStatus.INTERNAL_SERVER_ERROR.toString(),e.getMessage(),e,request.toString());
            }else{
                throw new EmployeeDirectorException(HttpStatus.INTERNAL_SERVER_ERROR.toString(),e.getMessage(),e,request.toString());
            }
        }

        return null;
    }

    @Override
    public List<Employee> getAllEmployee(KeycloakUser keycloakUser) {

        final String url = employeeManagerBasePath;

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        Map<String,String> map = new HashMap<>();
        String accessToken = systemTokenStore.getAccessToken(keycloakUser);
        if(accessToken != null && !accessToken.isEmpty()){
            map.put(EmployeeDirectorConstants.HEADER_KEY_AUTH, EmployeeDirectorConstants.AUTH_SCHEME + EmployeeDirectorConstants.SEPARATOR + accessToken);
        }

        headers.setAll(map);
        HttpEntity<?> request = new HttpEntity<>(headers);
        final long startTime = System.currentTimeMillis();
        ResponseEntity<EmployeeList> response = null;
        try{
            response = restTemplate.exchange(url, HttpMethod.GET, request, EmployeeList.class);
            if(response !=null ){
                EmployeeList employees = response.getBody();
                Employee_Management_Director_Logger.info("Get Employee returned the result : {} ", employees.toString());
                final long endTime = System.currentTimeMillis();
                externalSystemMetrics.createExternalMetrics(createExternalMetricsData(endTime-startTime, EmployeeDirectorConstants.EMPLOYEE_MANAGER, response.getStatusCode().toString(), uriAnonymizer.maskIdentifier(url), HttpMethod.GET.toString()));
                return employees.getEmployees();
            }
        }catch (Exception e){
            if(e instanceof HttpClientErrorException){
                HttpStatus httpStatus = ((HttpClientErrorException) e).getStatusCode();
                refreshAccessTokenIfUnauthorized(httpStatus);
                throw new EmployeeDirectorException(HttpStatus.INTERNAL_SERVER_ERROR.toString(),e.getMessage(),e,request.toString());
            }else if(e instanceof ResourceAccessException){
                throw new EmployeeDirectorException(HttpStatus.INTERNAL_SERVER_ERROR.toString(),e.getMessage(),e,request.toString());
            }else{
                throw new EmployeeDirectorException(HttpStatus.INTERNAL_SERVER_ERROR.toString(),e.getMessage(),e,request.toString());
            }
        }

        return null;
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
