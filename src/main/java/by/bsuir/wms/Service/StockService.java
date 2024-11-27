package by.bsuir.wms.Service;

import by.bsuir.wms.DTO.StockDTO;
import by.bsuir.wms.Entity.Employees;
import by.bsuir.wms.Entity.Stock;
import by.bsuir.wms.Entity.Supplier;
import by.bsuir.wms.Exception.AppException;
import by.bsuir.wms.Repository.EmployeesRepository;
import by.bsuir.wms.Repository.StockRepository;
import by.bsuir.wms.Repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository StockRepository;
    private final SupplierRepository supplierRepository;
    private final EmployeesRepository employeesRepository;

    public StockDTO createStock(String name, int amount, int suppliersId, double price) {
        findCurrentManager();

        Supplier supplier = supplierRepository.findById(suppliersId)
                .orElseThrow(() -> new AppException("No suppliers with this id", HttpStatus.NOT_FOUND));

        Stock stock = new Stock();
        stock.setName(name);
        stock.setAmount(amount);
        stock.setSupplier(supplier);
        stock.setPrice(price);

        StockRepository.save(stock);

        return convertToDTO(stock);
    }

    public List<StockDTO> getAllStock() {
        findCurrentManager();
        List<Stock> stocks = StockRepository.findAll();
        List<StockDTO> stockDTOs = new ArrayList<>();
        for (Stock stock : stocks) {
            stockDTOs.add(convertToDTO(stock));
        }
        return stockDTOs;
    }

    public StockDTO updateStock(int id, Optional<String> name, Optional<Integer> amount, Optional<Integer> suppliersId, Optional<Double> price) {
        Stock stock = StockRepository.findById(id)
                .orElseThrow(() -> new AppException("Stock not found", HttpStatus.NOT_FOUND));

        name.ifPresent(stock::setName);
        amount.ifPresent(stock::setAmount);
        price.ifPresent(stock::setPrice);

        suppliersId.ifPresent(supId -> {
            Supplier supplier = supplierRepository.findById(supId)
                    .orElseThrow(() -> new AppException("Supplier not found", HttpStatus.NOT_FOUND));
            stock.setSupplier(supplier);
        });

        Stock updatedStock = StockRepository.save(stock);

        return convertToDTO(updatedStock);
    }

    private StockDTO convertToDTO(Stock stock) {
        StockDTO stockDTO = new StockDTO();
        stockDTO.setId(stock.getId());
        stockDTO.setName(stock.getName());
        stockDTO.setAmount(stock.getAmount());
        stockDTO.setPrice(stock.getPrice());
        return stockDTO;
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
