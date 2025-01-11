package me.mourjo.conduit.commons;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import me.mourjo.conduit.commons.client.GrafanaAnnotationsCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static me.mourjo.conduit.commons.constants.Configuration.*;

public class PropertiesFileReader {

    private static String FILE_PATH = "../conduit_config.properties";
    private final int DEFAULT_CONCURRENCY = 1;
    private final int DEFAULT_SERVER_PROCESSING_TIME_SEC = 4;
    private final Logger logger = LoggerFactory.getLogger(PropertiesFileReader.class);
    private final GrafanaAnnotationsCreator grafanaAnnotationsCreator;
    private final Map<String, Integer> configurations;

    public PropertiesFileReader() {
        grafanaAnnotationsCreator = new GrafanaAnnotationsCreator();
        configurations = new HashMap<>();
    }

    public Properties readFile() throws IOException {
        Properties properties = new Properties();
        properties.load(new FileReader(Paths.get(FILE_PATH).toFile()));
        return properties;
    }

    public int getClientConcurrency() {
        return getInt(CLIENT_CONCURRENCY_CONF_KEY, DEFAULT_CONCURRENCY);
    }

    public int getServerProcessingTimeMillis() {
        return getInt(SERVER_PROCESSING_TIME_CONF_KEY, DEFAULT_SERVER_PROCESSING_TIME_SEC) * 1000;
    }

    private int getInt(String key, int defaultValue) {
        int newValue = defaultValue;
        try {
            var properties = readFile();
            String concurrency = properties.getProperty(key);
            if (concurrency != null) {
                newValue = Integer.parseInt(concurrency);
            }
        } catch (IOException e) {
            logger.error("File could not be read", e);
        }

        synchronized (configurations) {
            if (configurations.containsKey(key)) {
                int previousValue =  configurations.get(key);
                if (newValue > previousValue){
                    grafanaAnnotationsCreator.createAnnotation("%s increased".formatted(key));
                } else if (newValue < previousValue) {
                    grafanaAnnotationsCreator.createAnnotation("%s decreased".formatted(key));
                }
            }
            configurations.put(key, newValue);
        }

        return newValue;
    }
}
