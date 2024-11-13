package by.bsuir.wms.Service;

import by.bsuir.wms.DTO.DispatchDTO;
import by.bsuir.wms.DTO.ProductDTO;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class PDFService {

    private static final String FONT_PATH = "src/main/resources/fonts/arial.ttf";
    private int counter = 1;

    public byte[] generateReceiptOrderPDF(List<ProductDTO> products, List<Integer> ids) throws DocumentException, IOException {
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
        for (int i = 0; i < products.size(); i++) {
            addProductRow(table, products.get(i), rowIndex, regularFont, ids.get(i));
            rowIndex++;
        }

        document.add(table);
        document.close();

        return outputStream.toByteArray();
    }

    public byte[] generateDispatchOrderPDF(List<ProductDTO> products, List<Integer> ids) throws DocumentException, IOException {
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
        for (int i = 0; i < products.size(); i++) {
            addProductRow(table, products.get(i), rowIndex, regularFont, ids.get(i));
            rowIndex++;
        }

        document.add(table);
        document.close();

        return outputStream.toByteArray();
    }

    public byte[] generateTTN(DispatchDTO dispatchDTO, List<ProductDTO> products) throws DocumentException, IOException {
        Document document = new Document(PageSize.A4.rotate());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        PdfWriter.getInstance(document, outputStream);
        document.open();

        BaseFont baseFont = BaseFont.createFont(FONT_PATH, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        Font headerFont = new Font(baseFont, 14, Font.BOLD);
        Font regularFont = new Font(baseFont, 10);

        Paragraph title = new Paragraph("ТОВАРНО-ТРАНСПОРТНАЯ НАКЛАДНАЯ", headerFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        document.add(new Paragraph("Номер: " + dispatchDTO.getDocumentNumber(), regularFont));
        document.add(new Paragraph("Дата: " + dispatchDTO.getDocumentDate(), regularFont));
        document.add(new Paragraph("Автомобиль: " + dispatchDTO.getVehicle(), regularFont));
        document.add(new Paragraph("Водитель: " + dispatchDTO.getDriverName(), regularFont));
        document.add(new Paragraph("Грузоотправитель: " + dispatchDTO.getOrganizationName(), regularFont));
        document.add(new Paragraph("Адрес грузоотправителя: " + dispatchDTO.getOrganizationAddress(), regularFont));
        document.add(new Paragraph("Грузополучатель: " + dispatchDTO.getCustomerName(), regularFont));
        document.add(new Paragraph("Адрес грузополучателя: " + dispatchDTO.getCustomerAddress(), regularFont));

        PdfPTable table = new PdfPTable(10);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setWidths(new float[] {1, 3, 1.5f, 2, 2, 2, 2, 2, 2, 1.5f});

        addTTNTableHeader(table, regularFont);

        int rowIndex = 1;
        for (ProductDTO product : products) {
            addTTNProductRow(table, product, rowIndex, regularFont);
            rowIndex++;
        }

        PdfPCell totalCell = new PdfPCell(new Paragraph("Итого:", regularFont));
        totalCell.setColspan(3);
        totalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(totalCell);

        table.addCell(new PdfPCell(new Paragraph("X", regularFont)));
        table.addCell(new PdfPCell(new Paragraph("X", regularFont)));
        table.addCell(new PdfPCell(new Paragraph("X", regularFont)));
        table.addCell(new PdfPCell(new Paragraph("X", regularFont)));
        table.addCell(new PdfPCell(new Paragraph("", regularFont)));
        table.addCell(new PdfPCell(new Paragraph("", regularFont)));

        document.add(table);
        document.close();

        return outputStream.toByteArray();
    }

    private void addTTNTableHeader(PdfPTable table, Font font) {
        String[] headers = {
                "№", "Наименование товара", "Ед. изм.", "Количество",
                "Цена руб.", "Стоимость руб.", "Ставка НДС %", "Сумма НДС руб.",
                "Стоимость с НДС руб.", "Масса груза, кг"
        };

        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Paragraph(header, font));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPadding(5);
            table.addCell(cell);
        }
    }

    private void addTTNProductRow(PdfPTable table, ProductDTO product, int rowIndex, Font font) {
        table.addCell(new PdfPCell(new Paragraph(String.valueOf(rowIndex), font)));
        table.addCell(new PdfPCell(new Paragraph(product.getName(), font)));
        table.addCell(new PdfPCell(new Paragraph(product.getUnit(), font)));
        table.addCell(new PdfPCell(new Paragraph(String.valueOf(product.getAmount()), font)));
        table.addCell(new PdfPCell(new Paragraph(String.valueOf(product.getPrice()), font)));

        double cost = product.getPrice() * product.getAmount();
        table.addCell(new PdfPCell(new Paragraph(String.format("%.2f", cost), font)));

        double vatRate = 20;
        double vatAmount = cost * vatRate / 100;
        double totalWithVAT = cost + vatAmount;

        table.addCell(new PdfPCell(new Paragraph(String.valueOf(vatRate), font)));
        table.addCell(new PdfPCell(new Paragraph(String.format("%.2f", vatAmount), font)));
        table.addCell(new PdfPCell(new Paragraph(String.format("%.2f", totalWithVAT), font)));
        table.addCell(new PdfPCell(new Paragraph(String.valueOf(product.getWeight()), font)));
    }

    private void addTableHeader(PdfPTable table, Font font) {
        String[] headers = {"Номер","Наимен-ние", "Код", "Наименование ед. изм.",
                "Кол-во по документу", "Кол-во принято", "Цена"};

        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new com.itextpdf.text.Paragraph(header, font));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
    }

    private void addProductRow(PdfPTable table, ProductDTO product, int rowIndex, Font font, int id) {
        table.addCell(new PdfPCell(new com.itextpdf.text.Paragraph(String.valueOf(rowIndex), font)));
        table.addCell(new PdfPCell(new com.itextpdf.text.Paragraph(product.getName(), font)));
        table.addCell(new PdfPCell(new com.itextpdf.text.Paragraph(String.valueOf(id), font)));
        table.addCell(new PdfPCell(new com.itextpdf.text.Paragraph(product.getUnit(), font)));
        table.addCell(new PdfPCell(new com.itextpdf.text.Paragraph(String.valueOf(product.getAmount()), font)));
        table.addCell(new PdfPCell(new com.itextpdf.text.Paragraph(String.valueOf(product.getAmount()), font)));
        table.addCell(new PdfPCell(new com.itextpdf.text.Paragraph(String.valueOf(product.getPrice()), font)));
    }
}
