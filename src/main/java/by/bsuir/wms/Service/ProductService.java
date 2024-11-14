package by.bsuir.wms.Service;

import by.bsuir.wms.DTO.*;
import by.bsuir.wms.Entity.*;
import by.bsuir.wms.Entity.Enum.Status;
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

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final RackRepository rackRepository;
    private final CellRepository cellRepository;
    private final EmployeesRepository employeesRepository;
    private final PDFService pdfService;

    public byte[] performInventoryCheck(InventoryDTO inventoryDTO) throws DocumentException, IOException {
        Employees worker = findAccountant();
        Warehouse warehouse = warehouseRepository.findWarehouseByEmployeesId(worker.getId())
                .orElseThrow(() -> new RuntimeException("No warehouse for this user"));

        if (inventoryDTO.getIds().size() != inventoryDTO.getAmounts().size()) {
            throw new AppException("Mistake in input", HttpStatus.BAD_REQUEST);
        }

        List<Product> products = new ArrayList<>();
        Map<Integer, Integer> expectedQuantities = new HashMap<>();

        for (int i = 0; i < inventoryDTO.getIds().size(); i++) {
            Integer productId = inventoryDTO.getIds().get(i);
            Integer expectedAmount = inventoryDTO.getAmounts().get(i);

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("No product found for this discrepancy product"));

            products.add(product);
            expectedQuantities.put(productId, expectedAmount);
        }

        List<Integer> productsIdsOnWarehouse = productRepository.findIdsByCells_Rack_Warehouse(warehouse);

        for (Integer productId : productsIdsOnWarehouse) {
            if (!expectedQuantities.containsKey(productId)) {
                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new RuntimeException("Product not found in warehouse"));
                product.setStatus(Status.nonverified);
                productRepository.save(product);
            }
        }

        List<ProductDTO> productDTOs = products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return pdfService.generateInventoryReport(worker.getOrganization().getName(), warehouse.getName(), productDTOs, inventoryDTO.getAmounts(), inventoryDTO.getIds());
    }

    public byte[] writeOffProduct(WriteOffDTO writeOffDTO) throws DocumentException, IOException {

        Employees worker = findAccountant();
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

        for (int i = 0; i < writeOffDTO.getProductId().size(); i++) {
            products.get(i).setAmount(products.get(i).getAmount() - writeOffDTO.getQuantity().get(i));
            productRepository.save(products.get(i));
        }
        List<ProductDTO> productDTOs = products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return pdfService.generateWriteOffAct(worker.getOrganization().getName(), LocalDate.parse(writeOffDTO.getDate()), "бухгалтер " + worker.getSecondName() + " " + worker.getFirstName(), productDTOs);
    }

    public byte[] revaluateProducts(List<RevaluationDTO> revaluationList) throws DocumentException, IOException {
        List<ProductDTO> updatedProducts = new ArrayList<>();

        Employees worker = findAccountant();
        warehouseRepository.findWarehouseByEmployeesId(worker.getId())
                .orElseThrow(() -> new RuntimeException("No warehouse for this user"));

        for (RevaluationDTO dto : revaluationList) {
            Product product = productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new AppException("Product not found", HttpStatus.NOT_FOUND));

            product.setPrice(dto.getNewPrice());
            productRepository.save(product);

            updatedProducts.add(convertToDTO(product));
        }

        return pdfService.generateRevaluationAct(revaluationList, updatedProducts);
    }

    public byte[] addProductToCell(List<ProductDTO> productDTOs) throws DocumentException, IOException {
        Employees worker = findCurrentWorker();
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
                            .mapToDouble(p -> p.getLength() * p.getWidth() * p.getHeight())
                            .sum();

                    double availableVolume = cellVolume - usedVolume;

                    double productVolume = productDTO.getLength() * productDTO.getWidth() * productDTO.getHeight();

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
                    }
                }
            }
        }
        return pdfService.generateReceiptOrderPDF(placedProducts, ids);
    }

    public Map<String, byte[]> dispatchProducts(DispatchDTO dispatchDTO) throws DocumentException, IOException {

        Employees worker = findCurrentWorker();
        Warehouse warehouse = warehouseRepository.findWarehouseByEmployeesId(worker.getId())
                .orElseThrow(() -> new AppException("No warehouse for this user", HttpStatus.CONFLICT));

        List<Product> products = new ArrayList<>();
        for(int i = 0; i < dispatchDTO.getProductIds().size(); i++) {
            products.add(productRepository.findAllByIdAndCells_Rack_Warehouse(dispatchDTO.getProductIds().get(i), warehouse));
        }

        for (Product value : products) {
            if (value == null) {
                throw new AppException("Mistake with your input", HttpStatus.CONFLICT);
            }
        }
        if (products.isEmpty()) {
            throw new AppException("No products found for dispatch", HttpStatus.CONFLICT);
        }

        List<ProductDTO> productDTOs = products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        boolean check = true;
        for(int i = 0; i < products.size(); i++){
            Product product = products.get(i);
            int dispatchAmount = dispatchDTO.getAmounts().get(i);

            if (product.getAmount() < dispatchAmount) {
                check = false;
                break;
            }
        }

        if(check){
            for (int i = 0; i < products.size(); i++) {
                Product product = products.get(i);
                int dispatchAmount = dispatchDTO.getAmounts().get(i);
                product.setAmount(product.getAmount() - dispatchAmount);

                if (product.getAmount() == 0) {
                    for (Cell cell : product.getCells()) {
                        cell.getProducts().remove(product);
                        cellRepository.save(cell);
                    }
                    product.getCells().clear();
                }
                productRepository.save(product);
            }
        }
        else{
            throw new AppException("Insufficient quantity for one of product", HttpStatus.CONFLICT);
        }


        byte[] orderPDF = pdfService.generateDispatchOrderPDF(productDTOs, dispatchDTO.getProductIds());
        byte[] ttnPDF = pdfService.generateTTN(dispatchDTO, productDTOs);

        Map<String, byte[]> pdfFiles = new HashMap<>();
        pdfFiles.put("dispatch_order.pdf", orderPDF);
        pdfFiles.put("ttn.pdf", ttnPDF);

        return pdfFiles;
    }

    public List<ProductDTO> findNonVerified(){
        Employees worker = findAccountant();
        warehouseRepository.findWarehouseByEmployeesId(worker.getId())
                .orElseThrow(() -> new RuntimeException("No warehouse for this user"));

        return productRepository.findAllByStatus(Status.nonverified).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ProductDTO> getStoredProducts() {
        Employees worker = findCurrentWorker();
        Warehouse warehouse = warehouseRepository.findWarehouseByEmployeesId(worker.getId())
                .orElseThrow(() -> new AppException("No warehouse for this user", HttpStatus.CONFLICT));

        List<Product> productDTOs = productRepository.findAllByCells_Rack_Warehouse(warehouse);
        List<ProductDTO> productDTOList = new ArrayList<>();
        for(Product product : productDTOs){
            productDTOList.add(convertToDTO(product));
        }
        System.out.println(productDTOList.get(0).getBestBeforeDate());
        if(productDTOList.isEmpty()){
            throw new AppException("No products found for current warehouse", HttpStatus.CONFLICT);
        }
        return productDTOList;
    }

    public String getProductLocation(int id){
        Employees worker = findCurrentWorker();
        warehouseRepository.findWarehouseByEmployeesId(worker.getId())
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

    private Employees findCurrentWorker() {
        String currentUsername = getCurrentUsername();
        return employeesRepository.findByLogin(currentUsername)
                .filter(Employees::isWorker)
                .orElseThrow(() -> new RuntimeException("Current worker not found or does not have WORKER role"));
    }

    private Employees findAccountant(){
        String currentUsername = getCurrentUsername();
        return employeesRepository.findByLogin(currentUsername)
                .filter(Employees::isAccountant)
                .orElseThrow(() -> new RuntimeException("Current worker not found or does not have ACCOUNTANT role"));
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
/*
TODO
 1)if from supplier do key
 2)Delete if 0
 3)Store products not like amount
*/