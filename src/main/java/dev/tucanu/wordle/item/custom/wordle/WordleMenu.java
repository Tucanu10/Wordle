package dev.tucanu.wordle.item.custom.wordle;

import dev.tucanu.wordle.util.ModMenus;
import dev.tucanu.wordle.util.ModNetwork;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.PacketDistributor;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WordleMenu extends AbstractContainerMenu {

    public enum GameState { PLAYING, WON, LOST }

    private final CompoundTag tag;
    private final boolean isServer;
    private final Player player;

    // Client cache
    private List<String> clientGuesses = new ArrayList<>();
    private int clientRow = 0;
    private GameState clientState = GameState.PLAYING;
    private String clientTarget = "";

    public WordleMenu(int windowId, Inventory inv) {
        super(ModMenus.WORDLE_MENU.get(), windowId);

        this.player = inv.player;
        if (player instanceof ServerPlayer serverPlayer) {
            isServer = true;
            tag = WordleData.get(serverPlayer);

            ensureNBT(serverPlayer); // server-authoritative reset
            updateClientCache();

        } else {
            isServer = false;
            tag = new CompoundTag();
            // request sync from server
            ModNetwork.INSTANCE.sendToServer(new WordleRequestDataPacket());
        }

        setupDataSlots();
    }

    private void updateClientCache() {
        clientGuesses = getGuessesFromTag();
        clientRow = tag.getInt("WordleRow");

        try {
            clientState = GameState.valueOf(tag.getString("WordleState"));
        } catch (Exception e) {
            clientState = GameState.PLAYING;
        }

        clientTarget = tag.getString("WordleTarget");
    }

    public void updateFromServer(CompoundTag serverTag) {
        if (!isServer) {
            // Replace instead of merge so no stale values
            tag.getAllKeys().forEach(tag::remove);
            serverTag.getAllKeys().forEach(k -> tag.put(k, serverTag.get(k).copy()));
            updateClientCache();
        }
    }

    private void ensureNBT(ServerPlayer serverPlayer) {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        long todayCode = today.toEpochDay();
        String currentDaily = WordleLogic.getDailyWord();

        boolean needsReset = false;

        // Check if it's a new day or missing data
        if (!tag.contains("WordleDay") || tag.getLong("WordleDay") != todayCode) {
            needsReset = true;
        }

        // Check if the target word has changed (force-reset scenario)
        if (!tag.contains("WordleTarget") || !currentDaily.equalsIgnoreCase(tag.getString("WordleTarget"))) {
            needsReset = true;
        }

        if (needsReset) {
            tag.putLong("WordleDay", todayCode);
            tag.putString("WordleTarget", currentDaily);
            tag.putString("WordleGuesses", "");
            tag.putInt("WordleRow", 0);
            tag.putString("WordleState", GameState.PLAYING.name());

            WordleData.save(serverPlayer, tag);

            // Sync corrected state to client
            ModNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
                    new WordleDataSyncPacket(tag));

        } else if (!tag.contains("WordleState")) {
            // Fallback: only set PLAYING if state is missing
            tag.putString("WordleState", GameState.PLAYING.name());
        }
    }

    private void setupDataSlots() {
        addDataSlot(new DataSlot() {
            @Override public int get() {
                return isServer ? tag.getInt("WordleRow") : clientRow;
            }
            @Override public void set(int value) {
                if (isServer) tag.putInt("WordleRow", value);
                clientRow = value;
            }
        });

        addDataSlot(new DataSlot() {
            @Override public int get() {
                GameState state = isServer ? getState() : clientState;
                return state.ordinal();
            }
            @Override public void set(int value) {
                GameState[] states = GameState.values();
                if (value >= 0 && value < states.length) {
                    if (isServer) tag.putString("WordleState", states[value].name());
                    clientState = states[value];
                }
            }
        });
    }

    public String getTargetWord() {
        return isServer ? tag.getString("WordleTarget") : clientTarget;
    }

    public List<String> getGuesses() {
        return isServer ? getGuessesFromTag() : clientGuesses;
    }

    private List<String> getGuessesFromTag() {
        String raw = tag.getString("WordleGuesses");
        if (raw.isEmpty()) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(raw.split(",")));
    }

    public int getCurrentRow() {
        return isServer ? tag.getInt("WordleRow") : clientRow;
    }

    public GameState getState() {
        try {
            return isServer
                    ? GameState.valueOf(tag.getString("WordleState"))
                    : clientState;
        } catch (IllegalArgumentException e) {
            return GameState.PLAYING;
        }
    }

    /**
     * Called when ENTER is pressed.
     */
    public boolean submitGuess(String guess, Player player) {
        // Client-side handling
        if (!isServer && WordleLogic.isValidWord(guess)) {
            ModNetwork.INSTANCE.sendToServer(new WordleGuessPacket(guess));
            if (clientGuesses.size() == clientRow) {
                clientGuesses.add(guess);
                clientRow++;
            }
            return true;
        }

        // Server-side handling
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }

        boolean valid = WordleMenuLogic.submitGuess(tag, guess, serverPlayer);
        updateClientCache(); // refresh server-side cache too

        return valid;
    }


    @Override public boolean stillValid(Player player) { return true; }
    @Override public ItemStack quickMoveStack(Player player, int i) { return ItemStack.EMPTY; }
}
