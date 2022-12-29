package com.reactive.employee.manager.domain.stub.keycloak.handler;

import com.reactive.employee.manager.domain.exception.ApplicationErrorHandler;
import com.reactive.employee.manager.domain.exception.EmployeeManagerException;
import com.reactive.employee.manager.domain.stub.keycloak.model.KeycloakUser;
import com.reactive.employee.manager.domain.stub.keycloak.repository.KeycloakStubRepository;
import com.reactive.employee.manager.domain.stub.keycloak.service.KeycloakService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@Component
public class KeycloakHandler {

    @Autowired
    KeycloakStubRepository keycloakStubRepository;

    @Autowired
    KeycloakService keycloakService;

    @Autowired
    private ApplicationErrorHandler applicationErrorHandler;

    public Mono<ServerResponse> getKeycloakUser(ServerRequest request) {
        String id = request.pathVariable("username");
        try{
            Mono<ServerResponse> notFound = ServerResponse.notFound().build();

            Mono<KeycloakUser> keycloakUserMono = keycloakStubRepository.findById(id);

            return keycloakUserMono
                    .flatMap(user ->
                            ServerResponse.ok()
                                    .contentType(APPLICATION_JSON)
                                    .body(fromValue(user)))
                    .switchIfEmpty(notFound);
        }catch (EmployeeManagerException e){

            return applicationErrorHandler.handleDownStreamError(e);
        }
    }

    public Mono<ServerResponse> requestForToken(ServerRequest request) {

        try{
            //TODO Check the user is both in Valid Group for requested Action
            Mono<KeycloakUser> keycloakUserMono = request.bodyToMono(KeycloakUser.class);

            return keycloakUserMono
                    .flatMap(keycloakUser -> {
                            return  keycloakService.validateKeyCloakUser(keycloakUser);
                    });
        }catch (EmployeeManagerException e){

            return applicationErrorHandler.handleDownStreamError(e);
        }
    }


    public Mono<ServerResponse> deleteKeycloakUser(ServerRequest request) {
        String id = request.pathVariable("username");
        Mono<ServerResponse> status_ok = ServerResponse.noContent().build();
        try{
            keycloakStubRepository.deleteById(id).subscribe();
            return status_ok;
        }catch (EmployeeManagerException e){
            return applicationErrorHandler.handleDownStreamError(e);
        }
    }


}
