package by.bsuir.wms.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;


@Service
@RequiredArgsConstructor
public class RouteService {

    private final GeoJsonParserService geoJsonParserService;

    private static final String API_URL = "https://api.openrouteservice.org/v2/directions/driving-car";

    @Value("${api.key}")
    private String API_KEY;

    public double getDistance(double startLat, double startLon, double endLat, double endLon) {

        String url = UriComponentsBuilder.fromHttpUrl(API_URL)
                .queryParam("api_key", API_KEY)
                .queryParam("start", startLat + "," + startLon)
                .queryParam("end", endLat + "," + endLon)
                .toUriString();

        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            System.out.println(response);
            if (response.getBody() == null || response.getBody().isEmpty()) {
                throw new RuntimeException("Empty response body");
            }
            System.out.println(response.getBody());
            return geoJsonParserService.parseDistanceFromGeoJson(response.getBody());

        } catch (Exception e) {
            throw new RuntimeException("Error while fetching distance from OpenRouteService API", e);
        }
    }
}
