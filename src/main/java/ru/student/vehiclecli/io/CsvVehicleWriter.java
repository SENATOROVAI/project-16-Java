package ru.student.vehiclecli.io;

import ru.student.vehiclecli.model.Vehicle;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Writes vehicles to CSV using FileOutputStream.
 */
public final class CsvVehicleWriter {
    public void write(String filePath, Map<Long, Vehicle> vehicles) throws IOException {
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8)) {
            writer.write("key,id,name,coordinatesX,coordinatesY,creationDate,enginePower,type,fuelType");
            writer.write(System.lineSeparator());
            for (Map.Entry<Long, Vehicle> entry : vehicles.entrySet()) {
                Vehicle vehicle = entry.getValue();
                writer.write(escape(entry.getKey() == null ? "" : String.valueOf(entry.getKey())));
                writer.write(',');
                writer.write(String.valueOf(vehicle.getId()));
                writer.write(',');
                writer.write(escape(vehicle.getName()));
                writer.write(',');
                writer.write(String.valueOf(vehicle.getCoordinates().getX()));
                writer.write(',');
                writer.write(String.valueOf(vehicle.getCoordinates().getY()));
                writer.write(',');
                writer.write(String.valueOf(vehicle.getCreationDate()));
                writer.write(',');
                writer.write(vehicle.getEnginePower() == null ? "" : String.valueOf(vehicle.getEnginePower()));
                writer.write(',');
                writer.write(vehicle.getType().name());
                writer.write(',');
                writer.write(vehicle.getFuelType().name());
                writer.write(System.lineSeparator());
            }
        }
    }

    private static String escape(String raw) {
        String value = raw == null ? "" : raw;
        boolean needsQuotes = value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r");
        if (!needsQuotes) {
            return value;
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}


