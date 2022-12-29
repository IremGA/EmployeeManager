package com.reactive.employee.manager.domain.stub.keycloak.repository;

import com.reactive.employee.manager.domain.stub.keycloak.model.KeycloakUser;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface KeycloakStubRepository
        extends ReactiveMongoRepository<KeycloakUser, String> {
}
