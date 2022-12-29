package com.reactive.employee.manager.domain.stub.configManager.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ConfigManager {
    List<ConfigManagerMap> configManagerMaps;
}
