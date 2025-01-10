package me.mourjo.conduit.commons.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerProcessingTime {

    protected static final Logger logger = LoggerFactory.getLogger(ServerProcessingTime.class);
    protected final int DEFAULT_TIME_MS = 1000;
    protected final String FILE_PATH = "../server_processing_time_millis.txt";

    public int readFromFile() {
        Scanner scanner = null;
        int result = DEFAULT_TIME_MS;
        try {
            File file = Paths.get(FILE_PATH).toFile();
            scanner = new Scanner(file);

            if (scanner.hasNextInt()) {
                result = scanner.nextInt();
            }

        } catch (FileNotFoundException e) {
            logger.error("File not found: " + FILE_PATH);
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
        return result;
    }

}
