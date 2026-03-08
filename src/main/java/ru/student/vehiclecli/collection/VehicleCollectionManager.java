package ru.student.vehiclecli.collection;

import ru.student.vehiclecli.model.FuelType;
import ru.student.vehiclecli.model.Vehicle;
import ru.student.vehiclecli.model.VehicleType;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Iterator;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * In-memory collection manager for vehicles.
 */
public final class VehicleCollectionManager {
    private final HashMap<Long, Vehicle> vehicles = new HashMap<>();
    private final ZonedDateTime initializationDate = ZonedDateTime.now();

    public int size() {
        return vehicles.size();
    }

    public ZonedDateTime getInitializationDate() {
        return initializationDate;
    }

    public Map<Long, Vehicle> snapshot() {
        return new HashMap<>(vehicles);
    }

    public void put(Long key, Vehicle vehicle) {
        validateVehicle(vehicle);
        if (vehicles.containsKey(key)) {
            throw new IllegalArgumentException("Key already exists: " + key);
        }
        ensureUniqueVehicleId(vehicle.getId(), null);
        vehicles.put(key, vehicle);
    }

    public void putAll(Map<Long, Vehicle> data) {
        Objects.requireNonNull(data, "Data map must not be null");
        HashSet<Long> ids = new HashSet<>();
        for (Map.Entry<Long, Vehicle> entry : data.entrySet()) {
            validateVehicle(entry.getValue());
            if (!ids.add(entry.getValue().getId())) {
                throw new IllegalArgumentException("Duplicate vehicle id in map: " + entry.getValue().getId());
            }
        }
        vehicles.clear();
        vehicles.putAll(data);
    }

    public void updateById(long id, Vehicle vehicle) {
        validateVehicle(vehicle);
        Long targetKey = null;
        Vehicle currentVehicle = null;
        boolean found = false;
        for (Map.Entry<Long, Vehicle> entry : vehicles.entrySet()) {
            if (entry.getValue().getId() == id) {
                targetKey = entry.getKey();
                currentVehicle = entry.getValue();
                found = true;
                break;
            }
        }
        if (!found || currentVehicle == null) {
            throw new IllegalArgumentException("Vehicle with id not found: " + id);
        }

        Vehicle replacement = Vehicle.withIdentityAndCreationDate(
                currentVehicle.getId(),
                currentVehicle.getCreationDate(),
                vehicle
        );
        vehicles.put(targetKey, replacement);
    }

    public Vehicle removeKey(Long key) {
        if (!vehicles.containsKey(key)) {
            throw new IllegalArgumentException("Key not found: " + key);
        }
        return vehicles.remove(key);
    }

    public void clear() {
        vehicles.clear();
    }

    public List<Map.Entry<Long, Vehicle>> entriesSortedByVehicle() {
        List<Map.Entry<Long, Vehicle>> entries = new ArrayList<>(vehicles.entrySet());
        entries.sort(Comparator.comparing(Map.Entry<Long, Vehicle>::getValue)
                .thenComparing(entry -> entry.getKey(), VehicleCollectionManager::compareNullableLong));
        return entries;
    }

    public int removeLower(Vehicle reference) {
        validateVehicle(reference);
        int initialSize = vehicles.size();
        vehicles.entrySet().removeIf(entry -> entry.getValue().compareTo(reference) < 0);
        return initialSize - vehicles.size();
    }

    public boolean replaceIfLower(Long key, Vehicle newValue) {
        validateVehicle(newValue);
        Vehicle current = vehicles.get(key);
        if (current == null) {
            throw new IllegalArgumentException("Key not found: " + key);
        }
        Vehicle replacement = Vehicle.withIdentityAndCreationDate(
                current.getId(),
                current.getCreationDate(),
                newValue
        );
        if (newValue.compareTo(current) < 0) {
            vehicles.put(key, replacement);
            return true;
        }
        return false;
    }

    public int removeLowerKey(Long key) {
        int initialSize = vehicles.size();
        vehicles.entrySet().removeIf(entry -> compareNullableLong(entry.getKey(), key) < 0);
        return initialSize - vehicles.size();
    }

    public boolean removeAnyByType(VehicleType type) {
        Objects.requireNonNull(type, "Type must not be null");
        Iterator<Map.Entry<Long, Vehicle>> iterator = vehicles.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, Vehicle> entry = iterator.next();
            if (entry.getValue().getType() == type) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    public List<Vehicle> filterGreaterThanType(VehicleType type) {
        Objects.requireNonNull(type, "Type must not be null");
        return vehicles.values().stream()
                .filter(vehicle -> vehicle.getType().ordinal() > type.ordinal())
                .sorted()
                .collect(Collectors.toList());
    }

    public List<FuelType> fuelTypesAscending() {
        return vehicles.values().stream()
                .map(Vehicle::getFuelType)
                .sorted(Comparator.comparingInt(Enum::ordinal))
                .collect(Collectors.toList());
    }

    private static void validateVehicle(Vehicle vehicle) {
        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle must not be null");
        }
    }

    private static int compareNullableLong(Long left, Long right) {
        if (left == null && right == null) {
            return 0;
        }
        if (left == null) {
            return -1;
        }
        if (right == null) {
            return 1;
        }
        return Long.compare(left, right);
    }

    private void ensureUniqueVehicleId(long id, Long exceptKey) {
        for (Map.Entry<Long, Vehicle> entry : vehicles.entrySet()) {
            if (Objects.equals(entry.getKey(), exceptKey)) {
                continue;
            }
            if (entry.getValue().getId() == id) {
                throw new IllegalArgumentException("Vehicle id must be unique: " + id);
            }
        }
    }
}

