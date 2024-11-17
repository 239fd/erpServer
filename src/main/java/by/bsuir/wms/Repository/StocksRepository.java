package by.bsuir.wms.Repository;

import by.bsuir.wms.Entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StocksRepository extends JpaRepository<Stock, Integer> {

    List<Stock> findStocksByName(@Param("name") String name);

}
