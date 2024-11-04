package by.wms.server.Mappers;

import by.wms.server.DTO.CredentialsDTO;
import by.wms.server.DTO.EmployeesDTO;
import by.wms.server.DTO.SignUpDTO;
import by.wms.server.Entity.Employees;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EmployeesMapper {

    EmployeesDTO toEmployeesDTO(Employees employees);

    @Mapping(target = "password", ignore = true)
    Employees signUpToEmployee(SignUpDTO signUpDTO);
}
