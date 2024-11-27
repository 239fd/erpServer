package by.bsuir.wms.Service;

import by.bsuir.wms.API.ApiResponse;
import by.bsuir.wms.DTO.SupplierDataDTO;
import by.bsuir.wms.DTO.SupplierOrderDTO;
import by.bsuir.wms.Entity.*;
import by.bsuir.wms.Exception.AppException;
import by.bsuir.wms.Repository.EmployeesRepository;
import by.bsuir.wms.Repository.ProductRepository;
import by.bsuir.wms.Repository.StocksRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final DemandForecastService demandForecastService;
    private final ProductRepository productRepository;
    private final EmployeesRepository employeesRepository;
    private final StocksRepository stockRepository;
    private final RouteService routeService;

    private static final double ORDER_COST = 50;
    private static final double HOLDING_COST = 2;
    private static final double FUEL_COST = 2.44;
    private static final double FUEL_CONSUMPTION = 20.0;

    public ApiResponse createOrder(Integer productId, int months, int forecastMonths) {

        Map<LocalDate, Integer> forecastedDemand = demandForecastService.forecastDemand(productId, months, forecastMonths);
        int totalForecastedDemand = forecastedDemand.values().stream().mapToInt(Integer::intValue).sum();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException("No product with this ID", HttpStatus.NOT_FOUND));
        String productName = product.getName();

        int totalInventoryInOrganization = productRepository.findTotalInventoryByName(productName);
        if (totalInventoryInOrganization >= totalForecastedDemand) {
            return ApiResponse.builder()
                    .status(true)
                    .message("Запасы в организации достаточны для покрытия прогнозируемого спроса.")
                    .build();
        }

        int requiredAdditionalQuantity = totalForecastedDemand - totalInventoryInOrganization;

        double eoq = calculateEOQ(totalForecastedDemand, ORDER_COST, HOLDING_COST);
        int requiredOrderQuantity = (int) Math.max(eoq, requiredAdditionalQuantity);

        List<Stock> supplierStocks = stockRepository.findStocksByName(productName);
        int totalAvailableFromSuppliers = supplierStocks.stream()
                .mapToInt(Stock::getAmount)
                .sum();

        if (totalAvailableFromSuppliers < requiredOrderQuantity) {
            return ApiResponse.builder()
                    .status(false)
                    .message("Поставщики не могут полностью удовлетворить прогнозируемый спрос.")
                    .build();
        }

        List<SupplierOrderDTO> supplierOrders = new ArrayList<>();

        int remainingQuantity = requiredOrderQuantity;
        Employees employees = findCurrentManager()
                .orElseThrow();

        Warehouse warehouse = employees.getWarehouse();
        for (Stock stock : supplierStocks) {
            if (remainingQuantity <= 0) break;

            int orderQuantity = Math.min(stock.getAmount(), remainingQuantity);

            double distance = routeService.getDistance(warehouse.getLatitude(), warehouse.getLongitude(), stock.getSupplier().getLatitude(), stock.getSupplier().getLongitude());

            double deliveryCost = calculateDeliveryCost(distance);

            supplierOrders.add(new SupplierOrderDTO(convertToDTO(stock.getSupplier()), productName, orderQuantity, orderQuantity*eoq, deliveryCost));
            remainingQuantity -= orderQuantity;
        }

        return ApiResponse.builder()
                .status(true)
                .message("Необходимо закупить товар у поставщиков для покрытия прогнозируемого спроса.")
                .data(supplierOrders)
                .build();
    }

    private double calculateEOQ(double demand, double orderCost, double holdingCost) {
        return Math.sqrt((2 * demand * orderCost) / holdingCost);
    }

    private double calculateDeliveryCost(double distance) {
        return (distance / 100000) * FUEL_CONSUMPTION * FUEL_COST;
    }

    private SupplierDataDTO convertToDTO(Supplier supplier) {
        SupplierDataDTO supplierDTO = new SupplierDataDTO();
        supplierDTO.setId(supplier.getId());
        supplierDTO.setInn(supplier.getINN());
        supplierDTO.setName(supplier.getName());
        supplierDTO.setAddress(supplier.getAddress());
        return supplierDTO;
    }

    private Optional<Employees> findCurrentManager() {
        String currentUsername = getCurrentUsername();
        employeesRepository.findByLogin(currentUsername)
                .filter(Employees::isManager)
                .orElseThrow(() -> new RuntimeException("Current manager not found or does not have MANAGER role"));
        return employeesRepository.findByLogin(currentUsername);
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
