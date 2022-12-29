package com.reactive.employee.manager.domain.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Document
public class Vacation {

    @Id
    @NotNull
    @JsonProperty("id")
    private String id;

    @JsonProperty("requester")
    private String requester;

    @JsonProperty("reason")
    private String reason;

    @JsonProperty("vacationDay")
    private VacationDay vacationDay;

    @JsonProperty("daysUsed")
    private Float daysUsed;

    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @JsonProperty("requestedDate")
    private Date requestedDate;

    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @JsonProperty("approveDate")
    private Date approveDate;

    @JsonProperty("approveRejectReason")
    private String approveRejectReason;

    @JsonProperty("status")
    private String status;


}
