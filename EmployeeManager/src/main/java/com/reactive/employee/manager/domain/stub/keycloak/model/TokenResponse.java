package com.reactive.employee.manager.domain.stub.keycloak.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class TokenResponse {

    @JsonProperty("access_token")
    private String access_token;

    @JsonProperty("refresh_token")
    private String refresh_token;

    @JsonProperty("token_type")
    private String token_type;

    @JsonProperty("session_state")
    private String session_state;

    @JsonProperty("expires_in")
    private Integer expires_in;

    @JsonProperty("refresh_expires_in")
    private Integer refresh_expires_in;

    @JsonProperty("not_before_policy")
    private Integer not_before_policy;

}
