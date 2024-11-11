package by.bsuir.wms.Repository;

import by.bsuir.wms.Entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Integer> {

}
