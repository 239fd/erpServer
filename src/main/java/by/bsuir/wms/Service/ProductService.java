package by.bsuir.wms.Service;

import by.bsuir.wms.DTO.DispatchDTO;
import by.bsuir.wms.DTO.ProductDTO;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
                double usedCapacity = 0;
                List<Cell> cells = cellRepository.findAllByRackId(rack.getId());

                for (Cell cell : cells) {
                    for (Product product : cell.getProducts()) {
                        usedCapacity += product.getWeight();
                    }
                }

                double availableCapacity = rack.getCapacity() - usedCapacity;

                if (availableCapacity >= productDTO.getWeight()) {
                    for (Cell cell : cells) {
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
                    if (productPlaced) {
                        break;
                    }
                }
            }

            if (!productPlaced) {
                throw new AppException("No suitable rack or cell found for product: " + productDTO.getName(), HttpStatus.BAD_REQUEST);
            }
            else{
                if(dataForSave.size()/2 == productDTOs.size()) {
                    for(int i = 0; i < dataForSave.size(); i += 2) {
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
    public Employees findCurrentWorker() {
        String currentUsername = getCurrentUsername();
        return employeesRepository.findByLogin(currentUsername)
                .filter(Employees::isWorker)
                .orElseThrow(() -> new RuntimeException("Current worker not found or does not have WORKER role"));
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
 2)A lot of things in one cell
 3)TTN creating
 4)Delete if 0
 5)Store products not like amount
*/