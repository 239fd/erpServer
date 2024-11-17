package by.bsuir.wms.Repository;

import by.bsuir.wms.Entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SupplierRepository extends JpaRepository<Supplier, Integer> {
    Optional<Supplier> findSupplierByINN(String inn);
    Supplier findByINN(String inn);
}
