package com.scorpio4938.LLMCall.service.utils;

import java.util.Map;

public class Validation {

    /**
     * Validates that the given object is not null.
     *
     * @param obj        The object to validate.
     * @param objectName The name of the object (for error messages).
     * @throws IllegalArgumentException If the object is null.
     */
    public static void notNull(Object obj, String objectName) {
        if (obj == null) {
            throw new IllegalArgumentException(objectName + " must not be null.");
        }
    }

    /**
     * Validates that the map and the given object are not null.
     *
     * @param map        The map to validate.
     * @param obj        The object to validate.
     * @param objectName The name of the object (for error messages).
     * @throws IllegalArgumentException If the map or object is null.
     */
    public static void mapObjectInput(Map<?, ?> map, Object obj, String objectName) {
        notNull(map, "Map");
        notNull(obj, objectName);
    }
}
