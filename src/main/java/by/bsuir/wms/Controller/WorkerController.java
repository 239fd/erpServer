package by.bsuir.wms.Controller;

import by.bsuir.wms.DTO.ProductDTO;
import by.bsuir.wms.Service.ProductService;
import com.itextpdf.text.DocumentException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/worker")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_WORKER')")
public class WorkerController {

    private final ProductService productService;

    @PostMapping("/receive")
    public ResponseEntity<byte[]> receiveProducts(@RequestBody List<ProductDTO> products) throws DocumentException, IOException {
        byte[] pdfContent = productService.addProductToCell(products);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"receipt_order.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfContent);
    }

//    @PostMapping("/dispatch")
//    public ResponseEntity<String> dispatchProducts(@RequestParam List<Integer> productIds, @RequestParam String documentFormat, @RequestBody String additionalInfo) {
//        String response = productService.dispatchProducts(productIds, documentFormat, additionalInfo);
//        return ResponseEntity.ok(response);
//    }
}
