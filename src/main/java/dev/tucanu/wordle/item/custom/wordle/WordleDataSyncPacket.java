package dev.tucanu.wordle.item.custom.wordle;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record WordleDataSyncPacket(CompoundTag tag) {

    public WordleDataSyncPacket(FriendlyByteBuf buf) {
        this(buf.readNbt());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeNbt(tag);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (context.get().getSender() == null) { // Client side
                net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
                if (minecraft.player.containerMenu instanceof WordleMenu menu) {
                    menu.updateFromServer(tag);
                }
            }
        });
        context.get().setPacketHandled(true);
    }
}
