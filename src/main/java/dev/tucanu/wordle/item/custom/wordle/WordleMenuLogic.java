package dev.tucanu.wordle.item.custom.wordle;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WordleMenuLogic {

    public static boolean submitGuess(CompoundTag tag, String guess, ServerPlayer player) {
        List<String> guesses = new ArrayList<>();
        String raw = tag.getString("WordleGuesses");
        if (!raw.isEmpty()) guesses.addAll(Arrays.asList(raw.split(",")));

        if (!tag.getString("WordleState").equalsIgnoreCase(WordleMenu.GameState.PLAYING.name())) {
            return false; // already ended
        }

        String target = tag.getString("WordleTarget");
        guesses.add(guess);
        tag.putString("WordleGuesses", String.join(",", guesses));
        tag.putInt("WordleRow", guesses.size());

        boolean won = guess.equalsIgnoreCase(target);
        if (won) {
            tag.putString("WordleState", WordleMenu.GameState.WON.name());
            WordleLeaderboard.submitResult(player.getName().getString(), guesses.size(), target);
        } else if (guesses.size() >= 6) {
            tag.putString("WordleState", WordleMenu.GameState.LOST.name());
            WordleLeaderboard.submitResult(player.getName().getString(), -1, target);
        }


        return true;
    }
}
