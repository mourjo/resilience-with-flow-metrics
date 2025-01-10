package me.mourjo.conduit.commons;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertiesFileReader {

    private static String FILE_PATH = "../conduit_config.properties";
    private final int DEFAULT_CONCURRENCY = 1;
    private final int DEFAULT_SERVER_PROCESSING_TIME_SEC = 4;
    private final Logger logger = LoggerFactory.getLogger(PropertiesFileReader.class);

    public Properties readFile() throws IOException {
        Properties properties = new Properties();
        properties.load(new FileReader(Paths.get(FILE_PATH).toFile()));
        return properties;
    }

    public int getClientConcurrency() {
        return getInt("client-concurrency", DEFAULT_CONCURRENCY);
    }

    public int getServerProcessingTimeMillis() {
        return getInt("server-processing-time-sec", DEFAULT_SERVER_PROCESSING_TIME_SEC) * 1000;
    }

    private int getInt(String key, int defaultValue) {
        try {
            var properties = readFile();
            String concurrency = properties.getProperty(key);
            if (concurrency == null) {
                return defaultValue;
            }
            return Integer.parseInt(concurrency);
        } catch (IOException e) {
            logger.error("File could not be read", e);
        }

        return defaultValue;
    }
}
