package ru.student.vehiclecli.model;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Monotonic id generator for vehicles.
 */
public final class VehicleIdGenerator {
    private static final AtomicLong COUNTER = new AtomicLong(1);

    private VehicleIdGenerator() {
    }

    public static long nextId() {
        return COUNTER.getAndIncrement();
    }

    public static void registerExistingId(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Vehicle id must be greater than 0");
        }
        COUNTER.updateAndGet(current -> Math.max(current, id + 1));
    }
}


