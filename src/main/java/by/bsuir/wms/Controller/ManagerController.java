package by.bsuir.wms.Controller;

import by.bsuir.wms.API.ApiResponse;
import by.bsuir.wms.DTO.*;
import by.bsuir.wms.Service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(maxAge = 3600L)
@RequestMapping("/api/v1/manager")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_MANAGER')")
@RestController
public class ManagerController {

    private final SupplierService supplierService;
    private final DemandForecastService demandForecastService;
    private final StockService stocksService;
    private final OrderService orderService;
    private final ProductService productService;

    @PostMapping("/supplier")
    ResponseEntity<ApiResponse<OrganizationDTO>> createSupplier(@RequestBody OrganizationDTO organizationDTO) {
        supplierService.createSupplierOrganization(organizationDTO);

        ApiResponse<OrganizationDTO> response = ApiResponse.<OrganizationDTO>builder()
                .data(organizationDTO)
                .status(true)
                .message("Supplier created successfully")
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/supplier")
    ResponseEntity<ApiResponse<List<SupplierDataDTO>>> getSuppliers() {
        List<SupplierDataDTO> suppliers = supplierService.getSuppliers();

        ApiResponse<List<SupplierDataDTO>> response = ApiResponse.<List<SupplierDataDTO>>builder()
                .data(suppliers)
                .status(true)
                .message("Supplier created successfully")
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/supplier")
    ResponseEntity<ApiResponse<OrganizationDTO>> updateSupplier(@RequestBody OrganizationDTO organizationDTO) {
        supplierService.updateSupplier(organizationDTO);

        ApiResponse<OrganizationDTO> response = ApiResponse.<OrganizationDTO>builder()
                .data(organizationDTO)
                .status(true)
                .message("Supplier created successfully")
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/supplier")
    public ResponseEntity<ApiResponse<SupplierDTO>> deleteOrganization(@RequestBody SupplierDTO supplierDTO) {
        supplierService.deleteOrganization(supplierDTO);
        ApiResponse<SupplierDTO> response = ApiResponse.<SupplierDTO>builder()
                .status(true)
                .message("Supplier deleted successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/analyze")
    public ResponseEntity<Map<LocalDate, Integer>> getDemandForecast(
            @RequestParam Integer productId,
            @RequestParam int months,
            @RequestParam int forecastMonths
    ) {

        Map<LocalDate, Integer> forecast = demandForecastService.forecastDemand(
                productId, months, forecastMonths);
        ApiResponse< Map<LocalDate, Integer>> response = ApiResponse.< Map<LocalDate, Integer>>builder()
                .status(true)
                .message("Analyze demand")
                .data(forecast)
                .build();
        return ResponseEntity.ok(response.getData());
    }

    @PostMapping("/stock")
    public ResponseEntity<ApiResponse<StockDTO>> createStock(@RequestParam String name,
                                                             @RequestParam int amount,
                                                             @RequestParam double price,
                                                             @RequestParam int suppliersId) {
        StockDTO stocks = stocksService.createStock(name, amount, suppliersId,price);
        ApiResponse<StockDTO> response = ApiResponse.<StockDTO>builder()
                .status(true)
                .message("Update stock")
                .data(stocks)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stock")
    public ResponseEntity<ApiResponse<List<StockDTO>>> getAllStock() {
        List<StockDTO> stocks = stocksService.getAllStock();
        ApiResponse<List<StockDTO>> response = ApiResponse.<List<StockDTO>>builder()
                .status(true)
                .message("Update stock")
                .data(stocks)
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/stock")
    public ResponseEntity<ApiResponse<StockDTO>> updateStock(@RequestParam int id,
                                                             @RequestParam(required = false) Optional<String> name,
                                                             @RequestParam(required = false) Optional<Integer> amount,
                                                             @RequestParam(required = false) Optional<Double> price,
                                                             @RequestParam(required = false) Optional<Integer> suppliersId) {
        StockDTO updatedOrder = stocksService.updateStock(id, name, amount, suppliersId, price);
        ApiResponse<StockDTO> response = ApiResponse.<StockDTO>builder()
                .status(true)
                .message("Update stock")
                .data(updatedOrder)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/order")
    public ResponseEntity<ApiResponse> createOrder(
            @RequestParam Integer productId,
            @RequestParam int months,
            @RequestParam int forecastMonths) {
        try {
            ApiResponse response = orderService.createOrder(productId, months, forecastMonths);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse errorResponse = ApiResponse.builder()
                    .status(false)
                    .message("An error occurred while creating the order: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    @GetMapping("/stored-products")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getStoredProducts() {
        List<ProductDTO> productDTOs = productService.getStoredProducts();
        ApiResponse<List<ProductDTO>> response = ApiResponse.<List<ProductDTO>>builder()
                .data(productDTOs)
                .status(true)
                .message("Products get successful!")
                .build();
        return ResponseEntity.ok(response);
    }


}

