# FileProcessor - Java Concurrency Exercise

## Overview

This project contains a Java program (`FileProcessor`) designed to read lines from a file and process them concurrently.  
The goal of this exercise is to demonstrate the ability to **analyze existing code, identify concurrency issues, inefficiencies, and bad practices, and propose a robust, production-ready solution**.

---

## Original Code Issues

The original code had several critical problems:

1. **Shared mutable list (`ArrayList`) accessed concurrently**
    - The `lines` list was a static `ArrayList` accessed by multiple threads without synchronization.
    - **Critical**: This can cause **race conditions, data corruption, or `ConcurrentModificationException`**.

2. **Redundant parallel file reading (`for` loop with 10 tasks)**
    - Original code submitted 10 tasks, each reading the **entire file**.
    - **Critical**: Causes duplicated processing, high CPU usage, excessive I/O, and unnecessary memory consumption.

3. **No backpressure or memory control**
    - All lines were submitted immediately as tasks to the executor.
    - **Critical**: Can lead to **OutOfMemoryError** and poor performance with large files.

4. **Premature reporting of results**
    - `System.out.println("Lines processed: " + lines.size())` was executed immediately after `executor.shutdown()`.
    - **Critical**: The reported size may be **inaccurate** because tasks may still be running.

5. **No exception handling per line**
    - Errors during line processing could terminate a thread silently.
    - **Critical**: Data could be lost without logging or visibility.

6. **Poor separation of concerns**
    - File reading, line transformation, and concurrency management were mixed in one block.
    - **Critical**: Difficult to maintain, test, or extend.

---

## Improvements Implemented

The current implementation addresses all these issues:

1. **Producer/Consumer pattern with `BlockingQueue`**
    - One producer reads the file **once** and enqueues lines.
    - Multiple worker threads consume and process lines concurrently.
    - Provides **backpressure**, preventing memory overload.

2. **Thread-safe storage**
    - `ConcurrentLinkedQueue` stores processed lines safely across multiple threads.

3. **Graceful shutdown with configurable timeout**
    - Ensures all tasks complete before termination.

4. **Exception handling per line**
    - Each line that causes an exception is logged using `Logger` without stopping the process.

5. **Separation of responsibilities**
    - `produceLines()` → reads the file and enqueues lines
    - `consumeLines()` → processes lines in parallel
    - `transform()` → contains business logic (currently converting lines to uppercase)

6. **Configurable parameters**
    - Number of workers, queue capacity, and timeout are configurable.

7. **Professional logging and performance tracking**
    - `Logger` used for monitoring and error reporting.
    - Measures **total execution time** for processing, useful in high-volume scenarios.

---


---

## Output Example

1. **After running the `Main` class with a sample file (`data.txt`), the log output could look like this:**

- set. 20, 2025 6:41:46 PM main.java.com.example.fileprocessor.Main main
- INFORMAÇÕES: Lines processed: 10203
- set. 20, 2025 6:41:47 PM main.java.com.example.fileprocessor.Main main
- INFORMAÇÕES: Total time (ms): 258

---
