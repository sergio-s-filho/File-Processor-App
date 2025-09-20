package main.java.com.example.fileprocessor;

import java.io.IOException;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        // Configure the file processor
        int workerCount = Runtime.getRuntime().availableProcessors(); // use available CPU cores
        int queueCapacity = 50_000; // maximum number of lines in the queue at once
        long timeoutMinutes = 10; // maximum time to wait for all workers to finish

        FileProcessor fileProcessor = new FileProcessor(workerCount, queueCapacity, timeoutMinutes);
        String filePath = "src/main/resources/data.txt";

        try {
            long startTime = System.currentTimeMillis();
            fileProcessor.processFile(filePath);
            long endTime = System.currentTimeMillis();
            logger.info("Lines processed: " + fileProcessor.getProcessedCount());
            logger.info("Total time (ms): " + (endTime - startTime));

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to read the file: " + filePath, e);
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "Processing was interrupted.", e);
            Thread.currentThread().interrupt();
        }
    }
}
