package com.reactive.employee.manager.domain.router;

import com.reactive.employee.manager.domain.handler.EmployeeDirectorHandler;
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
public class EmployeeDirectorRouter {

    @Bean
    RouterFunction<ServerResponse> routeEmployee(EmployeeDirectorHandler handler) {
        return route()
                .path("/employee",
                        builder -> builder
                                .nest(accept(APPLICATION_JSON).or(contentType(APPLICATION_JSON)).or(accept(TEXT_EVENT_STREAM)),
                                        nestedBuilder -> nestedBuilder
                                                .GET("/{systemUser}/{username}", handler::getEmployee)
                                                .PATCH("/{systemUser}/{username}", handler::patchEmployee)
                                                .POST("/{systemUser}",handler::saveEmployee)
                                )
                                .DELETE("/{systemUser}/{username}", handler::deleteEmployee)
                ).build();
    }
}
