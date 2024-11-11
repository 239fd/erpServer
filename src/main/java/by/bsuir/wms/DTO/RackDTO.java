package by.bsuir.wms.DTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RackDTO {
    private Integer capacity;
    private Integer cellCount;
    private Double cellHeight;
    private Double cellWidth;
    private Double cellLength;
}

