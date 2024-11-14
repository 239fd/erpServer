package by.bsuir.wms.Repository;

import by.bsuir.wms.Entity.Enum.Status;
import by.bsuir.wms.Entity.Product;
import by.bsuir.wms.Entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    Product findAllByIdAndCells_Rack_Warehouse(Integer id, Warehouse warehouse);
    List<Product> findAllByCells_Rack_Warehouse(Warehouse warehouse);
    @Query("SELECT p.id FROM Product p JOIN p.cells c JOIN c.rack r JOIN r.warehouse w WHERE w = :warehouse")
    List<Integer> findIdsByCells_Rack_Warehouse(@Param("warehouse") Warehouse warehouse);
    List<Product> findAllByStatus(Status status);

}
