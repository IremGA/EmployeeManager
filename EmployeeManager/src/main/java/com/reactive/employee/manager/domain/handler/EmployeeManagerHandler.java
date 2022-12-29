package com.reactive.employee.manager.domain.handler;

import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public interface EmployeeManagerHandler {

    Mono<ServerResponse> getAllEmployees(ServerRequest request);
    Mono<ServerResponse> getEmployee(ServerRequest request);
    Mono<ServerResponse> saveEmployee(ServerRequest request);
    Mono<ServerResponse> updateEmployee(ServerRequest request);
    Mono<ServerResponse> deleteEmployee(ServerRequest request);

    Mono<ServerResponse> deleteAllEmployees(ServerRequest request);
    Mono<ServerResponse> patchEmployee(ServerRequest request);
}
