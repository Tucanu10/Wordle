package dev.tucanu.wordle.item.custom.wordle;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record WordleGuessPacket(String guess) {

    public WordleGuessPacket(FriendlyByteBuf buf) {
        this(buf.readUtf());
        System.out.println("DEBUG: WordleGuessPacket decoded: " + this.guess);
    }

    public void encode(FriendlyByteBuf buf) {
        System.out.println("DEBUG: WordleGuessPacket encoded: " + this.guess);
        buf.writeUtf(guess);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        System.out.println("DEBUG: WordleGuessPacket handling started");
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player != null) {
                CompoundTag tag = WordleData.get(player);
                boolean result = WordleMenuLogic.submitGuess(tag, guess, player);
            }
        });
        context.get().setPacketHandled(true);
        System.out.println("DEBUG: WordleGuessPacket handling completed");
    }
}