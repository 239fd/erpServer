package by.bsuir.wms.Repository;

import by.bsuir.wms.Entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrganizationRepository extends JpaRepository<Organization, Integer> {
    Optional<Organization> getOrganizationByINN(String substring);
    Optional<Organization> findOrganizationByEmployees_Id(int id);
}