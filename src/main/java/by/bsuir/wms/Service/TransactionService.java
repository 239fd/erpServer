package by.bsuir.wms.Service;

import by.bsuir.wms.Entity.ProductSalesHistory;
import by.bsuir.wms.Repository.ProductSalesHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final ProductSalesHistoryRepository productSalesHistoryRepository;

    public Map<String, Map<String, Integer>> getTransactionsSummary() {
        List<ProductSalesHistory> transactions = productSalesHistoryRepository.findAll();

        Map<String, Map<String, Integer>> summary = new HashMap<>();

        for (ProductSalesHistory transaction : transactions) {
            String productId = String.valueOf(transaction.getProduct().getId());
            String transactionType = transaction.getTransactionType().name().toLowerCase();

            summary.putIfAbsent(productId, new HashMap<>());
            summary.get(productId).put(transactionType,
                    summary.get(productId).getOrDefault(transactionType, 0) + transaction.getQuantity());
        }

        return summary;
    }
}
