package by.bsuir.wms.DTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CellDTO {
    private Double length;
    private Double width;
    private Double height;
}
