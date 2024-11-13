package by.bsuir.wms.Controller;

import by.bsuir.wms.DTO.DispatchDTO;
import by.bsuir.wms.DTO.ProductDTO;
import by.bsuir.wms.Service.ProductService;
import by.bsuir.wms.Service.ZIPService;
import com.itextpdf.text.DocumentException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/worker")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_WORKER')")
public class WorkerController {

    private final ProductService productService;
    private final ZIPService zipService;

    @PostMapping("/receive")
    public ResponseEntity<byte[]> receiveProducts(@RequestBody List<ProductDTO> products) throws DocumentException, IOException {
        byte[] pdfContent = productService.addProductToCell(products);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"receipt_order.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfContent);
    }

    @PostMapping("/dispatch")
    public ResponseEntity<byte[]> dispatchProducts(@RequestBody DispatchDTO dispatchDTO) {
        try {

            Map<String, byte[]> pdfFiles = productService.dispatchProducts(dispatchDTO);

            byte[] zipContent = zipService.createZip(pdfFiles);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "dispatch_documents.zip");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(zipContent);
        } catch (DocumentException | IOException e) {
            return ResponseEntity.status(500).body(null);
        }
    }
}
