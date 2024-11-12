package by.bsuir.wms.Repository;

import by.bsuir.wms.Entity.Product;
import by.bsuir.wms.Entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    Product findAllByIdAndCells_Rack_Warehouse(Integer id, Warehouse warehouse);
}
