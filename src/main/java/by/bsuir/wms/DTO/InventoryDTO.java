package by.bsuir.wms.DTO;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InventoryDTO {

    private List<Integer> ids;
    private List<Integer> amounts;
}
