
package com.reactive.employee.manager.domain.utility;

import com.reactive.employee.manager.domain.constants.EmployeeDirectorConstants;
import com.reactive.employee.manager.domain.exception.EmployeeDirectorException;
import com.reactive.employee.manager.domain.stub.keycloak.model.KeycloakUser;
import com.reactive.employee.manager.domain.stub.keycloak.model.TokenResponse;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * This is a utility class which is used to fetch system Token form IAM service.
 * Following properties are to be added in application.properties file of each service that uses this utility. <br>
 * iam.url = API URL to fetch the token <br>
 * iam.client.id = client-id required to fetch the token <br>
 * iam.grant.type = grant type required to fetch the token <br>
 * iam.client.secret.path = path where the client secret mounted
 */

@Component("systemTokenStore")
public class SystemTokenStore {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${iam.token.url}")
    private String iamURL;

    @Setter
    private String accessToken = null;
    
    private String refreshToken = null;
    
    private static final String ACCESS_TOKEN_FAIL_MSG = "Failed to get access token: ";
    
    private AtomicBoolean isTokenValid = new AtomicBoolean(true);

    public static final Logger Employee_Director_Logger = LoggerFactory.getLogger(SystemTokenStore.class);
    
    public String getAccessToken(KeycloakUser keycloakUser) throws EmployeeDirectorException{
        final String url = iamURL;

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        Map<String,String> map = new HashMap<>();
        map.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        map.put(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
            map.put(EmployeeDirectorConstants.HEADER_KEY_AUTH, EmployeeDirectorConstants.AUTH_SCHEME + EmployeeDirectorConstants.SEPARATOR );


        headers.setAll(map);

        HttpEntity<?> request = new HttpEntity<>(keycloakUser, headers);
        final long startTime = System.currentTimeMillis();
        try{
            ResponseEntity<TokenResponse> tokenResponse = restTemplate.postForEntity(url, request, TokenResponse.class);
            if(tokenResponse !=null ){
                TokenResponse tokenResponsed = tokenResponse.getBody();
                Employee_Director_Logger.info("Token has been created  Token : {}", tokenResponsed.getAccess_token());
                final long endTime = System.currentTimeMillis();
                return tokenResponsed.getAccess_token();
            }
        }catch (Exception e){
            if(e instanceof HttpClientErrorException){
                String message = "Token cannot be created. InValidUser for the action.." + keycloakUser.getUsername() + " Action : "+ keycloakUser.getClient_id();
                Employee_Director_Logger.info(message);
                throw new EmployeeDirectorException(HttpStatus.INTERNAL_SERVER_ERROR.toString(), message, e,  url);
            }else if(e instanceof ResourceAccessException){
                throw new EmployeeDirectorException(HttpStatus.INTERNAL_SERVER_ERROR.toString(), e.getMessage(), e, url);
            }else{
                throw new EmployeeDirectorException(HttpStatus.INTERNAL_SERVER_ERROR.toString(), e.getMessage(), e, url);
            }
        }
        return null;
    }


    
}

