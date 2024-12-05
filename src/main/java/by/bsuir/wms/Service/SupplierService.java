package by.bsuir.wms.Service;

import by.bsuir.wms.DTO.OrganizationDTO;
import by.bsuir.wms.DTO.SupplierDTO;
import by.bsuir.wms.DTO.SupplierDataDTO;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final EmployeesRepository employeesRepository;
    private final GeocodingService geocodingService;


    public void createSupplierOrganization(OrganizationDTO organizationDTO) {

        double[] coordinates = geocodingService.getCoordinatesByAddress(organizationDTO.getAddress());

        Supplier supplier = Supplier.builder()
                .name(organizationDTO.getName())
                .INN(organizationDTO.getInn())
                .address(organizationDTO.getAddress())
                 .latitude(coordinates[0])
                 .longitude(coordinates[1])
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

    public void updateSupplier(OrganizationDTO organizationDTO) {

        findCurrentManager();

        Supplier supplier = supplierRepository.findByINN(organizationDTO.getInn());
        if (supplier == null) {
            throw new AppException("Supplier with this INN does not exist", HttpStatus.CONFLICT);
        }
        if (organizationDTO.getName() != null) {
            supplier.setName(organizationDTO.getName());
        }
        if (organizationDTO.getAddress() != null) {
            double[] coordinates = geocodingService.getCoordinatesByAddress(organizationDTO.getAddress());
            supplier.setAddress(organizationDTO.getAddress());
            supplier.setLatitude(coordinates[0]);
            supplier.setLongitude(coordinates[1]);

        }
        supplierRepository.save(supplier);

    }

    public List<SupplierDataDTO> getSuppliers(){
        findCurrentManager();
        List<SupplierDataDTO> suppliers = new ArrayList<>();
        List<Supplier> suppliersList = supplierRepository.findAll();
        for (Supplier supplier : suppliersList) {
            suppliers.add(conertToDTO(Collections.singletonList(supplier)));
        }
         return suppliers;
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

    private SupplierDataDTO conertToDTO(List<Supplier> suppliers) {
        SupplierDataDTO supplierDTO = new SupplierDataDTO();
        supplierDTO.setId(suppliers.get(0).getId());
        supplierDTO.setName(suppliers.get(0).getName());
        supplierDTO.setInn(suppliers.get(0).getINN());
        supplierDTO.setAddress(suppliers.get(0).getAddress());
        return supplierDTO;
    }
}
// TODO
//  1)CHECK IF SIMILAR INN IN ORGs