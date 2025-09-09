package dev.tucanu.wordle.item.custom.wordle;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;
import java.util.function.Supplier;

public record WordleLeaderboardSyncPacket(Map<String, Integer> leaderboard) {
    public WordleLeaderboardSyncPacket(FriendlyByteBuf buf) {
        this(buf.readMap(FriendlyByteBuf::readUtf, FriendlyByteBuf::readInt));
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeMap(leaderboard, FriendlyByteBuf::writeUtf, FriendlyByteBuf::writeInt);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> WordleClientCache.setLeaderboard(leaderboard));
        context.get().setPacketHandled(true);
    }
}

