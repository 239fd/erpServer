package by.bsuir.wms.Controller;

import by.bsuir.wms.API.ApiResponse;
import by.bsuir.wms.DTO.CredentialsDTO;
import by.bsuir.wms.DTO.EmployeesDTO;
import by.bsuir.wms.DTO.SignUpDTO;
import by.bsuir.wms.Service.EmployeesService;
import by.bsuir.wms.Service.RouteService;
import by.bsuir.wms.config.UserAuthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(maxAge = 3600L)
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AuthController {

    private final EmployeesService employeesService;
    private final UserAuthProvider userAuthProvider;
    private final RouteService routeService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<EmployeesDTO>> login(@RequestBody CredentialsDTO credentialsDTO){
        EmployeesDTO employeesDTO = employeesService.login(credentialsDTO);

        employeesDTO.setToken(userAuthProvider.createToken(employeesDTO.getLogin()));
        ApiResponse<EmployeesDTO> response = ApiResponse.<EmployeesDTO>builder()
                .data(employeesDTO)
                .status(true)
                .message("User successfully logged in")
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<EmployeesDTO>> register(@RequestBody SignUpDTO signUpDTO) {
        EmployeesDTO employeesDTO = employeesService.register(signUpDTO);
        employeesDTO.setToken(userAuthProvider.createToken(signUpDTO.getLogin()));
        ApiResponse<EmployeesDTO> response = ApiResponse.<EmployeesDTO>builder()
                .data(employeesDTO)
                .status(true)
                .message("User successfully registered in")
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/registerdirector")
    public ResponseEntity<ApiResponse<EmployeesDTO>> registerDirector(@RequestBody SignUpDTO signUpDTO) {
        EmployeesDTO employeesDTO = employeesService.registerDirector(signUpDTO);
        employeesDTO.setToken(userAuthProvider.createToken(signUpDTO.getLogin()));
        ApiResponse<EmployeesDTO> response = ApiResponse.<EmployeesDTO>builder()
                .data(employeesDTO)
                .status(true)
                .message("User successfully registered in as director")
                .build();
        return ResponseEntity.ok(response);
    }
}
