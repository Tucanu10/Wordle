package dev.tucanu.wordle.item.custom.wordle;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class WordleLogic {

    // Words that can be daily targets
    private static final List<String> answerList = new ArrayList<>();

    // All valid guesses (including answers)
    private static final List<String> validWordList = new ArrayList<>();
    private static final Set<String> validWords = new HashSet<>();

    // Daily word management
    public static String dailyWord = null;
    private static LocalDate lastDailyWordDate = null;

    static {
        // Load answers (daily words)
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                WordleLogic.class.getResourceAsStream("/assets/wordle/wordle-answers.txt"),
                StandardCharsets.UTF_8
        ))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim().toUpperCase();
                if (!line.isEmpty()) {
                    answerList.add(line);
                    validWords.add(line); // answers are also valid guesses
                }
            }
        } catch (IOException | NullPointerException e) {
            throw new RuntimeException("Failed to load Wordle answer list!", e);
        }

        // Load all valid guesses (may include answers)
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                WordleLogic.class.getResourceAsStream("/assets/wordle/valid-words.txt"),
                StandardCharsets.UTF_8
        ))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim().toUpperCase();
                if (!line.isEmpty()) {
                    validWordList.add(line);
                    validWords.add(line); // for fast lookup
                }
            }
        } catch (IOException | NullPointerException e) {
            throw new RuntimeException("Failed to load Wordle valid guesses!", e);
        }
    }

    /**
     * Returns today's daily word. Generates a new one if the day has changed.
     */
    public static String getDailyWord() {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());

        if (dailyWord == null || lastDailyWordDate == null || !lastDailyWordDate.equals(today)) {
            long seed = today.toEpochDay();
            Random random = new Random(seed);
            dailyWord = answerList.get(random.nextInt(answerList.size()));
            lastDailyWordDate = today;
        }

        return dailyWord;
    }

    /**
     * Returns true if the word is a valid guess.
     */
    public static boolean isValidWord(String word) {
        return word != null && validWords.contains(word.toUpperCase());
    }

    /**
     * Enum representing the color of letters after a guess.
     */
    public enum LetterColor { GREEN, YELLOW, GRAY }

    /**
     * Checks a guess against the target word and returns the color for each letter.
     */
    public static LetterColor[] checkGuess(String guess, String target) {
        guess = guess.toUpperCase();
        target = target.toUpperCase();
        LetterColor[] result = new LetterColor[guess.length()];
        boolean[] used = new boolean[target.length()];

        // Greens
        for (int i = 0; i < guess.length(); i++) {
            if (i < target.length() && guess.charAt(i) == target.charAt(i)) {
                result[i] = LetterColor.GREEN;
                used[i] = true;
            }
        }

        // Yellows & Grays
        for (int i = 0; i < guess.length(); i++) {
            if (result[i] != null) continue;

            char c = guess.charAt(i);
            boolean found = false;

            for (int j = 0; j < target.length(); j++) {
                if (!used[j] && target.charAt(j) == c) {
                    result[i] = LetterColor.YELLOW;
                    used[j] = true;
                    found = true;
                    break;
                }
            }

            if (!found) result[i] = LetterColor.GRAY;
        }

        return result;
    }

    /**
     * Picks a new random word for a player (for custom games or testing).
     */
    public static void NewWord(ServerPlayer player) {
        CompoundTag tag = WordleData.get(player);

        // Pick a random word from the answer list
        dailyWord = answerList.get(new Random().nextInt(answerList.size()));

        tag.putString("WordleTarget", dailyWord);
        tag.putString("WordleGuesses", "");
        tag.putInt("WordleRow", 0);
        tag.putString("WordleState", WordleMenu.GameState.PLAYING.name());

        WordleData.save(player, tag);

        // Sync to client if menu is open
        if (player.containerMenu instanceof WordleMenu menu) {
            menu.updateFromServer(tag);
        }
    }
}
