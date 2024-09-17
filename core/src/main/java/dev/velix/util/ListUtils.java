package dev.velix.util;

import java.util.List;

public class ListUtils {
    
    public static <T> boolean contains(List<T> list, T value) {
        for (T v : list) {
            if (v.equals(value)) return true;
        }
        return false;
    }
    
}
