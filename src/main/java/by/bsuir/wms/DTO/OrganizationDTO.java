package by.bsuir.wms.DTO;

import by.bsuir.wms.Entity.Organization;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationDTO {
    private String name;
    private String inn;
    private String address;

    public OrganizationDTO(Organization organization) {
        this.name = organization.getName();
        this.inn = organization.getINN();
        this.address = organization.getAddress();
    }
}
