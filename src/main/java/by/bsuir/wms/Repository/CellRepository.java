package by.bsuir.wms.Repository;

import by.bsuir.wms.Entity.Cell;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CellRepository extends JpaRepository<Cell, Integer> {
    List<Cell> findAllByRackId(int rackId);
}
