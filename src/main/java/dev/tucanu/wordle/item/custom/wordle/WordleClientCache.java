package dev.tucanu.wordle.item.custom.wordle;

import java.util.LinkedHashMap;
import java.util.Map;

public class WordleClientCache {
    private static final Map<String, Integer> leaderboard = new LinkedHashMap<>();

    public static void setLeaderboard(Map<String, Integer> data) {
        leaderboard.clear();
        leaderboard.putAll(data);
        System.out.println("Leaderboard updated on client: " + leaderboard);
    }

    public static Map<String, Integer> getLeaderboard() {
        return leaderboard;
    }
}
