package main.java.com.example.fileprocessor;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * main.java.fileprocessor.FileProcessor is responsible for processing text files in parallel.
 * Each line is transformed in a thread-safe way and stored in the result queue.
 */
public class FileProcessor {

    private static final Logger logger = Logger.getLogger(FileProcessor.class.getName());
    private final Queue<String> processedLines = new ConcurrentLinkedQueue<>();
    private final int workerCount;
    private final int queueCapacity;
    private final long timeoutMinutes;

    /**
     * Constructor to configure the file processor.
     *
     * @param workerCount    number of threads to process lines
     * @param queueCapacity  maximum size of the line queue
     * @param timeoutMinutes maximum execution time for threads
     * */

    public FileProcessor(int workerCount, int queueCapacity, long timeoutMinutes) {
        this.workerCount = workerCount;
        this.queueCapacity = queueCapacity;
        this.timeoutMinutes = timeoutMinutes;
    }

    /**
     * Processes a file by reading each line and transforming it in parallel.
     *
     * @param filePath path of the file to be processed
     * @throws IOException          if reading the file fails
     * @throws InterruptedException if any thread is interrupted
     */

    public void processFile(String filePath) throws IOException, InterruptedException {
        BlockingQueue<String> queue = new ArrayBlockingQueue<>(queueCapacity);
        AtomicBoolean producerDone = new AtomicBoolean(false);

        ExecutorService workers = Executors.newFixedThreadPool(workerCount);

        for (int i = 0; i < workerCount; i++) {
            workers.submit(() -> consumeLines(queue, producerDone));
        }

        produceLines(filePath, queue, producerDone);
        workers.shutdown();
        if (!workers.awaitTermination(timeoutMinutes, TimeUnit.MINUTES)) {
            workers.shutdownNow();
            throw new RuntimeException("Timeout: workers did not finish in time.");
        }
    }

    /**
     * Reads lines from the file and puts them into the queue for processing.
     */

    private void produceLines(String filePath, BlockingQueue<String> queue, AtomicBoolean producerDone) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(Path.of(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    queue.put(line);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        } finally {
            producerDone.set(true);
        }
    }

    /**
     * Worker that consumes lines from the queue and performs the transformation.
     */

    private void consumeLines(BlockingQueue<String> queue, AtomicBoolean producerDone) {
        try {
            while (!producerDone.get() || !queue.isEmpty()) {
                String line = queue.poll(200, TimeUnit.MILLISECONDS);
                if (line != null) {
                    try {
                        String transformed = transform(line);
                        processedLines.add(transformed);
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "Error processing line: " + line + " - ", e.getMessage());
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.log(Level.WARNING, "Consumer thread interrupted.", e);
        }
    }

    /**
     * Logic to transform a line (convert to uppercase).
     */

    private String transform(String line) {
        return line.toUpperCase();
    }

    /**
     * Returns the number of processed lines.
     */

    public int getProcessedCount() {
        return processedLines.size();
    }
}