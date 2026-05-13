package com.shop.scheduler;

import com.shop.service.OrderService;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class JobScheduler {
    private static final int CLEANUP_INTERVAL_SECONDS = 15 * 60;
    private static final OrderService orderService = OrderService.getInstance();
    private final ScheduledExecutorService scheduler;

    public JobScheduler() {
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    /**
     * Starts all background jobs.
     */
    public void startJobs() {
        scheduler.scheduleAtFixedRate(
                this::cleanUpPendingOrder,
                0,
                CLEANUP_INTERVAL_SECONDS,
                TimeUnit.SECONDS
        );
    }

    private void cleanUpPendingOrder() {
        try {
            System.out.println("[JobScheduler] Running order cleanup job...");
            orderService.cleanPendingOrders();

        } catch (Exception e) {
            System.err.println("[JobScheduler] Error in order cleanup job:");
            e.printStackTrace();
        }
    }

    /**
     * Gracefully shuts down the scheduler.
     */
    public void shutdown() {
        System.out.println("Shutting down background jobs...");
        scheduler.shutdown();
    }
}