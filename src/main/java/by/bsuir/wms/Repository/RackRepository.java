package by.bsuir.wms.Repository;

import by.bsuir.wms.Entity.Rack;
import by.bsuir.wms.Entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RackRepository extends JpaRepository<Rack, Integer> {
    Optional<List<Rack>> findRacksByWarehouse(Warehouse warehouse);
}
