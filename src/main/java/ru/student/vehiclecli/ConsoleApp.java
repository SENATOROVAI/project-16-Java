package ru.student.vehiclecli;

import ru.student.vehiclecli.collection.VehicleCollectionManager;
import ru.student.vehiclecli.io.CollectionFileResolver;
import ru.student.vehiclecli.io.CsvVehicleReader;
import ru.student.vehiclecli.io.CsvVehicleWriter;
import ru.student.vehiclecli.model.Coordinates;
import ru.student.vehiclecli.model.FuelType;
import ru.student.vehiclecli.model.Vehicle;
import ru.student.vehiclecli.model.VehicleType;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Interactive console application entry point.
 */
public final class ConsoleApp {
    private final VehicleCollectionManager manager = new VehicleCollectionManager();
    private final CsvVehicleReader reader = new CsvVehicleReader();
    private final CsvVehicleWriter writer = new CsvVehicleWriter();
    private final Deque<Path> scriptStack = new ArrayDeque<>();
    private String collectionFilePath;
    private boolean running = true;

    public void run() {
        try {
            collectionFilePath = new CollectionFileResolver().resolveFilePath();
        } catch (IllegalStateException ex) {
            System.err.println("Startup error: " + ex.getMessage());
            return;
        }

        loadCollection();
        System.out.println("Collection loaded. Type 'help' to see available commands.");

        Scanner scanner = new Scanner(System.in);
        while (running) {
            System.out.print("> ");
            if (!scanner.hasNextLine()) {
                break;
            }
            String line = scanner.nextLine();
            executeLine(line, scanner::nextLine, true);
        }
    }

    private void loadCollection() {
        try {
            Map<Long, Vehicle> loaded = reader.read(collectionFilePath);
            manager.putAll(loaded);
            System.out.println("Loaded vehicles: " + manager.size());
        } catch (IOException ex) {
            System.err.println("Cannot read collection file: " + ex.getMessage());
            System.err.println("Application starts with empty collection.");
        } catch (RuntimeException ex) {
            System.err.println("Invalid collection file format: " + ex.getMessage());
            System.err.println("Application starts with empty collection.");
        }
    }

    private void executeLine(String rawLine, Supplier<String> nextLineSupplier, boolean interactive) {
        String line = rawLine == null ? "" : rawLine.trim();
        if (line.isBlank()) {
            return;
        }
        String[] parts = line.split("\\s+");
        String command = parts[0].toLowerCase(Locale.ROOT);
        String[] args = Arrays.copyOfRange(parts, 1, parts.length);

        try {
            switch (command) {
                case "help" -> printHelp();
                case "info" -> printInfo();
                case "show" -> printShow();
                case "insert" -> commandInsert(args, nextLineSupplier, interactive);
                case "update" -> commandUpdateById(args, nextLineSupplier, interactive);
                case "remove_key" -> commandRemoveKey(args);
                case "clear" -> manager.clear();
                case "save" -> commandSave();
                case "execute_script" -> commandExecuteScript(args);
                case "exit" -> running = false;
                case "remove_lower" -> commandRemoveLower(nextLineSupplier, interactive);
                case "replace_if_lower" -> commandReplaceIfLower(args, nextLineSupplier, interactive);
                case "remove_lower_key" -> commandRemoveLowerKey(args);
                case "remove_any_by_type" -> commandRemoveAnyByType(args);
                case "filter_greater_than_type" -> commandFilterGreaterThanType(args);
                case "print_field_ascending_fuel_type" -> commandPrintFuelTypes();
                default -> System.err.println("Unknown command: " + command + ". Use 'help'.");
            }
        } catch (Exception ex) {
            System.err.println("Command error: " + ex.getMessage());
        }
    }

    private void printHelp() {
        System.out.println("Available commands:");
        System.out.println("help");
        System.out.println("info");
        System.out.println("show");
        System.out.println("insert <key|null>");
        System.out.println("update <id>");
        System.out.println("remove_key <key>");
        System.out.println("clear");
        System.out.println("save");
        System.out.println("execute_script <file_name>");
        System.out.println("exit");
        System.out.println("remove_lower");
        System.out.println("replace_if_lower <key>");
        System.out.println("remove_lower_key <key>");
        System.out.println("remove_any_by_type <type>");
        System.out.println("filter_greater_than_type <type>");
        System.out.println("print_field_ascending_fuel_type");
    }

    private void printInfo() {
        System.out.println("Collection type: java.util.HashMap<Long, Vehicle>");
        System.out.println("Initialization date: " + manager.getInitializationDate());
        System.out.println("Size: " + manager.size());
    }

    private void printShow() {
        List<Map.Entry<Long, Vehicle>> entries = manager.entriesSortedByVehicle();
        if (entries.isEmpty()) {
            System.out.println("Collection is empty.");
            return;
        }
        for (Map.Entry<Long, Vehicle> entry : entries) {
            System.out.println("key=" + entry.getKey() + " -> " + entry.getValue());
        }
    }

    private void commandInsert(String[] args, Supplier<String> nextLineSupplier, boolean interactive) {
        requireArgCount(args, 1, "insert requires one argument: key or null");
        Long key = parseNullableKey(args[0]);
        Vehicle vehicle = readVehicle(nextLineSupplier, interactive);
        manager.put(key, vehicle);
        System.out.println("Inserted.");
    }

    private void commandUpdateById(String[] args, Supplier<String> nextLineSupplier, boolean interactive) {
        requireArgCount(args, 1, "update requires one argument: id");
        long id = parseId(args[0]);
        Vehicle vehicle = readVehicle(nextLineSupplier, interactive);
        manager.updateById(id, vehicle);
        System.out.println("Updated.");
    }

    private void commandRemoveKey(String[] args) {
        requireArgCount(args, 1, "remove_key requires one argument: key");
        Long key = parseNullableKey(args[0]);
        manager.removeKey(key);
        System.out.println("Removed.");
    }

    private void commandSave() {
        try {
            writer.write(collectionFilePath, manager.snapshot());
            System.out.println("Saved to " + collectionFilePath);
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot save collection: " + ex.getMessage(), ex);
        }
    }

    private void commandExecuteScript(String[] args) {
        requireArgCount(args, 1, "execute_script requires one argument: file_name");
        Path path = Paths.get(args[0]).toAbsolutePath().normalize();
        if (scriptStack.contains(path)) {
            throw new IllegalStateException("Recursive script call detected: " + path);
        }
        scriptStack.push(path);
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(path.toFile()))) {
            List<String> lines = bufferedReader.lines().toList();
            int[] cursor = new int[]{0};
            while (running && cursor[0] < lines.size()) {
                String current = lines.get(cursor[0]++);
                executeLine(current, () -> {
                    if (cursor[0] >= lines.size()) {
                        throw new IllegalStateException("Unexpected end of script while reading command arguments");
                    }
                    return lines.get(cursor[0]++);
                }, false);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot read script file: " + ex.getMessage(), ex);
        } finally {
            scriptStack.pop();
        }
    }

    private void commandRemoveLower(Supplier<String> nextLineSupplier, boolean interactive) {
        Vehicle reference = readVehicle(nextLineSupplier, interactive);
        int removed = manager.removeLower(reference);
        System.out.println("Removed elements: " + removed);
    }

    private void commandReplaceIfLower(String[] args, Supplier<String> nextLineSupplier, boolean interactive) {
        requireArgCount(args, 1, "replace_if_lower requires one argument: key");
        Long key = parseNullableKey(args[0]);
        Vehicle newValue = readVehicle(nextLineSupplier, interactive);
        boolean replaced = manager.replaceIfLower(key, newValue);
        System.out.println(replaced ? "Element replaced." : "Element not replaced because new value is not lower.");
    }

    private void commandRemoveLowerKey(String[] args) {
        requireArgCount(args, 1, "remove_lower_key requires one argument: key");
        Long key = parseNullableKey(args[0]);
        int removed = manager.removeLowerKey(key);
        System.out.println("Removed elements: " + removed);
    }

    private void commandRemoveAnyByType(String[] args) {
        requireArgCount(args, 1, "remove_any_by_type requires one argument: type");
        VehicleType type = parseEnum(args[0], VehicleType.class, "type");
        boolean removed = manager.removeAnyByType(type);
        System.out.println(removed ? "One element removed." : "No elements found for type " + type);
    }

    private void commandFilterGreaterThanType(String[] args) {
        requireArgCount(args, 1, "filter_greater_than_type requires one argument: type");
        VehicleType type = parseEnum(args[0], VehicleType.class, "type");
        List<Vehicle> result = manager.filterGreaterThanType(type);
        if (result.isEmpty()) {
            System.out.println("No elements found.");
            return;
        }
        for (Vehicle vehicle : result) {
            System.out.println(vehicle);
        }
    }

    private void commandPrintFuelTypes() {
        List<FuelType> fuelTypes = manager.fuelTypesAscending();
        if (fuelTypes.isEmpty()) {
            System.out.println("Collection is empty.");
            return;
        }
        for (FuelType fuelType : fuelTypes) {
            System.out.println(fuelType);
        }
    }

    private Vehicle readVehicle(Supplier<String> nextLineSupplier, boolean interactive) {
        String name = readValidatedField(nextLineSupplier, interactive, "name", ConsoleApp::parseName);
        Double x = readValidatedField(nextLineSupplier, interactive, "coordinates.x", value -> parseCoordinateX(value, "coordinates.x"));
        long y = readValidatedField(nextLineSupplier, interactive, "coordinates.y", value -> parseLong(value, "coordinates.y"));
        Float enginePower = readValidatedField(nextLineSupplier, interactive, "enginePower", value -> parseNullableFloat(value, "enginePower"));
        VehicleType type = readValidatedField(nextLineSupplier, interactive, "type", value -> parseEnum(value, VehicleType.class, "type"));
        FuelType fuelType = readValidatedField(nextLineSupplier, interactive, "fuelType", value -> parseEnum(value, FuelType.class, "fuelType"));
        return new Vehicle(name, new Coordinates(x, y), enginePower, type, fuelType);
    }

    private <T> T readValidatedField(
            Supplier<String> nextLineSupplier,
            boolean interactive,
            String fieldName,
            Function<String, T> parser
    ) {
        while (true) {
            if (interactive) {
                if ("type".equals(fieldName)) {
                    System.out.println("Available values for type: " + String.join(", ", enumValues(VehicleType.class)));
                }
                if ("fuelType".equals(fieldName)) {
                    System.out.println("Available values for fuelType: " + String.join(", ", enumValues(FuelType.class)));
                }
                System.out.print(fieldName + ": ");
            }
            String value = readFieldRaw(nextLineSupplier, fieldName);
            try {
                return parser.apply(value);
            } catch (IllegalArgumentException ex) {
                if (!interactive) {
                    throw ex;
                }
                System.err.println(ex.getMessage() + ". Повторите ввод поля.");
            }
        }
    }

    private static String readFieldRaw(Supplier<String> nextLineSupplier, String fieldName) {
        String value = nextLineSupplier.get();
        if (value == null) {
            throw new IllegalStateException("Input stream ended while reading field " + fieldName);
        }
        return value.trim();
    }

    private static Long parseNullableKey(String raw) {
        if ("null".equalsIgnoreCase(raw)) {
            return null;
        }
        return parseRequiredKey(raw);
    }

    private static Long parseRequiredKey(String raw) {
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Key must be long or 'null', got: " + raw, ex);
        }
    }

    private static long parseId(String raw) {
        try {
            long id = Long.parseLong(raw);
            if (id <= 0) {
                throw new IllegalArgumentException("id must be greater than 0");
            }
            return id;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("id must be a long number: " + raw, ex);
        }
    }

    private static long parseLong(String raw, String field) {
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid " + field + ": " + raw, ex);
        }
    }

    private static Double parseDouble(String raw, String field) {
        try {
            return Double.parseDouble(raw);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid " + field + ": " + raw, ex);
        }
    }

    private static Double parseCoordinateX(String raw, String field) {
        Double value = parseDouble(raw, field);
        if (value <= -501) {
            throw new IllegalArgumentException(field + " must be greater than -501");
        }
        return value;
    }

    private static String parseName(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("name must not be null or blank");
        }
        return raw;
    }

    private static Float parseNullableFloat(String raw, String field) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            float value = Float.parseFloat(raw);
            if (value <= 0F) {
                throw new IllegalArgumentException(field + " must be greater than 0");
            }
            return value;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid " + field + ": " + raw, ex);
        }
    }

    private static <E extends Enum<E>> E parseEnum(String raw, Class<E> type, String field) {
        try {
            return Enum.valueOf(type, raw.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid " + field + ": " + raw, ex);
        }
    }

    private static <E extends Enum<E>> String[] enumValues(Class<E> type) {
        return Arrays.stream(type.getEnumConstants()).map(Enum::name).toArray(String[]::new);
    }

    private static void requireArgCount(String[] args, int expected, String errorMessage) {
        if (args.length != expected) {
            throw new IllegalArgumentException(errorMessage);
        }
    }
}


