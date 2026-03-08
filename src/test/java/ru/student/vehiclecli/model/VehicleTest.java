package ru.student.vehiclecli.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class VehicleTest {
    @Test
    void shouldRejectBlankName() {
        assertThrows(IllegalArgumentException.class, () ->
                new Vehicle(" ", new Coordinates(1.0, 1), 10F, VehicleType.CAR, FuelType.DIESEL));
    }

    @Test
    void shouldRejectInvalidCoordinatesX() {
        assertThrows(IllegalArgumentException.class, () ->
                new Coordinates(-501.0, 1));
    }

    @Test
    void shouldRejectNonPositiveEnginePower() {
        assertThrows(IllegalArgumentException.class, () ->
                new Vehicle("V", new Coordinates(1.0, 1), 0F, VehicleType.CAR, FuelType.DIESEL));
    }
}


