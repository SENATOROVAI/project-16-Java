package ru.student.vehiclecli.collection;

import org.junit.jupiter.api.Test;
import ru.student.vehiclecli.model.Coordinates;
import ru.student.vehiclecli.model.FuelType;
import ru.student.vehiclecli.model.Vehicle;
import ru.student.vehiclecli.model.VehicleType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VehicleCollectionManagerTest {
    @Test
    void shouldInsertAndRemoveByKey() {
        VehicleCollectionManager manager = new VehicleCollectionManager();
        Vehicle vehicle = new Vehicle("a", new Coordinates(1.0, 1), 10F, VehicleType.CAR, FuelType.DIESEL);

        manager.put(1L, vehicle);
        assertEquals(1, manager.size());

        manager.removeKey(1L);
        assertEquals(0, manager.size());
    }

    @Test
    void shouldReplaceIfLower() {
        VehicleCollectionManager manager = new VehicleCollectionManager();
        Vehicle bigger = new Vehicle("b", new Coordinates(1.0, 1), 20F, VehicleType.CAR, FuelType.DIESEL);
        Vehicle lower = new Vehicle("a", new Coordinates(1.0, 1), 10F, VehicleType.CAR, FuelType.DIESEL);
        manager.put(1L, bigger);

        boolean replaced = manager.replaceIfLower(1L, lower);

        assertTrue(replaced);
        assertEquals(10F, manager.snapshot().get(1L).getEnginePower());
    }

    @Test
    void shouldUpdateByVehicleId() {
        VehicleCollectionManager manager = new VehicleCollectionManager();
        Vehicle original = new Vehicle("base", new Coordinates(1.0, 1), 15F, VehicleType.CAR, FuelType.DIESEL);
        manager.put(7L, original);

        Vehicle candidate = new Vehicle("updated", new Coordinates(2.0, 2), 22F, VehicleType.CHOPPER, FuelType.NUCLEAR);
        manager.updateById(original.getId(), candidate);

        Vehicle updated = manager.snapshot().get(7L);
        assertEquals("updated", updated.getName());
        assertEquals(original.getId(), updated.getId());
        assertEquals(original.getCreationDate(), updated.getCreationDate());
    }

    @Test
    void shouldUpdateByVehicleIdWhenStoredUnderNullKey() {
        VehicleCollectionManager manager = new VehicleCollectionManager();
        Vehicle original = new Vehicle("base", new Coordinates(1.0, 1), 15F, VehicleType.CAR, FuelType.DIESEL);
        manager.put(null, original);

        Vehicle candidate = new Vehicle("updated", new Coordinates(2.0, 2), 22F, VehicleType.CHOPPER, FuelType.NUCLEAR);
        manager.updateById(original.getId(), candidate);

        Vehicle updated = manager.snapshot().get(null);
        assertEquals("updated", updated.getName());
        assertEquals(original.getId(), updated.getId());
        assertEquals(original.getCreationDate(), updated.getCreationDate());
    }

    @Test
    void shouldAllowNullKeyForInsertAndRemove() {
        VehicleCollectionManager manager = new VehicleCollectionManager();
        Vehicle vehicle = new Vehicle("n", new Coordinates(1.0, 1), 7F, VehicleType.CAR, FuelType.DIESEL);

        manager.put(null, vehicle);
        assertEquals(1, manager.size());
        manager.removeKey(null);
        assertEquals(0, manager.size());
    }

    @Test
    void shouldRejectDuplicateVehicleId() {
        VehicleCollectionManager manager = new VehicleCollectionManager();
        Vehicle first = Vehicle.restore(100, "a", new Coordinates(1.0, 1), java.time.LocalDate.now(), 5F, VehicleType.CAR, FuelType.DIESEL);
        Vehicle second = Vehicle.restore(100, "b", new Coordinates(2.0, 2), java.time.LocalDate.now(), 6F, VehicleType.CHOPPER, FuelType.NUCLEAR);
        manager.put(1L, first);
        assertThrows(IllegalArgumentException.class, () -> manager.put(2L, second));
    }
}

