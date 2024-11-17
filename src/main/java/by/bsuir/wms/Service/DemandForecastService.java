package by.bsuir.wms.Service;

import by.bsuir.wms.Entity.Employees;
import by.bsuir.wms.Entity.Enum.Type;
import by.bsuir.wms.Entity.Product;
import by.bsuir.wms.Entity.ProductSalesHistory;
import by.bsuir.wms.Exception.AppException;
import by.bsuir.wms.Repository.EmployeesRepository;
import by.bsuir.wms.Repository.ProductRepository;
import by.bsuir.wms.Repository.ProductSalesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DemandForecastService {

    private final ProductSalesRepository productSalesHistoryRepository;
    private final ProductRepository productRepository;
    private final EmployeesRepository employeesRepository;

    public Map<LocalDate, Integer> forecastDemand(Integer productId, int months, int forecastMonths) {

        findCurrentManager();

        LocalDateTime startDate = LocalDate.now().minusMonths(months).atStartOfDay();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException("No products with this ID", HttpStatus.NOT_FOUND));

        List<Product> products = productRepository.findAllByName(product.getName());
        if (products.isEmpty()) {
            throw new AppException("No products found with the same name", HttpStatus.NOT_FOUND);
        }

        List<Integer> productIds = products.stream()
                .map(Product::getId)
                .collect(Collectors.toList());

        List<ProductSalesHistory> salesHistory = productSalesHistoryRepository.findSalesHistoryByProductsAndDate(
                productIds, Type.dispatch, startDate
        );

        if (salesHistory.isEmpty()) {
            return generateZeroForecast(months);
        }

        Map<LocalDate, Integer> monthlySales = salesHistory.stream()
                .collect(Collectors.groupingBy(
                        history -> history.getDate().toLocalDate().withDayOfMonth(1),
                        Collectors.summingInt(ProductSalesHistory::getQuantity)
                ));

        List<LocalDate> dates = new ArrayList<>(monthlySales.keySet());
        Collections.sort(dates);
        List<Integer> quantities = dates.stream()
                .map(monthlySales::get)
                .collect(Collectors.toList());

        if (quantities.isEmpty()) {
            return generateZeroForecast(months);
        }

        double avgChangePerMonth = calculateAverageChange(quantities);

        Map<Integer, Double> monthlySeasonality = calculateSeasonalFactors(salesHistory);

        int movingAverage = calculateMovingAverage(quantities);

        return generateForecast(dates, quantities, avgChangePerMonth, monthlySeasonality, forecastMonths);
    }

    private Map<LocalDate, Integer> generateForecast(
            List<LocalDate> dates, List<Integer> quantities, double avgChangePerMonth,
            Map<Integer, Double> monthlySeasonality, int forecastMonths
    ) {
        Map<LocalDate, Integer> futureDemand = new HashMap<>();
        LocalDate lastDate = dates.get(dates.size() - 1);

        int baseQuantity = quantities.stream().mapToInt(Integer::intValue).sum() / quantities.size();

        for (int i = 1; i <= forecastMonths; i++) {
            LocalDate futureDate = lastDate.plusMonths(i);
            int month = futureDate.getMonthValue();

            double seasonalFactor = monthlySeasonality.getOrDefault(month, 1.0);
            seasonalFactor = Math.min(Math.max(seasonalFactor, 0.5), 1.5);

            int predictedQuantity = Math.max(0, (int) (
                    baseQuantity + avgChangePerMonth * i
                            * seasonalFactor
            ));

            futureDemand.put(futureDate, predictedQuantity);
        }

        return new TreeMap<>(futureDemand);
    }


    private Map<LocalDate, Integer> generateZeroForecast(int months) {
        Map<LocalDate, Integer> zeroForecast = new HashMap<>();
        LocalDate currentDate = LocalDate.now().withDayOfMonth(1);

        for (int i = 0; i < months; i++) {
            zeroForecast.put(currentDate.plusMonths(i), 0);
        }

        return zeroForecast;
    }

    private Map<Integer, Double> calculateSeasonalFactors(List<ProductSalesHistory> salesHistory) {
        Map<Integer, List<Integer>> monthlyData = salesHistory.stream()
                .collect(Collectors.groupingBy(
                        history -> history.getDate().getMonthValue(),
                        Collectors.mapping(ProductSalesHistory::getQuantity, Collectors.toList())
                ));

        Map<Integer, Double> monthlySeasonality = new HashMap<>();
        monthlyData.forEach((month, quantities) -> {
            double total = quantities.stream().mapToInt(Integer::intValue).sum();
            double average = !quantities.isEmpty() ? total / quantities.size() : 1.0;
            monthlySeasonality.put(month, average);
        });

        return monthlySeasonality;
    }

    private double calculateAverageChange(List<Integer> quantities) {
        if (quantities.size() < 2) {
            return 0;
        }

        List<Integer> changes = new ArrayList<>();
        for (int i = 1; i < quantities.size(); i++) {
            changes.add(quantities.get(i) - quantities.get(i - 1));
        }

        return changes.stream().mapToInt(Integer::intValue).average().orElse(0.0);
    }

    private int calculateMovingAverage(List<Integer> quantities) {
        if (quantities.size() < 3) {
            return quantities.stream().mapToInt(Integer::intValue).sum() / quantities.size();
        }
        int sum = 0;
        for (int i = quantities.size() - 3; i < quantities.size(); i++) {
            sum += quantities.get(i);
        }
        return sum / 3;
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
