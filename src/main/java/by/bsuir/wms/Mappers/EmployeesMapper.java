package by.bsuir.wms.Mappers;

import by.bsuir.wms.DTO.EmployeesDTO;
import by.bsuir.wms.DTO.SignUpDTO;
import by.bsuir.wms.Entity.Employees;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EmployeesMapper {

    EmployeesDTO toEmployeesDTO(Employees employees);

    @Mapping(target = "password", ignore = true)
    Employees signUpToEmployee(SignUpDTO signUpDTO);
}
