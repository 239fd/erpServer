package by.bsuir.wms.Service;

import by.bsuir.wms.DTO.DispatchDTO;
import by.bsuir.wms.DTO.ProductDTO;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class PDFService {

    private static final String FONT_PATH = "src/main/resources/fonts/arial.ttf";
    private int counter = 1;

    public byte[] generateReceiptOrderPDF(List<ProductDTO> products) throws DocumentException, IOException {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        PdfWriter.getInstance(document, outputStream);
        document.open();

        BaseFont baseFont = BaseFont.createFont(FONT_PATH, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        Font headerFont = new Font(baseFont, 12, Font.BOLD);
        Font regularFont = new Font(baseFont, 10);

        document.add(new com.itextpdf.text.Paragraph("ПРИХОДНЫЙ ОРДЕР № " + counter, headerFont));
        counter++;

        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);

        addTableHeader(table, headerFont);

        int rowIndex = 1;
        for (ProductDTO product : products) {
            addProductRow(table, product, rowIndex, regularFont);
            rowIndex++;
        }

        document.add(table);
        document.close();

        return outputStream.toByteArray();
    }

    public byte[] generateDispatchOrderPDF(List<ProductDTO> products) throws DocumentException, IOException {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        PdfWriter.getInstance(document, outputStream);
        document.open();

        BaseFont baseFont = BaseFont.createFont(FONT_PATH, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        Font headerFont = new Font(baseFont, 12, Font.BOLD);
        Font regularFont = new Font(baseFont, 10);

        document.add(new com.itextpdf.text.Paragraph("ОТПУСКНОЙ ОРДЕР № " + counter, headerFont));
        counter++;

        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);

        addTableHeader(table, headerFont);

        int rowIndex = 1;
        for (ProductDTO product : products) {
            addProductRow(table, product, rowIndex, regularFont);
            rowIndex++;
        }

        document.add(table);
        document.close();

        return outputStream.toByteArray();
    }

    public byte[] generateTTN(DispatchDTO dispatchDTO, List<ProductDTO> products) throws DocumentException, IOException {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        PdfWriter.getInstance(document, outputStream);
        document.open();

        BaseFont baseFont = BaseFont.createFont(FONT_PATH, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        Font headerFont = new Font(baseFont, 12, Font.BOLD);
        Font regularFont = new Font(baseFont, 10);

        document.add(new com.itextpdf.text.Paragraph("ТОВАРНО-ТРАНСПОРТНАЯ НАКЛАДНАЯ", headerFont));
        document.add(new com.itextpdf.text.Paragraph("Номер: " + dispatchDTO.getDocumentNumber(), regularFont));
        document.add(new com.itextpdf.text.Paragraph("Дата: " + dispatchDTO.getDocumentDate(), regularFont));

        document.add(new com.itextpdf.text.Paragraph("Автомобиль: " + dispatchDTO.getVehicle(), regularFont));
        document.add(new com.itextpdf.text.Paragraph("Водитель: " + dispatchDTO.getDriverName(), regularFont));
        document.add(new com.itextpdf.text.Paragraph("Грузоотправитель: " + dispatchDTO.getOrganizationName(), regularFont));
        document.add(new com.itextpdf.text.Paragraph("Адрес грузоотправителя: " + dispatchDTO.getOrganizationAddress(), regularFont));
        document.add(new com.itextpdf.text.Paragraph("Грузополучатель: " + dispatchDTO.getCustomerName(), regularFont));
        document.add(new com.itextpdf.text.Paragraph("Адрес грузополучателя: " + dispatchDTO.getCustomerAddress(), regularFont));

        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);

        addTableHeader(table, headerFont);

        int rowIndex = 1;
        for (ProductDTO product : products) {
            addProductRow(table, product, rowIndex, regularFont);
            rowIndex++;
        }

        document.add(table);
        document.close();

        return outputStream.toByteArray();
    }

    private void addTableHeader(PdfPTable table, Font font) {
        String[] headers = {"Наименование", "Код", "Код (номенкл.)", "Наименование ед. изм.",
                "Кол-во по документу", "Кол-во принято", "Цена"};

        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new com.itextpdf.text.Paragraph(header, font));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
    }
    public byte[] combinePDFs(byte[] pdf1, byte[] pdf2) throws IOException, DocumentException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        com.itextpdf.text.Document document = new com.itextpdf.text.Document();
        PdfWriter writer = PdfWriter.getInstance(document, outputStream);
        document.open();

        PdfReader reader1 = new PdfReader(pdf1);
        PdfReader reader2 = new PdfReader(pdf2);

        PdfContentByte content = writer.getDirectContent();

        for (int i = 1; i <= reader1.getNumberOfPages(); i++) {
            document.newPage();
            content.addTemplate(writer.getImportedPage(reader1, i), 0, 0);
        }

        for (int i = 1; i <= reader2.getNumberOfPages(); i++) {
            document.newPage();
            content.addTemplate(writer.getImportedPage(reader2, i), 0, 0);
        }

        document.close();
        reader1.close();
        reader2.close();

        return outputStream.toByteArray();
    }
    private void addProductRow(PdfPTable table, ProductDTO product, int rowIndex, Font font) {
        table.addCell(new PdfPCell(new com.itextpdf.text.Paragraph(String.valueOf(rowIndex), font)));
        table.addCell(new PdfPCell(new com.itextpdf.text.Paragraph(product.getName(), font)));
        table.addCell(new PdfPCell(new com.itextpdf.text.Paragraph("", font))); // Если нет кода, оставляем пустым
        table.addCell(new PdfPCell(new com.itextpdf.text.Paragraph(product.getUnit(), font)));
        table.addCell(new PdfPCell(new com.itextpdf.text.Paragraph(String.valueOf(product.getAmount()), font)));
        table.addCell(new PdfPCell(new com.itextpdf.text.Paragraph(String.valueOf(product.getAmount()), font)));
        table.addCell(new PdfPCell(new com.itextpdf.text.Paragraph(String.valueOf(product.getPrice()), font)));
    }
}
