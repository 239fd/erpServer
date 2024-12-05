package by.bsuir.wms.Service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;

@Service
public class GeocodingService {

    private static final String GEO_API_URL = "https://geocode-maps.yandex.ru/1.x?apikey=";
    @Value("${api.key.yandex}")
    private String API_KEY;

    public double[] getCoordinatesByAddress(String address) {
        try {

            String url = GEO_API_URL + API_KEY + "&geocode=" + address + "&lang=ru_RU";

            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(url, String.class);

            if (response == null || response.isEmpty()) {
                throw new RuntimeException("Невозможно получить координаты для данного адреса");
            }

            double[] coordinates = new double[2];
            try {
                coordinates = getCoordinatesFromXml(response);

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (coordinates[0] != 0 && coordinates[1] != 0) {
                double lat = Double.parseDouble(String.valueOf(coordinates[1]));
                double lon = Double.parseDouble(String.valueOf(coordinates[0]));

                return new double[]{lat, lon};
            } else {
                throw new RuntimeException("Не найдено координат для данного адреса");
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error parsing geocoding response", e);
        }
    }


    public double[] getCoordinatesFromXml(String xmlResponse) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xmlResponse));
        Document document = builder.parse(is);

        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();

        String expression = "/ymaps/GeoObjectCollection/featureMember/GeoObject/Point/pos";
        String coordinates = (String) xPath.evaluate(expression, document, XPathConstants.STRING);

        if (coordinates != null && !coordinates.isEmpty()) {
            String[] coords = coordinates.split(" ");
            double lon = Double.parseDouble(coords[0]);
            double lat = Double.parseDouble(coords[1]);
            return new double[]{lat, lon};
        } else {
            throw new RuntimeException("No coordinates found in the XML response");
        }
    }
}
