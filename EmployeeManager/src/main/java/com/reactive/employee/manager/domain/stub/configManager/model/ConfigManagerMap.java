package com.reactive.employee.manager.domain.stub.configManager.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Document
public class ConfigManagerMap {
    @Id
    @JsonProperty("groupName")
    private String groupName;

    @JsonProperty("actionList")
    private List<String> actionList;
}
