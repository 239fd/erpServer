package by.wms.server.DTO;

import by.wms.server.Entity.Enum.Title;
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
    private Title title;
    private String firstName;
    private String secondName;
    private String surname;
    private String organizationId;
}
