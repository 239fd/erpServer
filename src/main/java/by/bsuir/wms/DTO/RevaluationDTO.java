package by.bsuir.wms.DTO;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevaluationDTO {
    private List<Integer> productIds;
    private List<Double> newPrice;
}
