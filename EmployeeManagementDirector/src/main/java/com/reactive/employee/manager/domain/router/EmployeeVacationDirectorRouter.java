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
public class EmployeeVacationDirectorRouter {

    @Bean
    RouterFunction<ServerResponse> routeVacation(EmployeeDirectorHandler handler) {
        return route()
                .path("/vacation",
                        builder -> builder
                                .nest(accept(APPLICATION_JSON).or(contentType(APPLICATION_JSON)).or(accept(TEXT_EVENT_STREAM)),
                                        nestedBuilder -> nestedBuilder
                                                .POST("/new/{username}", handler::createVacationRequest)
                                                .POST("/approve/{username}",handler::approveVacationRequest)
                                                .POST("/reject/{username}", handler::rejectVacationRequest)
                                                .GET("/approval/{username}", handler::getApprovalList)
                                                .GET("/{username}", handler::getUserVacation)
                                                .GET("/approvalList/{systemUser}", handler::getAllApprovalList)
                                )
                ).build();
    }
}
