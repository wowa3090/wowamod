// File: net/wowamod/NetworksyncSoulSystem.java
package net.wowamod;

import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.wowamod.procedures.SoulSystemWProcedure;
import net.wowamod.procedures.SoulSystemWProcedure.ISoulCapability;
import net.wowamod.procedures.SoulSystemWProcedure.SoulType;

import java.util.function.Supplier;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.BiFunction;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class NetworksyncSoulSystem {

    // --- Сетевой пакет для синхронизации данных души ---
    public static class SoulDataSyncMessage {
        private final int determination;
        private final int bravery;
        private final int justice;
        private final int kindness;
        private final int patience;
        private final int integrity;
        private final int perseverance;
        private final boolean soulDetermined;
        private final String currentSoulName;

        public SoulDataSyncMessage(ISoulCapability cap) {
            this.determination = cap.getSoulValue(SoulType.DETERMINATION);
            this.bravery = cap.getSoulValue(SoulType.BRAVERY);
            this.justice = cap.getSoulValue(SoulType.JUSTICE);
            this.kindness = cap.getSoulValue(SoulType.KINDNESS);
            this.patience = cap.getSoulValue(SoulType.PATIENCE);
            this.integrity = cap.getSoulValue(SoulType.INTEGRITY);
            this.perseverance = cap.getSoulValue(SoulType.PERSEVERANCE);
            this.soulDetermined = cap.isSoulDetermined();
            this.currentSoulName = cap.getCurrentSoul().name();
        }

        public SoulDataSyncMessage(FriendlyByteBuf buffer) {
            this.determination = buffer.readInt();
            this.bravery = buffer.readInt();
            this.justice = buffer.readInt();
            this.kindness = buffer.readInt();
            this.patience = buffer.readInt();
            this.integrity = buffer.readInt();
            this.perseverance = buffer.readInt();
            this.soulDetermined = buffer.readBoolean();
            this.currentSoulName = buffer.readUtf();
        }

        public static void buffer(SoulDataSyncMessage message, FriendlyByteBuf buffer) {
            buffer.writeInt(message.determination);
            buffer.writeInt(message.bravery);
            buffer.writeInt(message.justice);
            buffer.writeInt(message.kindness);
            buffer.writeInt(message.patience);
            buffer.writeInt(message.integrity);
            buffer.writeInt(message.perseverance);
            buffer.writeBoolean(message.soulDetermined);
            buffer.writeUtf(message.currentSoulName);
        }

        public static void handler(SoulDataSyncMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            if (context.getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
                context.enqueueWork(() -> {
                    // Обновляем Capability на клиенте
                    // Получаем игрока из Minecraft.getInstance() на клиенте
                    Player player = net.minecraft.client.Minecraft.getInstance().player;
                    if (player != null) {
                        SoulSystemWProcedure.getCapability(player).ifPresent(cap -> {
                            // Устанавливаем флаг
                            cap.setSoulDetermined(message.soulDetermined);
                            // Устанавливаем значения переменных
                            cap.setSoulValue(SoulType.DETERMINATION, message.determination);
                            cap.setSoulValue(SoulType.BRAVERY, message.bravery);
                            cap.setSoulValue(SoulType.JUSTICE, message.justice);
                            cap.setSoulValue(SoulType.KINDNESS, message.kindness);
                            cap.setSoulValue(SoulType.PATIENCE, message.patience);
                            cap.setSoulValue(SoulType.INTEGRITY, message.integrity);
                            cap.setSoulValue(SoulType.PERSEVERANCE, message.perseverance);
                            // Устанавливаем текущую душу
                            try {
                                cap.setCurrentSoul(SoulType.valueOf(message.currentSoulName));
                            } catch (IllegalArgumentException e) {
                                cap.setCurrentSoul(SoulType.NONE);
                            }
                        });
                    }
                });
                context.setPacketHandled(true);
            } else {
                System.err.println("SoulDataSyncMessage received on wrong side: " + context.getDirection());
                context.setPacketHandled(true);
            }
        }
    }

    // --- Регистрация пакета с использованием существующего метода из Universe3090Mod ---
    @SubscribeEvent
    public static void registerMessage(FMLCommonSetupEvent event) {
        // Используем статический метод addNetworkMessage из Universe3090Mod
        Universe3090Mod.addNetworkMessage(
            SoulDataSyncMessage.class,
            SoulDataSyncMessage::buffer,           // BiConsumer<T, FriendlyByteBuf> encoder
            SoulDataSyncMessage::new,              // Function<FriendlyByteBuf, T> decoder
            SoulDataSyncMessage::handler           // BiConsumer<T, Supplier<NetworkEvent.Context>> messageConsumer
        );
    }

    // --- Метод для отправки пакета с сервера клиенту ---
    public static void syncSoulDataToClient(ServerPlayer player) {
        SoulSystemWProcedure.getCapability(player).ifPresent(cap -> {
            SoulDataSyncMessage message = new SoulDataSyncMessage(cap);
            Universe3090Mod.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> player), message);
        });
    }

    // --- Остальная часть вашего класса ---
    public NetworksyncSoulSystem() {
    }

    @SubscribeEvent
    public static void init(FMLCommonSetupEvent event) {
        new NetworksyncSoulSystem();
    }

    @Mod.EventBusSubscriber
    private static class ForgeBusEvents {
        @SubscribeEvent
        public static void serverLoad(ServerStartingEvent event) {
        }

        @OnlyIn(Dist.CLIENT)
        @SubscribeEvent
        public static void clientLoad(FMLClientSetupEvent event) {
        }
    }
}