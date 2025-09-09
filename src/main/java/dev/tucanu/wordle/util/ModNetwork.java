package dev.tucanu.wordle.util;

import dev.tucanu.wordle.Wordle;
import dev.tucanu.wordle.item.custom.wordle.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetwork {
    private static final String PROTOCOL_VERSION = "1";
    private static int packetId = 0;

    public static final SimpleChannel INSTANCE = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(Wordle.MOD_ID, "main"))
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .simpleChannel();

    public static void register() {

        // Message sharing
        INSTANCE.messageBuilder(WordleSharePacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(WordleSharePacket::encode)
                .decoder(WordleSharePacket::new)
                .consumerMainThread(WordleSharePacket::handle)
                .add();

        // Client → Server
        INSTANCE.messageBuilder(WordleGuessPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(WordleGuessPacket::encode)
                .decoder(WordleGuessPacket::new)
                .consumerMainThread(WordleGuessPacket::handle)
                .add();

        // Client → Server (requesting data)
        INSTANCE.messageBuilder(WordleRequestDataPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(WordleRequestDataPacket::encode)
                .decoder(WordleRequestDataPacket::new)
                .consumerMainThread(WordleRequestDataPacket::handle)
                .add();

        // Server → Client (sync data)
        INSTANCE.messageBuilder(WordleDataSyncPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(WordleDataSyncPacket::encode)
                .decoder(WordleDataSyncPacket::new)
                .consumerMainThread(WordleDataSyncPacket::handle)
                .add();

        // Leaderboard Server + Client (sync and request)
        INSTANCE.messageBuilder(WordleLeaderboardRequestPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(WordleLeaderboardRequestPacket::encode)
                .decoder(WordleLeaderboardRequestPacket::new)
                .consumerMainThread(WordleLeaderboardRequestPacket::handle)
                .add();

        INSTANCE.messageBuilder(WordleLeaderboardSyncPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(WordleLeaderboardSyncPacket::encode)
                .decoder(WordleLeaderboardSyncPacket::new)
                .consumerMainThread(WordleLeaderboardSyncPacket::handle)
                .add();
    }
}
