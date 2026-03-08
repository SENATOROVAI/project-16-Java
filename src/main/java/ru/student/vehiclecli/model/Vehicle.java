package ru.student.vehiclecli.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Domain entity stored in collection.
 */
public final class Vehicle implements Comparable<Vehicle> {
    private final long id;
    private final String name;
    private final Coordinates coordinates;
    private final LocalDate creationDate;
    private final Float enginePower;
    private final VehicleType type;
    private final FuelType fuelType;

    public Vehicle(String name, Coordinates coordinates, Float enginePower, VehicleType type, FuelType fuelType) {
        this(VehicleIdGenerator.nextId(), name, coordinates, LocalDate.now(), enginePower, type, fuelType);
    }

    public static Vehicle restore(
            long id,
            String name,
            Coordinates coordinates,
            LocalDate creationDate,
            Float enginePower,
            VehicleType type,
            FuelType fuelType
    ) {
        Vehicle restored = new Vehicle(id, name, coordinates, creationDate, enginePower, type, fuelType);
        VehicleIdGenerator.registerExistingId(id);
        return restored;
    }

    public static Vehicle withIdentityAndCreationDate(long id, LocalDate creationDate, Vehicle source) {
        Objects.requireNonNull(source, "Source vehicle must not be null");
        return restore(
                id,
                source.getName(),
                source.getCoordinates(),
                creationDate,
                source.getEnginePower(),
                source.getType(),
                source.getFuelType()
        );
    }

    private Vehicle(
            long id,
            String name,
            Coordinates coordinates,
            LocalDate creationDate,
            Float enginePower,
            VehicleType type,
            FuelType fuelType
    ) {
        validateId(id);
        validateName(name);
        validateCoordinates(coordinates);
        validateCreationDate(creationDate);
        validateEnginePower(enginePower);
        validateType(type);
        validateFuelType(fuelType);

        this.id = id;
        this.name = name;
        this.coordinates = coordinates;
        this.creationDate = creationDate;
        this.enginePower = enginePower;
        this.type = type;
        this.fuelType = fuelType;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public Float getEnginePower() {
        return enginePower;
    }

    public VehicleType getType() {
        return type;
    }

    public FuelType getFuelType() {
        return fuelType;
    }

    @Override
    public int compareTo(Vehicle other) {
        if (other == null) {
            return 1;
        }
        int byPower = compareNullableFloat(this.enginePower, other.enginePower);
        if (byPower != 0) {
            return byPower;
        }
        int byName = this.name.compareTo(other.name);
        if (byName != 0) {
            return byName;
        }
        return Long.compare(this.id, other.id);
    }

    @Override
    public String toString() {
        return "Vehicle{"
                + "id=" + id
                + ", name='" + name + '\''
                + ", coordinates=(" + coordinates.getX() + ", " + coordinates.getY() + ")"
                + ", creationDate=" + creationDate
                + ", enginePower=" + enginePower
                + ", type=" + type
                + ", fuelType=" + fuelType
                + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Vehicle vehicle)) {
            return false;
        }
        return id == vehicle.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    private static int compareNullableFloat(Float left, Float right) {
        if (left == null && right == null) {
            return 0;
        }
        if (left == null) {
            return -1;
        }
        if (right == null) {
            return 1;
        }
        return Float.compare(left, right);
    }

    private static void validateId(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Vehicle id must be greater than 0");
        }
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Vehicle name must not be null or blank");
        }
    }

    private static void validateCoordinates(Coordinates coordinates) {
        if (coordinates == null) {
            throw new IllegalArgumentException("Vehicle coordinates must not be null");
        }
    }

    private static void validateCreationDate(LocalDate creationDate) {
        if (creationDate == null) {
            throw new IllegalArgumentException("Vehicle creationDate must not be null");
        }
    }

    private static void validateEnginePower(Float enginePower) {
        if (enginePower != null && enginePower <= 0F) {
            throw new IllegalArgumentException("Vehicle enginePower must be greater than 0 when not null");
        }
    }

    private static void validateType(VehicleType type) {
        if (type == null) {
            throw new IllegalArgumentException("Vehicle type must not be null");
        }
    }

    private static void validateFuelType(FuelType fuelType) {
        if (fuelType == null) {
            throw new IllegalArgumentException("Vehicle fuelType must not be null");
        }
    }
}


