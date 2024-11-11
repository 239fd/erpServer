package by.bsuir.wms.DTO;

import lombok.*;

import java.sql.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {
    private String name;
    private String unit;
    private Double price;
    private Double height;
    private Double length;
    private Double width;
    private Double weight;
    private Date bestBeforeDate;
    private Integer amount;
}
