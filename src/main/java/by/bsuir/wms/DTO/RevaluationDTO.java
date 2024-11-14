package by.bsuir.wms.DTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevaluationDTO {
    private int productId;
    private double newPrice;
}
