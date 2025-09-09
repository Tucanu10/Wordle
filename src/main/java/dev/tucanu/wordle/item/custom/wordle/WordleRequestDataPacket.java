package dev.tucanu.wordle.item.custom.wordle;

import dev.tucanu.wordle.util.ModNetwork;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record WordleRequestDataPacket() {

    public WordleRequestDataPacket(FriendlyByteBuf buf) { this(); }
    public void encode(FriendlyByteBuf buf) { /* No data to encode */ }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player != null) {
                CompoundTag tag = WordleData.get(player);
                ModNetwork.INSTANCE.reply(new WordleDataSyncPacket(tag), context.get());
            }
        });
        context.get().setPacketHandled(true);
    }
}

