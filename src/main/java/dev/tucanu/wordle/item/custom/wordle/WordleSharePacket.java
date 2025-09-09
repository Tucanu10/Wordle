package dev.tucanu.wordle.item.custom.wordle;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record WordleSharePacket(String message) {

    public WordleSharePacket(FriendlyByteBuf buf) {
        this(buf.readUtf());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(message);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player != null) {
                // Broadcast the result to all players
                Component component = Component.literal(message);
                player.getServer().getPlayerList().broadcastSystemMessage(component, false);

                // Send word of the day only to the player who shared
                player.sendSystemMessage(
                        Component.literal("Today's word was: " + WordleLogic.getDailyWord())
                );
            }
        });
        context.get().setPacketHandled(true);
    }
}
