package dev.tucanu.wordle.item.custom.wordle;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class WordleLeaderboard {

    public record WordleResult(String playerName, int guesses) {}

    private static final String GLOBAL_KEY = "GooncraftWordleLeaderboard";
    private static final String WORD_KEY = "WordleCurrentWord";

    /** Submit or update a player's result for today */
    public static void submitResult(String playerName, int guesses, String currentWord) {
        CompoundTag global = getGlobalTag();

        // Reset leaderboard if the word changed
        String storedWord = global.getString(WORD_KEY);
        if (!currentWord.equalsIgnoreCase(storedWord)) {
            global = new CompoundTag(); // clear old data
            global.putString(WORD_KEY, currentWord);
        }

        CompoundTag today = new CompoundTag();
        today.putString("playerName", playerName);
        today.putInt("guesses", guesses);

        global.put(playerName, today); // key = player name
        saveGlobalTag(global);
    }

    /** Return a sorted leaderboard of all submissions */
    public static Map<String, WordleResult> getLeaderboard() {
        CompoundTag global = getGlobalTag();
        Map<String, WordleResult> map = new LinkedHashMap<>();

        for (String key : global.getAllKeys()) {
            if (key.equals(WORD_KEY)) continue; // skip word marker
            CompoundTag tag = global.getCompound(key);
            map.put(key, new WordleResult(tag.getString("playerName"), tag.getInt("guesses")));
        }

        // Sort by guesses (failures go last)
        return map.entrySet().stream()
                .sorted((a, b) -> Integer.compare(
                        a.getValue().guesses() == -1 ? 7 : a.getValue().guesses(),
                        b.getValue().guesses() == -1 ? 7 : b.getValue().guesses()
                ))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a,b) -> a,
                        LinkedHashMap::new
                ));
    }

    /** Load global leaderboard tag from server storage */
    private static CompoundTag getGlobalTag() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return new CompoundTag();

        CompoundTag tag = server.getWorldData().getCustomBossEvents().getCompound(GLOBAL_KEY);
        return tag == null ? new CompoundTag() : tag;
    }

    /** Save global leaderboard tag to server storage */
    private static void saveGlobalTag(CompoundTag tag) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        server.getWorldData().getCustomBossEvents().put(GLOBAL_KEY, tag);
    }
}
