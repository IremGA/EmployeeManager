package com.reactive.employee.manager.domain.mapstruct;

import com.reactive.employee.manager.domain.model.Employee;
import com.reactive.employee.manager.domain.stub.keycloak.model.KeycloakUser;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, componentModel = "spring")
public interface EmployeeMapper {
    EmployeeMapper INSTANCE = Mappers.getMapper(EmployeeMapper.class);
    Employee patchEmployeeToExistingEmployee(Employee patchEmployee, @MappingTarget Employee existingEmployee);

    @Mappings({
            @Mapping(target="username", source="employee.username"),
            @Mapping(target="firstName", source="employee.name"),
            @Mapping(target="lastName", source="employee.surname")
    })
    KeycloakUser mapEmployeetoKeycloakUser(Employee employee);
}
