package com.reactive.employee.manager.domain.handler;

import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EmployeeDirectorHandler {
    Mono<ServerResponse> createVacationRequest(ServerRequest serverRequest);

    Mono<ServerResponse> approveVacationRequest(ServerRequest serverRequest);

    Mono<ServerResponse> rejectVacationRequest(ServerRequest serverRequest);

    Mono<ServerResponse> getApprovalList(ServerRequest serverRequest);

    Mono<ServerResponse> getUserVacation(ServerRequest serverRequest);

    Mono<ServerResponse> getEmployee(ServerRequest serverRequest);

    Mono<ServerResponse> patchEmployee(ServerRequest serverRequest);

    Mono<ServerResponse> saveEmployee(ServerRequest serverRequest);

    Mono<ServerResponse> deleteEmployee(ServerRequest serverRequest);

    Mono<ServerResponse> getAllApprovalList(ServerRequest serverRequest);
}
