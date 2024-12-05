package by.bsuir.wms.Controller;

import by.bsuir.wms.API.ApiResponse;
import by.bsuir.wms.DTO.OrganizationDTO;
import by.bsuir.wms.DTO.WarehouseDTO;
import by.bsuir.wms.DTO.WarehouseRequestDTO;
import by.bsuir.wms.Service.OrganizationService;
import by.bsuir.wms.Service.TransactionService;
import by.bsuir.wms.Service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(maxAge = 3600L)
@RestController
@RequestMapping("/api/v1/director")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_DIRECTOR')")
public class DirectorController {

    private final OrganizationService organizationService;
    private final WarehouseService warehouseService;
    private final TransactionService transactionService;

    @PostMapping("/organization")
    public ResponseEntity<ApiResponse<OrganizationDTO>> createOrganization(@RequestBody OrganizationDTO organizationDTO) {
        organizationService.createOrganization(organizationDTO);

        ApiResponse<OrganizationDTO> response = ApiResponse.<OrganizationDTO>builder()
                .data(organizationDTO)
                .status(true)
                .message("Organization created successfully and linked to Director")
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/organization/warehouse")
    public ResponseEntity<ApiResponse<WarehouseRequestDTO>> createWarehouse(@RequestBody WarehouseRequestDTO warehouseRequestDTO) {

        warehouseService.createWarehouse( warehouseRequestDTO);

        ApiResponse<WarehouseRequestDTO> response = ApiResponse.<WarehouseRequestDTO>builder()
                .data(warehouseRequestDTO)
                .status(true)
                .message("Warehouse created successfully with racks and cells linked to Organization")
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/organization")
    public ResponseEntity<ApiResponse<OrganizationDTO>> getAllOrganizations() {
        OrganizationDTO organizations = organizationService.getAllOrganizations();
        ApiResponse<OrganizationDTO> response = ApiResponse.<OrganizationDTO>builder()
                .data(organizations)
                .status(true)
                .message("Organizations retrieved successfully")
                .build();
        return ResponseEntity.ok(response);

    }

    @PutMapping("/organization")
    public ResponseEntity<ApiResponse<OrganizationDTO>> updateOrganization(@RequestBody OrganizationDTO organizationDTO) {

        OrganizationDTO updatedOrganization = organizationService.updateOrganization(organizationDTO);
        ApiResponse<OrganizationDTO> response =  ApiResponse.<OrganizationDTO>builder()
                .data(updatedOrganization)
                .status(true)
                .message("Organization updated successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/organization")
    public ResponseEntity<ApiResponse<Void>> deleteOrganization() {
        organizationService.deleteOrganization();
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .status(true)
                .message("Organization deleted successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/organization/warehouses")
    public ResponseEntity<ApiResponse<List<WarehouseDTO>>> getAllWarehouses() {
        List<WarehouseDTO> warehouses = warehouseService.getAllWarehousesByOrganizationId();
        ApiResponse<List<WarehouseDTO>> response = ApiResponse.<List<WarehouseDTO>>builder()
                .data(warehouses)
                .status(true)
                .message("Warehouses retrieved successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/organization/warehouses/{warehouseId}")
    public ResponseEntity<ApiResponse<WarehouseDTO>> getWarehouse(@PathVariable Integer warehouseId) {
        WarehouseDTO warehouse = warehouseService.getWarehouse(warehouseId);
        ApiResponse<WarehouseDTO> response =  ApiResponse.<WarehouseDTO>builder()
                .data(warehouse)
                .status(true)
                .message("Warehouse retrieved successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/organization/warehouses/{warehouseId}")
    public ResponseEntity<ApiResponse<WarehouseDTO>> updateWarehouse(@PathVariable Integer warehouseId, @RequestBody WarehouseDTO warehouseDTO) {

        WarehouseDTO updatedWarehouse = warehouseService.updateWarehouse(warehouseId, warehouseDTO);

        ApiResponse<WarehouseDTO> response = ApiResponse.<WarehouseDTO>builder()
                .data(updatedWarehouse)
                .status(true)
                .message("Warehouse updated successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/organization/warehouses/{warehouseId}")
    public ResponseEntity<ApiResponse<Void>> deleteWarehouse(@PathVariable Integer warehouseId) {
        warehouseService.deleteWarehouse(warehouseId);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .status(true)
                .message("Warehouse deleted successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/transactions")
    public ResponseEntity<Map<String, Map<String, Integer>>> getTransactionsSummary() {
        Map<String, Map<String, Integer>> transactionsSummary = transactionService.getTransactionsSummary();
        return ResponseEntity.ok(transactionsSummary);
    }


}
