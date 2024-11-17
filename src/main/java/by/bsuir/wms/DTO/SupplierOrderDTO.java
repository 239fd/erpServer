package by.bsuir.wms.DTO;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class SupplierOrderDTO {

    private final SupplierDataDTO supplier;
    private final String productName;
    private final int quantity;

}
