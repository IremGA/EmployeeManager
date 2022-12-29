package com.reactive.employee.manager.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Document
public class Role {
    @Id
    @NotNull
    @JsonProperty("id")
    private String id;

    @JsonProperty("value")
    private String value;
}
