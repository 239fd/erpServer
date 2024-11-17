package by.bsuir.wms.Repository;

import by.bsuir.wms.Entity.Enum.Type;
import by.bsuir.wms.Entity.ProductSalesHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ProductSalesRepository extends JpaRepository<ProductSalesHistory, Integer> {

    @Query("SELECT psh FROM ProductSalesHistory psh WHERE psh.product.id IN :productIds AND psh.transactionType = :type AND psh.date >= :startDate")
    List<ProductSalesHistory> findSalesHistoryByProductsAndDate(
            @Param("productIds") List<Integer> productIds,
            @Param("type") Type transactionType,
            @Param("startDate") LocalDateTime startDate
    );
}
