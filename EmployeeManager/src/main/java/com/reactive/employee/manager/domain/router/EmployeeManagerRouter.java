package com.reactive.employee.manager.domain.router;

import com.reactive.employee.manager.domain.handler.EmployeeManagerHandler;
import com.reactive.employee.manager.domain.model.Employee;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class EmployeeManagerRouter {

    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            method = RequestMethod.GET,
                            path = "/employee/{id}",
                            operation =
                            @Operation(
                                    description = "Get employee by id common router",
                                    operationId = "getEmployee",
                                    tags = "employee",
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "Get employee by id response",
                                                    content = {
                                                            @Content(
                                                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                                                    schema = @Schema(implementation = Employee.class))
                                                    }),
                                            @ApiResponse(responseCode = "404", description = "Employee not found")
                                    },
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "id")}
                            )),
                    @RouterOperation(
                            method = RequestMethod.POST,
                            path = "/employee",
                            operation =
                            @Operation(
                                    description = "POST employee common router",
                                    operationId = "saveEmployee",
                                    tags = "employee",
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "201",
                                                    description = "CREATE employee",
                                                    content = {
                                                            @Content(
                                                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                                                    schema = @Schema(implementation = Employee.class))
                                                    }),
                                            @ApiResponse(responseCode = "404", description = "Employee not found")
                                    })),
                    @RouterOperation(
                            method = RequestMethod.PATCH,
                            path = "/employee/{id}",
                            operation =
                            @Operation(
                                    description = "PATCH employee common router",
                                    operationId = "patchEmployee",
                                    tags = "employee",
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "PATCH employee by id response",
                                                    content = {
                                                            @Content(
                                                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                                                    schema = @Schema(implementation = Employee.class))
                                                    }),
                                            @ApiResponse(responseCode = "404", description = "Employee not found")
                                    },
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "id")}
                            )),
                    @RouterOperation(
                            method = RequestMethod.DELETE,
                            path = "/employee/{id}",
                            operation =
                            @Operation(
                                    description = "PATCH employee common router",
                                    operationId = "deleteEmployee",
                                    tags = "employee",
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "204",
                                                    description = "DELETE employee by id response"
                                                    ),
                                            @ApiResponse(responseCode = "404", description = "Employee not found")
                                    },
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "id")}
                            ))
            })
    RouterFunction<ServerResponse> routeEmployee(EmployeeManagerHandler handler) {
        return route()
                .path("/employee",
                        builder -> builder
                                .nest(accept(APPLICATION_JSON).or(contentType(APPLICATION_JSON)).or(accept(TEXT_EVENT_STREAM)),
                                        nestedBuilder -> nestedBuilder
                                                .GET("/{username}", handler::getEmployee)
                                                .PUT("/{username}", handler::updateEmployee)
                                                .PATCH("/{username}", handler::patchEmployee)
                                                .POST("/getByCriteria", handler::getAllEmployees)
                                                .GET(handler::getAllEmployees)
                                                .POST(handler::saveEmployee)
                                )
                                .DELETE("/{username}", handler::deleteEmployee)
                                .DELETE(handler::deleteAllEmployees)
                ).build();
    }
}
