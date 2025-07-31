package dev.velix.imperat.command.tree.suggestions;

/**
 * Jaro-Winkler string similarity algorithm
 */
class JaroWinkler {
    private static final double DEFAULT_THRESHOLD = 0.7;
    private static final double JARO_WINKLER_PREFIX_SIZE = 4;
    
    public static double similarity(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return 0.0;
        }
        
        if (s1.equals(s2)) {
            return 1.0;
        }
        
        int[] matches = matches(s1, s2);
        double matchCount = matches[0];
        
        if (matchCount == 0) {
            return 0.0;
        }
        
        double jaro = (matchCount / s1.length() +
                matchCount / s2.length() +
                (matchCount - matches[1]) / matchCount) / 3.0;
        
        if (jaro < DEFAULT_THRESHOLD) {
            return jaro;
        }
        
        int prefix = commonPrefix(s1, s2);
        return jaro + Math.min(JARO_WINKLER_PREFIX_SIZE, prefix) * 0.1 * (1.0 - jaro);
    }
    
    private static int[] matches(String s1, String s2) {
        String longer = s1.length() > s2.length() ? s1 : s2;
        String shorter = s1.length() > s2.length() ? s2 : s1;
        
        int matchDistance = Math.max(longer.length() / 2 - 1, 1);
        boolean[] shorterMatches = new boolean[shorter.length()];
        boolean[] longerMatches = new boolean[longer.length()];
        
        int matches = 0;
        int transpositions = 0;
        
        for (int i = 0; i < shorter.length(); i++) {
            int start = Math.max(0, i - matchDistance);
            int end = Math.min(i + matchDistance + 1, longer.length());
            
            for (int j = start; j < end; j++) {
                if (longerMatches[j] || shorter.charAt(i) != longer.charAt(j)) {
                    continue;
                }
                shorterMatches[i] = true;
                longerMatches[j] = true;
                matches++;
                break;
            }
        }
        
        if (matches == 0) {
            return new int[]{0, 0};
        }
        
        int k = 0;
        for (int i = 0; i < shorter.length(); i++) {
            if (!shorterMatches[i]) {
                continue;
            }
            while (!longerMatches[k]) {
                k++;
            }
            if (shorter.charAt(i) != longer.charAt(k)) {
                transpositions++;
            }
            k++;
        }
        
        return new int[]{matches, transpositions / 2};
    }
    
    private static int commonPrefix(String s1, String s2) {
        int limit = Math.min(Math.min(s1.length(), s2.length()), 4);
        int i = 0;
        while (i < limit && s1.charAt(i) == s2.charAt(i)) {
            i++;
        }
        return i;
    }
}
