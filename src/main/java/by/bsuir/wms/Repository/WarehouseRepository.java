package by.bsuir.wms.Repository;

import by.bsuir.wms.Entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WarehouseRepository extends JpaRepository<Warehouse, Integer> {
    Warehouse getWarehouseById(Integer integer);
    Warehouse findByIdAndOrganizationId(Integer id, Integer organizationId);
    List<Warehouse> findByOrganizationId(Integer organizationId);
    Optional<Warehouse> findWarehouseByEmployeesId(Integer employeeId);
}
