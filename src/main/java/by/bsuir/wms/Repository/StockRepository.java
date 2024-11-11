package by.bsuir.wms.Repository;

import by.bsuir.wms.Entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockRepository extends JpaRepository<Stock, Integer> {
}
