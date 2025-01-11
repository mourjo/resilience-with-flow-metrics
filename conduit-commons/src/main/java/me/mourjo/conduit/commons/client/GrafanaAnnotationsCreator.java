package me.mourjo.conduit.commons.client;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrafanaAnnotationsCreator {

    HttpClient client = HttpClient.newHttpClient();
    private final String API_KEY = "glsa_lsDQm65IzGRvkC48C8J0nHhswVW4WQK5_e19b6f33";
    private final String GRAFANA_ENDPOINT = "http://localhost:3000/api/annotations";
    private final int DASHBOARD_ID = 1;

    protected static final Logger logger = LoggerFactory.getLogger(GrafanaAnnotationsCreator.class);

    public void createAnnotation(String text) {
        try {
            var request = buildHttpRequest(text, System.currentTimeMillis());
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200 && response.statusCode() != 201) {
                logger.error("Error in creating annotation, response code: %s, body: %s".formatted(response.statusCode(), response.body()));
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

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(GRAFANA_ENDPOINT))
            .header(AUTHORIZATION, "Bearer " + API_KEY)
            .header(CONTENT_TYPE, "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(payload))
            .build();
        return request;
    }

    public static void main(String[] args) {
        new GrafanaAnnotationsCreator().createAnnotation("test");
    }

}
