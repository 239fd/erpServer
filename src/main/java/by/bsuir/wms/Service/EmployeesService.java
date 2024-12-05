package by.bsuir.wms.Service;

import by.bsuir.wms.DTO.CredentialsDTO;
import by.bsuir.wms.DTO.EmployeesDTO;
import by.bsuir.wms.DTO.SignUpDTO;
import by.bsuir.wms.Entity.Employees;
import by.bsuir.wms.Entity.Organization;
import by.bsuir.wms.Exception.AppException;
import by.bsuir.wms.Mappers.EmployeesMapper;
import by.bsuir.wms.Repository.EmployeesRepository;
import by.bsuir.wms.Repository.OrganizationRepository;
import by.bsuir.wms.Repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.CharBuffer;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmployeesService {

    private final EmployeesRepository employeesRepository;
    private final EmployeesMapper employeesMapper;
    private final PasswordEncoder passwordEncoder;
    private final OrganizationRepository organizationRepository;
    private final WarehouseRepository warehouseRepository;

    public EmployeesDTO findByLogin(String login) {
        Employees employee = employeesRepository.findByLogin(login)
                .orElseThrow(()-> new AppException("Unknown user", HttpStatus.NOT_FOUND));
        return employeesMapper.toEmployeesDTO(employee);
    }
    public Employees findEmployeeByLogin(String login) {
        return employeesRepository.findByLogin(login)
                .orElseThrow(() -> new AppException("Unknown user", HttpStatus.NOT_FOUND));
    }


    public EmployeesDTO login(CredentialsDTO credentials) {
        Employees employee = employeesRepository.findByLogin(credentials.getLogin())
                .orElseThrow(()-> new AppException("Unknown user", HttpStatus.NOT_FOUND));

        if(passwordEncoder.matches(CharBuffer.wrap(credentials.getPassword()), employee.getPassword())){
            return EmployeesDTO.builder()
                    .id(employee.getId())
                    .login(employee.getLogin())
                    .role(employee.getRole())
                    .build();
        }

        throw new AppException("Wrong password or login", HttpStatus.UNAUTHORIZED);
    }

    public EmployeesDTO register(SignUpDTO signUpDTO) {
        checkForCorrectInput(signUpDTO);
        Employees employees = employeesMapper.signUpToEmployee(signUpDTO);

        Organization organization = organizationRepository.getOrganizationByINN(signUpDTO.getOrganizationId().substring(0, 9))
                .orElseThrow(() -> new AppException("Организация не найдена", HttpStatus.CONFLICT));

        if(warehouseRepository.getWarehouseById(Integer.valueOf((signUpDTO.getOrganizationId()).substring(9))) == null){
            throw new AppException("Нет складов с таким номером", HttpStatus.CONFLICT);
        }


        employees.setWarehouse(warehouseRepository.getWarehouseById(Integer.valueOf((signUpDTO.getOrganizationId()).substring(9))));
        employees.setOrganization(organization);
        employees.setPassword(passwordEncoder.encode(CharBuffer.wrap(signUpDTO.getPassword())));

        employees = employeesRepository.save(employees);

        return EmployeesDTO.builder()
                .id(employees.getId())
                .login(employees.getLogin())
                .role(employees.getRole())
                .build();
    }

    public EmployeesDTO registerDirector(SignUpDTO signUpDTO) {
        checkForCorrectInput(signUpDTO);
        Employees employees = employeesMapper.signUpToEmployee(signUpDTO);

        employees.setPassword(passwordEncoder.encode(CharBuffer.wrap(signUpDTO.getPassword())));

        employees = employeesRepository.save(employees);

        return EmployeesDTO.builder()
                .id(employees.getId())
                .login(employees.getLogin())
                .role(employees.getRole())
                .build();
    }

    private void checkForCorrectInput(SignUpDTO signUpDTO) {
        Optional<Employees> optionalEmployees = employeesRepository.findByLogin(signUpDTO.getLogin());

        if (optionalEmployees.isPresent()) {
            throw new AppException("Login already exists", HttpStatus.CONFLICT);
        }
        if(signUpDTO.getRole() == null || signUpDTO.getFirstName() == null|| signUpDTO.getPhone() == null || signUpDTO.getSecondName() == null) {
            throw new AppException("Not enough information", HttpStatus.CONFLICT);
        }
    }

}
