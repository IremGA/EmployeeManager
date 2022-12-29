package com.reactive.employee.manager.domain.stub.keycloak.router;

import com.reactive.employee.manager.domain.stub.keycloak.handler.KeycloakHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class KeycloakRouter {

    @Bean
    RouterFunction<ServerResponse> routeKeycloakUser(KeycloakHandler handler) {
        return route()
                .path("/realm_hr_stub/users",
                        builder -> builder
                                .nest(accept(APPLICATION_JSON).or(contentType(APPLICATION_JSON)).or(accept(TEXT_EVENT_STREAM)),
                                        nestedBuilder -> nestedBuilder
                                                .GET("/{username}", handler::getKeycloakUser)
                                )
                                .DELETE("/{username}", handler::deleteKeycloakUser)
                ).build();
    }
}
