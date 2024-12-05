package by.bsuir.wms.Service;

import by.bsuir.wms.DTO.*;
import by.bsuir.wms.Entity.*;
import by.bsuir.wms.Entity.Enum.Status;
import by.bsuir.wms.Entity.Enum.Type;
import by.bsuir.wms.Exception.AppException;
import by.bsuir.wms.Repository.*;
import com.itextpdf.text.DocumentException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final RackRepository rackRepository;
    private final CellRepository cellRepository;
    private final EmployeesRepository employeesRepository;
    private final PDFService pdfService;
    private final ProductSalesRepository productSalesHistoryRepository;

    public byte[] performInventoryCheck(InventoryDTO inventoryDTO) throws DocumentException, IOException {
        Employees worker;
        if(findAccountant() != null){
            worker = findAccountant();
        }
        else {
            throw new AppException("Current worker not found or does not have ACCOUNTANT or WORKER role", HttpStatus.CONFLICT);
        }
        Warehouse warehouse = warehouseRepository.findWarehouseByEmployeesId(worker.getId())
                .orElseThrow(() -> new RuntimeException("No warehouse for this user"));

        if (inventoryDTO.getIds().size() != inventoryDTO.getAmounts().size()) {
            throw new AppException("Mistake in input", HttpStatus.BAD_REQUEST);
        }

        List<Product> products = new ArrayList<>();
        Map<Integer, Integer> expectedQuantities = new HashMap<>();
        List<ProductDTO> productDTOs = new ArrayList<>();

        for (int i = 0; i < inventoryDTO.getIds().size(); i++) {
            Integer productId = inventoryDTO.getIds().get(i);
            Integer expectedAmount = inventoryDTO.getAmounts().get(i);

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("No product found for this discrepancy product"));

            products.add(product);
            expectedQuantities.put(productId, expectedAmount);
            productDTOs.add(convertToDTO(product));
            product.setAmount(inventoryDTO.getAmounts().get(i));

        }

        productRepository.saveAll(products);

        List<Integer> productsIdsOnWarehouse = productRepository.findIdsByCells_Rack_Warehouse(warehouse);



        for (Integer productId : productsIdsOnWarehouse) {
            if (!expectedQuantities.containsKey(productId)) {
                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new RuntimeException("Product not found in warehouse"));
                product.setStatus(Status.nonverified);
                productRepository.save(product);
            }
        }



        return pdfService.generateInventoryReport(worker.getOrganization().getName(), warehouse.getName(), productDTOs, inventoryDTO.getAmounts(), inventoryDTO.getIds());
    }

    public byte[] writeOffProduct(WriteOffDTO writeOffDTO) throws DocumentException, IOException {

        Employees worker;
        if(findAccountant() != null){
            worker = findAccountant();
        }
        else {
            throw new AppException("Current worker not found or does not have ACCOUNTANT or WORKER role", HttpStatus.CONFLICT);
        }
        warehouseRepository.findWarehouseByEmployeesId(worker.getId())
                .orElseThrow(() -> new RuntimeException("No warehouse for this user"));

        List<Product> products = new ArrayList<>();
        for(int i = 0; i < writeOffDTO.getProductId().size(); i++) {
            Product product = productRepository.findById(writeOffDTO.getProductId().get(i))
                    .orElseThrow(() -> new AppException("Product not found", HttpStatus.NOT_FOUND));
            if(product.getAmount() < writeOffDTO.getQuantity().get(i)) {
                throw new AppException("Insufficient quantity for write-off", HttpStatus.BAD_REQUEST);
            }
            products.add(product);
        }
        if(products.isEmpty()){
            throw new AppException("Mistake in your input", HttpStatus.NOT_FOUND);
        }

        List<ProductDTO> productDTOs = products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        for (int i = 0; i < writeOffDTO.getProductId().size(); i++) {
            products.get(i).setAmount(products.get(i).getAmount() - writeOffDTO.getQuantity().get(i));
            productRepository.save(products.get(i));
        }


        return pdfService.generateWriteOffAct(worker.getOrganization().getName(), LocalDate.parse(writeOffDTO.getDate()), "бухгалтер " + worker.getSecondName() + " " + worker.getFirstName(), productDTOs);
    }

    public byte[] revaluateProducts(RevaluationDTO revaluationDTO) throws DocumentException, IOException {

        Employees worker;
        if(findAccountant() != null){
            worker = findAccountant();
        }
        else {
            throw new AppException("Current worker not found or does not have ACCOUNTANT or WORKER role", HttpStatus.CONFLICT);
        }
        warehouseRepository.findWarehouseByEmployeesId(worker.getId())
                .orElseThrow(() -> new RuntimeException("No warehouse for this user"));

        List<Product> products = new ArrayList<>();
        for(int i = 0; i < revaluationDTO.getProductIds().size(); i++) {
            Product product = productRepository.findById(revaluationDTO.getProductIds().get(i))
                    .orElseThrow(() -> new AppException("Product not found", HttpStatus.NOT_FOUND));
            products.add(product);
        }

        List<Integer> productIds = revaluationDTO.getProductIds();
        List<Double> newPrices = revaluationDTO.getNewPrice();

        if (productIds.size() != newPrices.size()) {
            throw new AppException("Mismatch between product IDs and prices", HttpStatus.BAD_REQUEST);
        }

        List<ProductDTO> revaluatedProducts = new ArrayList<>();
        List<Double> revaluatedPrices = new ArrayList<>();
        for (int i = 0; i < products.size(); i++) {
            Product product = products.get(i);
            double oldPrice = product.getPrice();
            double newPrice = newPrices.get(i);
            int quantity = product.getAmount();

            product.setPrice(newPrice);
            productRepository.save(product);

            ProductDTO productDTO = ProductDTO.builder()
                    .name(product.getName())
                    .unit(product.getUnit())
                    .amount(quantity)
                    .price(oldPrice)
                    .build();
            revaluatedProducts.add(productDTO);
            revaluatedPrices.add(newPrice);
        }

        return pdfService.generateRevaluationReport(revaluatedProducts, revaluatedPrices);
    }

    public byte[] addProductToCell(List<ProductDTO> productDTOs) throws DocumentException, IOException {
        Employees worker;
        if(findCurrentWorker() != null){
            worker = findCurrentWorker();
        }
        else {
            throw new AppException("Current worker not found or does not have ACCOUNTANT or WORKER role", HttpStatus.CONFLICT);
        }
        Warehouse warehouse = warehouseRepository.findWarehouseByEmployeesId(worker.getId())
                .orElseThrow(() -> new RuntimeException("No warehouse for this user"));

        List<Rack> racks = rackRepository.findRacksByWarehouse(warehouse)
                .orElseThrow(() -> new RuntimeException("No racks in warehouse for this user"));
        List<Integer> ids = new ArrayList<>();
        List<ProductDTO> placedProducts = new ArrayList<>();
        List<Object> dataForSave = new ArrayList<>();

        for (ProductDTO productDTO : productDTOs) {
            boolean productPlaced = false;

            for (Rack rack : racks) {
                List<Cell> cells = cellRepository.findAllByRackId(rack.getId());

                for (Cell cell : cells) {

                    double cellVolume = cell.getLength() * cell.getWidth() * cell.getHeight();

                    double usedVolume = cell.getProducts().stream()
                            .mapToDouble(p -> p.getLength() * p.getWidth() * p.getHeight() * p.getAmount())
                            .sum();

                    double availableVolume = cellVolume - usedVolume;

                    double productVolume = productDTO.getLength() * productDTO.getWidth() * productDTO.getHeight() * productDTO.getAmount();

                    if (availableVolume >= productVolume && rack.getCapacity() >= productDTO.getWeight() + usedCapacityInRack(rack)) {
                        if (cell.getLength() >= productDTO.getLength() &&
                                cell.getWidth() >= productDTO.getWidth() &&
                                cell.getHeight() >= productDTO.getHeight()) {

                            Product product = getProduct(productDTO);
                            product.getCells().add(cell);
                            dataForSave.add(product);

                            cell.getProducts().add(product);
                            dataForSave.add(cell);

                            placedProducts.add(productDTO);
                            productPlaced = true;
                            break;
                        }
                    }
                }
                if (productPlaced) {
                    break;
                }
            }

            if (!productPlaced) {
                throw new AppException("No suitable rack or cell found for product: " + productDTO.getName(), HttpStatus.BAD_REQUEST);
            } else {
                if (dataForSave.size() / 2 == productDTOs.size()) {
                    for (int i = 0; i < dataForSave.size(); i += 2) {
                        Product product = (Product) dataForSave.get(i);
                        productRepository.save(product);
                        ids.add(product.getId());
                        Cell cell = (Cell) dataForSave.get(i + 1);
                        cellRepository.save(cell);

                        logProductHistory(product, warehouse, worker, product.getAmount(), Type.accept);
                    }
                }
            }
        }
        return pdfService.generateReceiptOrderPDF(placedProducts, ids, worker);
    }

    public Map<String, byte[]> dispatchProducts(DispatchDTO dispatchDTO) throws DocumentException, IOException {
        Employees worker;
        if(findCurrentWorker() != null){
            worker = findCurrentWorker();
        }
        else {
            throw new AppException("Current worker not found or does not have ACCOUNTANT or WORKER role", HttpStatus.CONFLICT);
        }
        Warehouse warehouse = warehouseRepository.findWarehouseByEmployeesId(worker.getId())
                .orElseThrow(() -> new AppException("No warehouse for this user", HttpStatus.CONFLICT));

        List<Product> products = dispatchDTO.getProductIds().stream()
                .map(productId -> productRepository.findAllByIdAndCells_Rack_Warehouse(productId, warehouse))
                .filter(Objects::nonNull)
                .toList();

        if (products.isEmpty()) {
            throw new AppException("No products found for dispatch", HttpStatus.CONFLICT);
        }

        boolean isQuantitySufficient = IntStream.range(0, products.size())
                .allMatch(i -> products.get(i).getAmount() >= dispatchDTO.getAmounts().get(i));

        if (!isQuantitySufficient) {
            throw new AppException("Insufficient quantity for one of the products", HttpStatus.CONFLICT);
        }

        List<ProductDTO> productDTOs = products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        for (int i = 0; i < products.size(); i++) {
            Product product = products.get(i);
            int dispatchAmount = dispatchDTO.getAmounts().get(i);

            product.setAmount(product.getAmount() - dispatchAmount);

            if (product.getAmount() <= 0) {
                product.getCells().forEach(cell -> {
                    cell.getProducts().remove(product);
                    cellRepository.save(cell);
                });
                product.getCells().clear();
            } else {
                productRepository.save(product);
            }

            logProductHistory(product, warehouse, worker, dispatchAmount, Type.dispatch);
        }

        byte[] orderPDF = pdfService.generateDispatchOrderPDF(productDTOs, dispatchDTO.getProductIds(), worker);
        byte[] ttnPDF = pdfService.generateTTN(dispatchDTO, productDTOs);

        Map<String, byte[]> pdfFiles = new HashMap<>();
        pdfFiles.put("dispatch_order.pdf", orderPDF);
        pdfFiles.put("ttn.pdf", ttnPDF);

        return pdfFiles;
    }

    public List<ProductDTO> findNonVerified(){
        Employees worker;
        if(findAccountant() != null){
            worker = findAccountant();
        }
        else {
            throw new AppException("Current worker not found or does not have ACCOUNTANT or WORKER role", HttpStatus.CONFLICT);
        }
        warehouseRepository.findWarehouseByEmployeesId(worker.getId())
                .orElseThrow(() -> new RuntimeException("No warehouse for this user"));

        return productRepository.findAllByStatus(Status.nonverified).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ProductDTO> getStoredProducts() {
        Employees worker;
        if(findCurrentWorker() != null){
            worker = findCurrentWorker();
        }
        else if(findAccountant() != null){
            worker = findAccountant();
        }
        else if(findManager() != null) {
            worker = findManager();
        }
        else {
            throw new AppException("Current worker not found or does not have ACCOUNTANT or WORKER role", HttpStatus.CONFLICT);
        }
        Warehouse warehouse = warehouseRepository.findWarehouseByEmployeesId(worker.getId())
                .orElseThrow(() -> new AppException("No warehouse for this user", HttpStatus.CONFLICT));

        List<Product> productDTOs = productRepository.findAllByCells_Rack_Warehouse(warehouse);

        if(productDTOs.isEmpty()){
            throw new AppException("No products found for current warehouse", HttpStatus.CONFLICT);
        }
        List<ProductDTO> productDTOList = new ArrayList<>();
        for(Product product : productDTOs){
            if (product.getStatus() == Status.accepted && product.getAmount() > 0) {
                productDTOList.add(convertToDTO(product));
            }
        }
        return productDTOList;
    }

    public String getProductLocation(int id){
        Employees worker;
        if(findCurrentWorker() != null){
            worker = findCurrentWorker();
        }
        else {
            throw new AppException("Current worker not found or does not have ACCOUNTANT or WORKER role", HttpStatus.CONFLICT);
        }        warehouseRepository.findWarehouseByEmployeesId(worker.getId())
                .orElseThrow(() -> new AppException("No warehouse for this user", HttpStatus.CONFLICT));

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException("Product not found", HttpStatus.NOT_FOUND));

        if (product.getCells().isEmpty()) {
            throw new AppException("Product is not located in any cell", HttpStatus.NOT_FOUND);
        }

        Cell cell = product.getCells().iterator().next();
        Rack rack = cell.getRack();

        return "Стеллаж: " + rack.getId() + ", Ячейка: " + cell.getId();
    }

    private double usedCapacityInRack(Rack rack) {
        return rack.getCells().stream()
                .flatMap(cell -> cell.getProducts().stream())
                .mapToDouble(Product::getWeight)
                .sum();
    }

    private ProductDTO convertToDTO(Product product) {

        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .unit(product.getUnit())
                .amount(product.getAmount())
                .price(product.getPrice())
                .height(product.getHeight())
                .length(product.getLength())
                .width(product.getWidth())
                .weight(product.getWeight())
                .bestBeforeDate(product.getBestBeforeDate())
                .build();
    }

    private static Product getProduct(ProductDTO productDTO) {
        Product product = new Product();
        product.setName(productDTO.getName());
        product.setAmount(productDTO.getAmount());
        product.setHeight(productDTO.getHeight());
        product.setLength(productDTO.getLength());
        product.setWidth(productDTO.getWidth());
        product.setWeight(productDTO.getWeight());
        product.setPrice(productDTO.getPrice());
        product.setUnit(productDTO.getUnit());
        product.setBestBeforeDate(productDTO.getBestBeforeDate());
        product.setStatus(Status.accepted);
        return product;
    }

    private void logProductHistory(Product product, Warehouse warehouse, Employees employee, int quantity, Type operationType) {
        ProductSalesHistory salesHistory = new ProductSalesHistory();
        salesHistory.setProduct(product);
        salesHistory.setDate(LocalDate.now().atStartOfDay());
        salesHistory.setQuantity(quantity);
        salesHistory.setTransactionType(operationType);
        salesHistory.setWarehouse(warehouse);
        salesHistory.setEmployees(employee);

        productSalesHistoryRepository.save(salesHistory);
    }

    private Employees findCurrentWorker() {
        String currentUsername = getCurrentUsername();
        return employeesRepository.findByLogin(currentUsername)
                .filter(Employees::isWorker)
                .orElse(null);
    }

    private Employees findAccountant(){
        String currentUsername = getCurrentUsername();
        return employeesRepository.findByLogin(currentUsername)
                .filter(Employees::isAccountant)
                .orElse(null);
    }

    private Employees findManager(){
        String currentUsername = getCurrentUsername();
        return employeesRepository.findByLogin(currentUsername)
                .filter(Employees::isManager)
                .orElse(null);
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
