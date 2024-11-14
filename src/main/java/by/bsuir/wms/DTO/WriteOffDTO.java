package by.bsuir.wms.DTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WriteOffDTO {
    private int productId;
    private int quantity;
    private String reason;
    private String date;
}
