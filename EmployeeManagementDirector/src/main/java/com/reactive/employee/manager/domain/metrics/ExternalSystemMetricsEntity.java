package com.reactive.employee.manager.domain.metrics;

import lombok.Data;

@Data
public class ExternalSystemMetricsEntity {
    
    private Long elapsedTime;
    private String uri;
    private String statusCode;
    private String externalSystem;
    private String httpMethod;
}
