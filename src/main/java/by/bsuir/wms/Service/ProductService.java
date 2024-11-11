package by.bsuir.wms.Service;

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
import java.util.List;

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
                System.out.println(dataForSave.size());
                System.out.println(placedProducts.size());
                System.out.println(productDTOs.size());
                if(dataForSave.size()/2 == productDTOs.size()) {
                    for(int i = 0; i < dataForSave.size(); i += 2) {
                        Product product = (Product) dataForSave.get(i);
                        productRepository.save(product);
                        Cell cell = (Cell) dataForSave.get(i + 1);
                        cellRepository.save(cell);
                    }
                }
            }
        }

        return pdfService.generateReceiptOrderPDF(placedProducts);
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
*/