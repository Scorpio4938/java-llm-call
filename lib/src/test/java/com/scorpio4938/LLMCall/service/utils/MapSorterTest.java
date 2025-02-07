package com.scorpio4938.LLMCall.service.utils;

import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class MapSorterTest {

    @Test
    void testSortByKeys() {
        Map<String, Integer> map = new HashMap<>();
        map.put("banana", 3);
        map.put("apple", 1);
        map.put("cherry", 2);

        Map<String, Integer> sorted = MapSorter.sortByKeys(map);
        
        assertEquals("apple", sorted.keySet().toArray()[0]);
        assertEquals("banana", sorted.keySet().toArray()[1]);
        assertEquals("cherry", sorted.keySet().toArray()[2]);
    }

    @Test
    void testSortByValues() {
        Map<String, Integer> map = new HashMap<>();
        map.put("banana", 3);
        map.put("apple", 1);
        map.put("cherry", 2);

        Map<String, Integer> sorted = MapSorter.sortByValues(map);
        
        assertEquals(1, sorted.get("apple"));
        assertEquals(2, sorted.get("cherry"));
        assertEquals(3, sorted.get("banana"));
    }
} 