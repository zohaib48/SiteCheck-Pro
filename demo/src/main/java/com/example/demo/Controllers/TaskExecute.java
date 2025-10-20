package com.example.demo.Controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class TaskExecute {

    private final Logger logger = LoggerFactory.getLogger(TaskExecute.class);

    private final ThreadPoolTaskExecutor taskExecutor;
    private final BlockingQueue<Runnable> userTaskQueue = new LinkedBlockingQueue<>();
    private final AtomicInteger enqueuedTaskCount = new AtomicInteger(0);
    private final AtomicInteger completedTaskCount = new AtomicInteger(0);

    private final Map<String, ReentrantLock> sessionLocks = new ConcurrentHashMap<>();

    public static final int MAX_CONCURRENT_REQUESTS = 10;
    public static final int QUEUE_CAPACITY = 4; // Set your desired queue capacity

    public TaskExecute() {
        this.taskExecutor = new ThreadPoolTaskExecutor();
        this.taskExecutor.setCorePoolSize(10); // Set your desired core pool size
        this.taskExecutor.setMaxPoolSize(10); // Set your desired max pool size
        this.taskExecutor.setQueueCapacity(QUEUE_CAPACITY); // Set your desired max pool size
        this.taskExecutor.initialize();
    }

    @PostConstruct
    public void initialize() {
        // Start a background thread to process the task queue
        new Thread(this::processTaskQueue).start();
    }

    public void submitTask(Runnable task, HttpSession session) {
        String sessionId = session.getId();
        ReentrantLock sessionLock = getSessionLock(sessionId);

        sessionLock.lock();
        try {
            int positionInQueue = enqueuedTaskCount.getAndIncrement() - completedTaskCount.get();
            if (!isServerBusy()) {
                userTaskQueue.offer(task);
                logger.info("Task submitted successfully");
            } else {
                userTaskQueue.offer(task);
                logger.warn("Task queue is full. Task is on hold. Position in queue: " + positionInQueue);
                session.setAttribute("error", "Your task is on hold. Please try again later. Your position in the queue is: " + positionInQueue);
            }
        } finally {
            sessionLock.unlock();
        }
    }

    private ReentrantLock getSessionLock(String sessionId) {
        return sessionLocks.computeIfAbsent(sessionId, key -> new ReentrantLock());
    }

    public BlockingQueue<Runnable> getTaskQueue() {
        return userTaskQueue;
    }

    public int getActiveTaskCount() {
        return taskExecutor.getActiveCount();
    }

    public boolean isServerBusy() {
        return getActiveTaskCount() >= MAX_CONCURRENT_REQUESTS;
    }

    public void processTaskQueue() {
        while (true) {
            try {
                Runnable task = userTaskQueue.take();
                logger.info("Executing task from the main queue");
                taskExecutor.execute(() -> {
                    try {
                        task.run();
                    } finally {
                        completedTaskCount.incrementAndGet();
                    }
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Error processing task queue", e);
                break;
            }
        }
    }
}
