package me.mourjo.conduit.commons.client;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrafanaAnnotationsCreator {

    protected static final Logger logger = LoggerFactory.getLogger(GrafanaAnnotationsCreator.class);
    private final String GRAFANA_USERNAME = "admin";
    private final String GRAFANA_PASSWORD = "admin123";
    private final String GRAFANA_ENDPOINT = "http://localhost:3000/api/annotations";
    private final int DASHBOARD_ID = 1;
    HttpClient client = HttpClient.newHttpClient();

    public static void main(String[] args) {
        new GrafanaAnnotationsCreator().createAnnotation("test");
    }

    public void createAnnotation(String text) {
        try {
            long nextTenthSecond = (1 + (System.currentTimeMillis() / 10000)) * 10000;
            var request = buildHttpRequest(text, nextTenthSecond);
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200 && response.statusCode() != 201) {
                logger.error("Error in creating annotation, response code: %s, body: %s".formatted(
                    response.statusCode(), response.body()));
            }
        } catch (Exception e) {
            logger.error("Error in creating annotation", e);
        }
    }

    private HttpRequest buildHttpRequest(String text, long timestamp) {
        String payload = String.format(
            """
                {
                    "dashboardId": %d,
                    "time": %s,
                    "text": "%s"
                }
                """,
            DASHBOARD_ID,
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
