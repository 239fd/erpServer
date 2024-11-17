package by.bsuir.wms.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class GeoJsonParserService {

    public double parseDistanceFromGeoJson(String geoJsonResponse) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(geoJsonResponse);

            JsonNode featuresNode = rootNode.path("features");
            if (featuresNode.isArray() && !featuresNode.isEmpty()) {
                JsonNode firstFeature = featuresNode.get(0);
                JsonNode propertiesNode = firstFeature.path("properties");
                JsonNode summaryNode = propertiesNode.path("summary");

                return summaryNode.path("distance").asDouble();
            } else {
                throw new RuntimeException("No features found in the response");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error parsing geojson response", e);
        }
    }
}
