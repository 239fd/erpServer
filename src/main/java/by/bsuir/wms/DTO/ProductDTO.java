package by.bsuir.wms.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date bestBeforeDate;
    private Integer amount;

    public Integer getId() {
        return 0;
    }
}
