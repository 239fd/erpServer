package by.bsuir.wms.Service;

import by.bsuir.wms.DTO.OrganizationDTO;
import by.bsuir.wms.DTO.SupplierDTO;
import by.bsuir.wms.Entity.Employees;
import by.bsuir.wms.Entity.Supplier;
import by.bsuir.wms.Exception.AppException;
import by.bsuir.wms.Repository.EmployeesRepository;
import by.bsuir.wms.Repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final EmployeesRepository employeesRepository;

    public void createSupplierOrganization(OrganizationDTO organizationDTO) {
         Supplier supplier = Supplier.builder()
                .name(organizationDTO.getName())
                .INN(organizationDTO.getInn())
                .address(organizationDTO.getAddress())
                .build();

        findCurrentManager();

        List<Supplier> suppliers = supplierRepository.findAll();
        for (Supplier value : suppliers) {
            if (Objects.equals(value.getINN(), supplier.getINN())) {
                throw new AppException("Supplier with this INN already exist", HttpStatus.CONFLICT);
            }
        }
        supplierRepository.save(supplier);
    }

    public void deleteOrganization(SupplierDTO supplierDTO) {

        findCurrentManager();

        Supplier supplier = supplierRepository.findSupplierByINN(supplierDTO.getInn())
                .orElseThrow(() -> new AppException("Supplier with this INN does not exist", HttpStatus.NOT_FOUND));

        supplierRepository.deleteById(supplier.getId());
    }

    public void updateOrganization(OrganizationDTO organizationDTO) {

        findCurrentManager();

        Supplier supplier = supplierRepository.findByINN(organizationDTO.getInn());
        if (supplier == null) {
            throw new AppException("Supplier with this INN does not exist", HttpStatus.CONFLICT);
        }
        if (organizationDTO.getName() != null) {
            supplier.setName(organizationDTO.getName());
        }
        if (organizationDTO.getAddress() != null) {
            supplier.setAddress(organizationDTO.getAddress());

        }
        supplierRepository.save(supplier);

    }

    public List<Supplier> getSuppliers(){
        findCurrentManager();
        return supplierRepository.findAll();
    }

    private void findCurrentManager() {
        String currentUsername = getCurrentUsername();
        employeesRepository.findByLogin(currentUsername)
                .filter(Employees::isManager)
                .orElseThrow(() -> new RuntimeException("Current manager not found or does not have MANAGER role"));
    }

    private String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }
}
// TODO
//  1)CHECK IF SIMILAR INN IN ORGs