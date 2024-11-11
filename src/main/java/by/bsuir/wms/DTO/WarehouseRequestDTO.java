package by.bsuir.wms.DTO;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseRequestDTO {
    private String name;
    private String address;
    private List<RackDTO> racks;
}


