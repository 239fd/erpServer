package by.bsuir.wms.Entity;

import by.bsuir.wms.Entity.Enum.Status;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Date;
import java.util.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "height")
    private Double height;

    @Column(name = "length")
    private Double length;

    @Column(name = "name")
    private String name;

    @Column(name = "price")
    private Double price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;

    @Column(name = "unit")
    private String unit;

    @Column(name = "width")
    private Double width;

    @Column(name = "weight")
    private Double weight;

    @Column(name = "bestbeforedate")
    private Date bestBeforeDate;

    @Column(name = "amount")
    private Integer amount;

    @ManyToMany(mappedBy = "products")
    @JsonBackReference
    private Set<Cell> cells = new HashSet<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductSalesHistory> salesHistory;
}