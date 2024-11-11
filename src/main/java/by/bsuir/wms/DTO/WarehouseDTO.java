package by.bsuir.wms.DTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseDTO {
    private String name;
    private String address;
    private Integer capacity;
}
