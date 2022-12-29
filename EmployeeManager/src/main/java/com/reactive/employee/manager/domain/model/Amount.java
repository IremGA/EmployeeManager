package com.reactive.employee.manager.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Document
public class Amount {
  
  @JsonProperty("unit")
  private String unit = null;

  @JsonProperty("value")
  private Float value = null;

  public Amount unit(String unit) {
    this.unit = unit;
    return this;
  }
  
}

