package by.bsuir.wms.Service;

import by.bsuir.wms.API.ApiResponse;
import by.bsuir.wms.DTO.SupplierDataDTO;
import by.bsuir.wms.DTO.SupplierOrderDTO;
import by.bsuir.wms.Entity.Product;
import by.bsuir.wms.Entity.Stock;
import by.bsuir.wms.Entity.Supplier;
import by.bsuir.wms.Exception.AppException;
import by.bsuir.wms.Repository.ProductRepository;
import by.bsuir.wms.Repository.StocksRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final DemandForecastService demandForecastService;
    private final ProductRepository productRepository;
    private final StocksRepository stockRepository;

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
        System.out.println(totalInventoryInOrganization);
        System.out.println(totalForecastedDemand);
        List<Stock> supplierStocks = stockRepository.findStocksByName(productName);

        int totalAvailableFromSuppliers = supplierStocks.stream()
                .mapToInt(Stock::getAmount)
                .sum();

        if (totalAvailableFromSuppliers < requiredAdditionalQuantity) {
            return ApiResponse.builder()
                    .status(false)
                    .message("Поставщики не могут полностью удовлетворить прогнозируемый спрос.")
                    .build();
        }

        List<SupplierOrderDTO> supplierOrders = new ArrayList<>();
        int remainingQuantity = requiredAdditionalQuantity;

        for (Stock stock : supplierStocks) {
            if (remainingQuantity <= 0) break;

            int orderQuantity = Math.min(stock.getAmount(), remainingQuantity);
            supplierOrders.add(new SupplierOrderDTO(convertToDTO(stock.getSupplier()), productName, orderQuantity));
            remainingQuantity -= orderQuantity;
        }

        return ApiResponse.builder()
                .status(true)
                .message("Необходимо закупить товар у поставщиков для покрытия прогнозируемого спроса.")
                .data(supplierOrders)
                .build();
    }
    private SupplierDataDTO convertToDTO(Supplier supplier) {
        SupplierDataDTO supplierDTO = new SupplierDataDTO();
        supplierDTO.setId(supplier.getId());
        supplierDTO.setInn(supplier.getINN());
        supplierDTO.setName(supplier.getName());
        supplierDTO.setAddress(supplier.getAddress());
        return supplierDTO;
    }
}
