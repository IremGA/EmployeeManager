package com.reactive.employee.manager.domain.stub.keycloak.service;

import com.reactive.employee.manager.domain.constants.EmployeeManagerConstants;
import com.reactive.employee.manager.domain.exception.EmployeeManagerException;
import com.reactive.employee.manager.domain.stub.configManager.model.ConfigManager;
import com.reactive.employee.manager.domain.stub.configManager.model.ConfigManagerMap;
import com.reactive.employee.manager.domain.stub.keycloak.model.KeycloakUser;
import com.reactive.employee.manager.domain.stub.keycloak.model.TokenResponse;
import com.google.gson.Gson;
import org.springframework.cache.CacheManager;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Service
public class KeycloakService {
    private ConfigManager configManager;
    private CacheManager cacheManager;

    public KeycloakService(CacheManager cacheManager){
        this.cacheManager = cacheManager;
    }

    private String configMapPath = "/data/hr/ConfigMapActionList.json";

    @PostConstruct
    public void init(){

        configManager = returnConfigMap(configMapPath);

    }

    private String path = "/data/hr/Valid_Token_Response.json";

    //TODO Validate User Action with defined actionList
    public Mono<ServerResponse> validateKeyCloakUser(KeycloakUser keycloakUser){

        List<String> groups = keycloakUser.getGroups();

        if(groups.contains(EmployeeManagerConstants.HR_ADMIN_GROUP_NAME)){
            return validateActionForGroup(EmployeeManagerConstants.HR_ADMIN_GROUP_NAME, keycloakUser);
        }else if(groups.contains(EmployeeManagerConstants.MANAGER_GROUP_NAME)){
            return validateActionForGroup(EmployeeManagerConstants.MANAGER_GROUP_NAME, keycloakUser);
        }else if(groups.contains(EmployeeManagerConstants.EMPLOYEE_GROUP_NAME)){
            return validateActionForGroup(EmployeeManagerConstants.EMPLOYEE_GROUP_NAME, keycloakUser);
        }else{
            return validateActionForGroup(EmployeeManagerConstants.DEFAULT_GROUP_NAME, keycloakUser);
        }

    }

    private Mono<ServerResponse> validateActionForGroup(String groupName, KeycloakUser keycloakUser) {

        TokenResponse validTokenResponse = returnStubTokenResponse();

        EmployeeManagerException employeeManagerException = new EmployeeManagerException();
        employeeManagerException.setCode(HttpStatus.FORBIDDEN.toString());
        employeeManagerException.setReason("Not authorized to make change on Employee for user : " + keycloakUser.getUsername() + ". Token cannot be generated");

        List<ConfigManagerMap> actionList = configManager.getConfigManagerMaps()
                .stream()
                .filter(s->s.getGroupName().equals(groupName))
                .collect(Collectors.toList());

        if(actionList != null && actionList.size() != 0 && actionList.get(0).getActionList().contains(keycloakUser.getClient_id())){
            return ServerResponse.status(HttpStatus.OK)
                    .contentType(APPLICATION_JSON)
                    .body(Mono.just(validTokenResponse), TokenResponse.class);
        }else{
            return ServerResponse.status(HttpStatus.FORBIDDEN)
                    .contentType(APPLICATION_JSON)
                    .body(Mono.just(employeeManagerException), EmployeeManagerException.class);
        }
    }

    private TokenResponse returnStubTokenResponse(){
        try{

            Gson gson = new Gson();
            InputStream resource = new ClassPathResource(path).getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(resource));
            TokenResponse tokenResponse = gson.fromJson(reader, TokenResponse.class);
            return tokenResponse;
        } catch (IOException e) {
            return null;
        }
    }

    private ConfigManager returnConfigMap(String configMapPath){
        try{

            Gson gson = new Gson();
            InputStream resource = new ClassPathResource(configMapPath).getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(resource));
            ConfigManager configManager1 = gson.fromJson(reader, ConfigManager.class);
            return configManager1;
        } catch (IOException e) {
            return null;
        }
    }
}
