package by.bsuir.wms.Entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "cell")
public class Cell {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "length")
    private Double length;

    @Column(name = "width")
    private Double width;

    @Column(name = "height")
    private Double height;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rack_id")
    private Rack rack;

    @ManyToMany(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(
            name = "cell_has_products",
            joinColumns = { @JoinColumn(name = "cell_id") },
            inverseJoinColumns = { @JoinColumn(name = "products_id")}
    )
    @JsonManagedReference
    private Set<Product> products;

}