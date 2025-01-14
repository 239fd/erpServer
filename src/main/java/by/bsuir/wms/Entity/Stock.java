package by.bsuir.wms.Entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "stocks")
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "amount", nullable = false)
    private Integer amount;

    private Double price;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "suppliers_id")
    private Supplier supplier;

}