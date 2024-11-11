package by.bsuir.wms.DTO;

import by.bsuir.wms.Entity.Enum.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CredentialsDTO {

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
