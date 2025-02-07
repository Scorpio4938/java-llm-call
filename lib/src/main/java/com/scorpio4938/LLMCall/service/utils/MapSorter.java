package com.scorpio4938.LLMCall.service.utils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class for sorting maps.
 * 
 * @since 1.0.0
 */
public class MapSorter {

        /**
         * 
         * Sorts a map by its keys in natural order.
         *
         * @param map The map to sort.
         * @return A new LinkedHashMap with entries sorted by keys.
         * @throws IllegalArgumentException If the map is null.
         */
        public static <K extends Comparable<? super K>, V> Map<K, V> sortByKeys(Map<K, V> map) {
                Validation.notNull(map, "Map");
                return map.entrySet().stream()
                                .sorted(Map.Entry.comparingByKey())
                                .collect(Collectors.toMap(
                                                Map.Entry::getKey,
                                                Map.Entry::getValue,
                                                (oldValue, newValue) -> oldValue, // Merge function (not needed here)
                                                LinkedHashMap::new // Preserve order
                                ));
        }

        /**
         * Sorts a map by its values in natural order.
         *
         * @param map The map to sort.
         * @return A new LinkedHashMap with entries sorted by values.
         * @throws IllegalArgumentException If the map is null.
         */
        public static <K, V extends Comparable<? super V>> Map<K, V> sortByValues(Map<K, V> map) {
                Validation.notNull(map, "Map");
                return map.entrySet().stream()
                                .sorted(Map.Entry.comparingByValue())
                                .collect(Collectors.toMap(
                                                Map.Entry::getKey,
                                                Map.Entry::getValue,
                                                (oldValue, newValue) -> oldValue, // Merge function (not needed here)
                                                LinkedHashMap::new // Preserve order
                                ));
        }

        /**
         * Sorts a map by its keys using a custom comparator.
         *
         * @param map        The map to sort.
         * @param comparator The comparator to use for sorting keys.
         * @return A new LinkedHashMap with entries sorted by keys.
         * @throws IllegalArgumentException If the map or comparator is null.
         */
        public static <K, V> Map<K, V> sortByKeys(Map<K, V> map, Comparator<? super K> comparator) {
                Validation.mapObjectInput(map, comparator, "Comparator");
                return map.entrySet().stream()
                                .sorted(Map.Entry.comparingByKey(comparator))
                                .collect(Collectors.toMap(
                                                Map.Entry::getKey,
                                                Map.Entry::getValue,
                                                (oldValue, newValue) -> oldValue, // Merge function (not needed here)
                                                LinkedHashMap::new // Preserve order
                                ));
        }

        /**
         * Sorts a map by its values using a custom comparator.
         *
         * @param map        The map to sort.
         * @param comparator The comparator to use for sorting values.
         * @return A new LinkedHashMap with entries sorted by values.
         * @throws IllegalArgumentException If the map or comparator is null.
         */
        public static <K, V> Map<K, V> sortByValues(Map<K, V> map, Comparator<? super V> comparator) {
                Validation.mapObjectInput(map, comparator, "Comparator");
                return map.entrySet().stream()
                                .sorted(Map.Entry.comparingByValue(comparator))
                                .collect(Collectors.toMap(
                                                Map.Entry::getKey,
                                                Map.Entry::getValue,
                                                (oldValue, newValue) -> oldValue, // Merge function (not needed here)
                                                LinkedHashMap::new // Preserve order
                                ));
        }
}
