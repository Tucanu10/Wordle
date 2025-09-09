package dev.tucanu.wordle.item.custom.wordle;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;
import java.util.function.Supplier;

public record WordleLeaderboardRequestPacket() {

    public WordleLeaderboardRequestPacket(FriendlyByteBuf buf) { this(); }
    public void encode(FriendlyByteBuf buf) { }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player == null) return;

            Map<String, WordleLeaderboard.WordleResult> leaderboard = WordleLeaderboard.getLeaderboard();
            if (leaderboard.isEmpty()) {
                player.sendSystemMessage(Component.literal("§6Wordle leaderboard is empty."));
                return;
            }

            player.sendSystemMessage(Component.literal("§6§lToday's Wordle Leaderboard:"));
            int rank = 1;
            for (WordleLeaderboard.WordleResult result : leaderboard.values()) {
                String symbol = result.guesses() == -1 ? "§cX" : getGuessSymbol(result.guesses());
                String line = String.format("§b#%d §f%s §e%s %s", rank, result.playerName(),
                        result.guesses() == -1 ? "" : result.guesses() + "/6", symbol);
                player.sendSystemMessage(Component.literal(line));
                rank++;
            }
        });
        context.get().setPacketHandled(true);
    }

    private String getGuessSymbol(int guessCount) {
        return switch (guessCount) {
            case 1 -> "§6★";
            case 2 -> "§a✦";
            case 3 -> "§b♦";
            case 4 -> "§e●";
            case 5 -> "§7○";
            case 6 -> "§c~";
            default -> "";
        };
    }
}
