package by.bsuir.wms.DTO;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WriteOffDTO {
    private List<Integer> productId;
    private List<Integer> quantity;
    private String reason;
    private String date;
}
