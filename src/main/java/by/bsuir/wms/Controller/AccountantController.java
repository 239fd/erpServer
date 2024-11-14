package by.bsuir.wms.Controller;

import by.bsuir.wms.API.ApiResponse;
import by.bsuir.wms.DTO.*;
import by.bsuir.wms.Service.ProductService;
import com.itextpdf.text.DocumentException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@CrossOrigin(maxAge = 3600L)
@RestController
@RequestMapping("/api/v1/accountant")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ACCOUNTANT')")
public class AccountantController {

    private final ProductService productService;

    @PostMapping("/inventory")
    public ResponseEntity<byte[]> performInventoryCheck(@RequestBody InventoryDTO inventoryDTO) {
        try {
            byte[] pdfContent = productService.performInventoryCheck(inventoryDTO);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "inventory_report.pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfContent);
        } catch (DocumentException | IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/writeoff")
    public ResponseEntity<byte[]> writeOffProduct(@RequestBody WriteOffDTO writeOffDTO) {
        try {
            byte[] pdfContent = productService.writeOffProduct(writeOffDTO);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "write_off_act.pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfContent);
        } catch (DocumentException | IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/revaluation")
    public ResponseEntity<byte[]> revaluateProducts(@RequestBody List<RevaluationDTO> revaluationList) {
        try {
            byte[] pdfContent = productService.revaluateProducts(revaluationList);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "revaluation_act.pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfContent);
        } catch (DocumentException | IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/nonverified")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getAllOrganizations() {
        List<ProductDTO> productDTOS = productService.findNonVerified();
        ApiResponse<List<ProductDTO>> response = ApiResponse.<List<ProductDTO>>builder()
                .data(productDTOS)
                .status(true)
                .message("Non verified products retrieved successfully")
                .build();
        return ResponseEntity.ok(response);

    }

}
