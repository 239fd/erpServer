package by.bsuir.wms.Repository;

import by.bsuir.wms.Entity.Employees;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeesRepository extends JpaRepository<Employees, Integer> {
    Optional<Employees> findByLogin(String login);
}
