package by.bsuir.wms.Service;

import by.bsuir.wms.DTO.WarehouseDTO;
import by.bsuir.wms.DTO.WarehouseRequestDTO;
import by.bsuir.wms.Entity.*;
import by.bsuir.wms.Repository.EmployeesRepository;
import by.bsuir.wms.Repository.WarehouseRepository;
import by.bsuir.wms.Repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final EmployeesRepository employeesRepository;
    private final OrganizationRepository organizationRepository;

    public void createWarehouse( WarehouseRequestDTO warehouseRequestDTO) {

        Employees director = findCurrentDirector();

        Organization organization = organizationRepository.getOrganizationByINN(director.getOrganization().getINN())
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        Warehouse warehouse = Warehouse.builder()
                .name(warehouseRequestDTO.getName())
                .address(warehouseRequestDTO.getAddress())
                .organization(organization)
                .build();

        List<Rack> racks = warehouseRequestDTO.getRacks().stream()
                .map(rackDTO -> {

                    Rack rack = Rack.builder()
                            .capacity(rackDTO.getCapacity())
                            .warehouse(warehouse)
                            .build();

                    List<Cell> cells = new ArrayList<>();
                    for (int i = 0; i < rackDTO.getCellCount(); i++) {
                        Cell cell = Cell.builder()
                                .height(rackDTO.getCellHeight())
                                .width(rackDTO.getCellWidth())
                                .length(rackDTO.getCellLength())
                                .rack(rack)
                                .build();
                        cells.add(cell);
                    }

                    rack.setCells(cells);
                    return rack;
                })
                .collect(Collectors.toList());

        warehouse.setRacks(racks);

        warehouseRepository.save(warehouse);
    }

    public void deleteWarehouse(Integer warehouseId) {
        Employees director = findCurrentDirector();

        warehouseRepository.deleteById(warehouseId);
    }

    public List<WarehouseDTO> getAllWarehousesByOrganizationId() {

        Employees director = findCurrentDirector();

        List<Warehouse> warehouses = warehouseRepository.findByOrganizationId(director.getOrganization().getId());

        return warehouses.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public WarehouseDTO getWarehouse(Integer warehouseId) {
        Employees director = findCurrentDirector();

        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new RuntimeException("Warehouse not found"));

        return convertToDTO(warehouse);
    }

    public WarehouseDTO updateWarehouse(Integer warehouseId, WarehouseDTO warehouseDTO) {

        Employees director = findCurrentDirector();

        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new RuntimeException("Warehouse not found"));

        warehouse.setName(warehouseDTO.getName());
        warehouse.setAddress(warehouseDTO.getAddress());
        warehouseRepository.save(warehouse);

        return convertToDTO(warehouse);
    }

    public Employees findCurrentDirector() {
        String currentUsername = getCurrentUsername();
        return employeesRepository.findByLogin(currentUsername)
                .filter(Employees::isDirector)
                .orElseThrow(() -> new RuntimeException("Current director not found or does not have DIRECTOR role"));
    }

    private String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }

    private WarehouseDTO convertToDTO(Warehouse warehouse) {
        return WarehouseDTO.builder()
                .name(warehouse.getName())
                .address(warehouse.getAddress())
                .build();
    }

}
