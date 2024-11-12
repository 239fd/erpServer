package by.bsuir.wms.DTO;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DispatchDTO {

    private List<Integer> productIds;
    private List<Integer> amounts;
    private String vehicle;
    private String driverName;
    private String deliveryAddress;
    private String organizationName;
    private String organizationAddress;
    private String customerName;
    private String customerAddress;
    private String documentNumber;
    private String documentDate;
}