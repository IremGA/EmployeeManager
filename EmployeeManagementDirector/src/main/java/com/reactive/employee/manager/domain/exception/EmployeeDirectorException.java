package com.reactive.employee.manager.domain.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDirectorException extends RuntimeException {

    private static final long serialVersionUID = -5155234678817491729L;
    private String code;
    
    private String reason;
    
    private Throwable cause;
    
    private String url;
    
}
