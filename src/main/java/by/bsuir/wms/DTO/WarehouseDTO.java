package by.bsuir.wms.DTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseDTO {
    private String id;
    private String name;
    private String address;
}
