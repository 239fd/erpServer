package by.bsuir.wms.DTO;

import by.bsuir.wms.Entity.Enum.Role;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeesDTO {

    private int id;
    private String login;
    private String firstName;
    private String password;
    private String phone;
    private String secondName;
    private String surname;
    private Role role;
    private String organizationId;
    private String token;
}
