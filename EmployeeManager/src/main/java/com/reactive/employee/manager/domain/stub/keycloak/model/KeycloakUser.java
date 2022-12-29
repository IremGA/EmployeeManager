package com.reactive.employee.manager.domain.stub.keycloak.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Document
public class KeycloakUser {

    @Id
    @NotNull
    @JsonProperty("username")
    private String username;

    @JsonProperty("firstName")
    private String firstName;

    @JsonProperty("lastName")
    private String lastName;

    @JsonProperty("groups")
    private List<String> groups;

    @JsonProperty("credentials")
    private List<CredentialReporesentation> credentials;

    @JsonProperty("email")
    private String email;

    @JsonProperty("enabled")
    private Boolean enabled;

    @JsonProperty("client_id")
    private String client_id;

    @JsonProperty("lastAction")
    private String lastAction;

    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @JsonProperty("lastActionDate")
    private Date lastActionDate;
}
