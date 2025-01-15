package me.mourjo.conduit.commons.client;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrafanaAnnotationsCreator {

    protected static final Logger logger = LoggerFactory.getLogger(GrafanaAnnotationsCreator.class);
    private final String GRAFANA_USERNAME = "admin";
    private final String GRAFANA_PASSWORD = "admin123";
    private final String GRAFANA_ENDPOINT = "http://localhost:3000/api/annotations";
    private final List<Integer> DASHBOARD_IDS = List.of(1,5);
    HttpClient client = HttpClient.newHttpClient();

    public void createAnnotation(String text) {
        long window = 30000;
        long roundedTs = (1 + (System.currentTimeMillis() / window)) * window;
        for (int dashboardId : DASHBOARD_IDS) {
            try {
                var request = buildHttpRequest(dashboardId, text, roundedTs);
                var response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200 && response.statusCode() != 201) {
                    logger.error(
                        "Error in creating annotation, response code: %s, body: %s".formatted(
                            response.statusCode(), response.body())
                    );
                }
            } catch (Exception e) {
                logger.error("Error in creating annotation in dashboard {}", dashboardId, e);
            }
        }
    }

    private HttpRequest buildHttpRequest(int dashboardId, String text, long timestamp) {
        String payload = String.format(
            """
                {
                    "dashboardId": %d,
                    "time": %s,
                    "text": "%s"
                }
                """,
            dashboardId,
            timestamp,
            text
        );

        String auth = "%s:%s".formatted(GRAFANA_USERNAME, GRAFANA_PASSWORD);
        String authToken = Base64.getEncoder()
            .encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        String authHeader = "Basic %s".formatted(authToken);

        return HttpRequest.newBuilder()
            .uri(URI.create(GRAFANA_ENDPOINT))
            .header(AUTHORIZATION, authHeader)
            .header(CONTENT_TYPE, "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(payload))
            .build();
    }

}
