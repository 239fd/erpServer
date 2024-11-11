package by.bsuir.wms.DTO;

import by.bsuir.wms.Entity.Enum.Role;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class SignUpDTO {

    private int id;
    private String login;
    private String password;
    private String phone;
    private Role role;
    private String firstName;
    private String secondName;
    private String surname;
    private String organizationId;
}
