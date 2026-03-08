package ru.student.vehiclecli.io;

/**
 * Resolves collection file path from environment variables.
 */
public final class CollectionFileResolver {
    public static final String ENV_NAME = "VEHICLE_COLLECTION_FILE";

    public String resolveFilePath() {
        String value = System.getenv(ENV_NAME);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Environment variable " + ENV_NAME + " is not set");
        }
        return value.trim();
    }
}


