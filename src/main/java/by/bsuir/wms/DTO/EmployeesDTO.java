package by.wms.server.DTO;

import by.wms.server.Entity.Enum.Title;
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
    private Title title;
    private String organizationId;
    private String token;
}
