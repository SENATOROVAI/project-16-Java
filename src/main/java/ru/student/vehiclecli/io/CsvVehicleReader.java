package ru.student.vehiclecli.io;

import ru.student.vehiclecli.model.Coordinates;
import ru.student.vehiclecli.model.FuelType;
import ru.student.vehiclecli.model.Vehicle;
import ru.student.vehiclecli.model.VehicleType;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads vehicles from CSV using FileReader.
 */
public final class CsvVehicleReader {
    public Map<Long, Vehicle> read(String filePath) throws IOException {
        HashMap<Long, Vehicle> result = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (lineNumber == 1 && line.startsWith("key,")) {
                    continue;
                }
                if (line.isBlank()) {
                    continue;
                }
                ParsedRecord record = parseRecord(line, lineNumber);
                if (result.containsKey(record.key)) {
                    throw new IllegalArgumentException("Duplicate key in CSV on line " + lineNumber + ": " + record.key);
                }
                result.put(record.key, record.vehicle);
            }
        }
        return result;
    }

    private ParsedRecord parseRecord(String line, int lineNumber) {
        List<String> cells = parseCsvLine(line);
        if (cells.size() != 9) {
            throw new IllegalArgumentException("Invalid CSV column count on line " + lineNumber + ", expected 9");
        }
        Long key = parseNullableLong(cells.get(0), "key", lineNumber);
        long id = parseLong(cells.get(1), "id", lineNumber);
        String name = cells.get(2);
        Double x = parseDouble(cells.get(3), "coordinates.x", lineNumber);
        long y = parseLong(cells.get(4), "coordinates.y", lineNumber);
        LocalDate creationDate = parseLocalDate(cells.get(5), "creationDate", lineNumber);
        Float enginePower = parseNullableFloat(cells.get(6), "enginePower", lineNumber);
        VehicleType type = parseEnum(cells.get(7), VehicleType.class, "type", lineNumber);
        FuelType fuelType = parseEnum(cells.get(8), FuelType.class, "fuelType", lineNumber);

        Coordinates coordinates = new Coordinates(x, y);
        Vehicle vehicle = Vehicle.restore(id, name, coordinates, creationDate, enginePower, type, fuelType);
        return new ParsedRecord(key, vehicle);
    }

    private static List<String> parseCsvLine(String line) {
        List<String> cells = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
                continue;
            }
            if (c == ',' && !inQuotes) {
                cells.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        cells.add(current.toString());
        return cells;
    }

    private static Long parseNullableLong(String raw, String field, int lineNumber) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid " + field + " on line " + lineNumber + ": " + raw, ex);
        }
    }

    private static long parseLong(String raw, String field, int lineNumber) {
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid " + field + " on line " + lineNumber + ": " + raw, ex);
        }
    }

    private static Double parseDouble(String raw, String field, int lineNumber) {
        try {
            return Double.parseDouble(raw.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid " + field + " on line " + lineNumber + ": " + raw, ex);
        }
    }

    private static Float parseNullableFloat(String raw, String field, int lineNumber) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Float.parseFloat(raw.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid " + field + " on line " + lineNumber + ": " + raw, ex);
        }
    }

    private static LocalDate parseLocalDate(String raw, String field, int lineNumber) {
        try {
            return LocalDate.parse(raw.trim());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid " + field + " on line " + lineNumber + ": " + raw, ex);
        }
    }

    private static <E extends Enum<E>> E parseEnum(String raw, Class<E> enumClass, String field, int lineNumber) {
        try {
            return Enum.valueOf(enumClass, raw.trim());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid " + field + " on line " + lineNumber + ": " + raw, ex);
        }
    }

    private record ParsedRecord(Long key, Vehicle vehicle) {
    }
}


