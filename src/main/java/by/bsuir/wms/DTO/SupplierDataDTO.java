package by.bsuir.wms.DTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierDataDTO {

    private int id;
    private String inn;
    private String name;
    private String address;
}
