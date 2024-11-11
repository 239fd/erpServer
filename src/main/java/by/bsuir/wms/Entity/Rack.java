package by.bsuir.wms.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "rack")
public class Rack {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "capacity")
    private Integer capacity;

    @OneToMany(mappedBy = "rack", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Cell> cells;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;
}