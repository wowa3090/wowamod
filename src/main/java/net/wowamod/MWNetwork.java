package net.wowamod.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class MWNetwork {
    private static SimpleChannel INSTANCE;
    private static int packetId = 0;
    private static int id() { return packetId++; }

    private static boolean isRegistered = false;

    public static void register() {
        if (isRegistered) return;
        isRegistered = true;

        // Наш уникальный канал "we_channel"
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation("universe3090", "we_channel")) 
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net;

        // Регистрация нашего пакета
        net.messageBuilder(C2SUpdateEmitterPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(C2SUpdateEmitterPacket::new)
                .encoder(C2SUpdateEmitterPacket::toBytes)
                .consumerMainThread(C2SUpdateEmitterPacket::handle)
                .add();
                
        net.messageBuilder(S2CSyncWavesPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(S2CSyncWavesPacket::new)
                .encoder(S2CSyncWavesPacket::toBytes)
                .consumerMainThread(S2CSyncWavesPacket::handle)
                .add();
                
        net.messageBuilder(C2SDeleteWavePacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(C2SDeleteWavePacket::new)
                .encoder(C2SDeleteWavePacket::toBytes)
                .consumerMainThread(C2SDeleteWavePacket::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        if (INSTANCE != null) {
            INSTANCE.sendToServer(message);
        }
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        if (INSTANCE != null) {
            INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
        }
    }
}