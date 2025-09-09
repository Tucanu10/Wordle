package dev.tucanu.wordle.item.custom.wordle;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public class WordleData {

    private static final String WORDLE_KEY = "GooncraftWordleData";

    // Retrieve or create persistent data for a player
    public static CompoundTag get(ServerPlayer player) {
        CompoundTag persistent = player.getPersistentData();
        if (!persistent.contains(WORDLE_KEY)) {
            persistent.put(WORDLE_KEY, new CompoundTag());
        }
        return persistent.getCompound(WORDLE_KEY);
    }

    // Ensure changes are saved
    public static void save(ServerPlayer player, CompoundTag tag) {
        player.getPersistentData().put(WORDLE_KEY, tag);
    }
}
