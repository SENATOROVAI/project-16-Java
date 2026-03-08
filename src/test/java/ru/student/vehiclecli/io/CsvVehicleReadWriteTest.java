package ru.student.vehiclecli.io;

import org.junit.jupiter.api.Test;
import ru.student.vehiclecli.model.Coordinates;
import ru.student.vehiclecli.model.FuelType;
import ru.student.vehiclecli.model.Vehicle;
import ru.student.vehiclecli.model.VehicleType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CsvVehicleReadWriteTest {
    @Test
    void shouldWriteAndReadBack() throws IOException {
        Path tempFile = Files.createTempFile("vehicles-", ".csv");
        try {
            Map<Long, Vehicle> source = new HashMap<>();
            source.put(1L, Vehicle.restore(10, "Alpha", new Coordinates(1.1, 2), LocalDate.now(), 3.5F, VehicleType.CAR, FuelType.DIESEL));
            source.put(null, Vehicle.restore(11, "Bravo", new Coordinates(2.2, 3), LocalDate.now(), null, VehicleType.CHOPPER, FuelType.NUCLEAR));

            CsvVehicleWriter writer = new CsvVehicleWriter();
            CsvVehicleReader reader = new CsvVehicleReader();
            writer.write(tempFile.toString(), source);

            Map<Long, Vehicle> loaded = reader.read(tempFile.toString());
            assertEquals(2, loaded.size());
            assertEquals("Alpha", loaded.get(1L).getName());
            assertEquals("Bravo", loaded.get(null).getName());
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void shouldRejectInvalidCsv() throws IOException {
        Path tempFile = Files.createTempFile("vehicles-invalid-", ".csv");
        try {
            Files.writeString(tempFile, "key,id,name,coordinatesX,coordinatesY,creationDate,enginePower,type,fuelType\n"
                    + "1,notLong,Name,1.0,1,2026-03-08,1.0,CAR,DIESEL\n");
            CsvVehicleReader reader = new CsvVehicleReader();
            assertThrows(IllegalArgumentException.class, () -> reader.read(tempFile.toString()));
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }
}


