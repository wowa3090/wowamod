// ElissClawsSuperAbility.java
package net.wowamod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer; // Добавлен импорт ServerPlayer
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor; // Добавлен импорт
import net.minecraftforge.network.simple.SimpleChannel;
import net.wowamod.init.Universe3090ModItems;

import java.util.Random;
import java.util.function.Supplier;

@EventBusSubscriber(modid = Universe3090Mod.MODID, bus = EventBusSubscriber.Bus.FORGE)
public class ElissClawsSuperAbility {

    // --- Сетевой канал ---
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(Universe3090Mod.MODID, "eliss_claws_dash_channel"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    public static void registerNetwork() {
        int id = 0;
        INSTANCE.registerMessage(id++, StartDashMessage.class, StartDashMessage::encode, StartDashMessage::new, StartDashMessage.Handler::handle);
        INSTANCE.registerMessage(id++, ExecuteAttacksMessage.class, ExecuteAttacksMessage::encode, ExecuteAttacksMessage::new, ExecuteAttacksMessage.Handler::handle);
        INSTANCE.registerMessage(id++, FinishDashMessage.class, FinishDashMessage::encode, FinishDashMessage::new, FinishDashMessage.Handler::handle);
    }

    // --- Серверная логика ---
    @SubscribeEvent
    public static void onPlayerRightClickEntity(PlayerInteractEvent.EntityInteract event) {
        if (event.getHand() != InteractionHand.MAIN_HAND || event.getEntity().level().isClientSide) return;

        Player player = event.getEntity();
        if (isElissClaws(player.getItemInHand(event.getHand())) && event.getTarget() instanceof LivingEntity target) {
            // Отправляем пакет клиенту для начала визуального эффекта
            StartDashMessage msg = new StartDashMessage(target.getId());
            // --- ИСПРАВЛЕНО: Явное приведение Player к ServerPlayer ---
            if (player instanceof ServerPlayer serverPlayer) {
                INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), msg);
            } else {
                // Логика на случай, если Player не является ServerPlayer (хотя в событии на сервере это обычно ServerPlayer)
                // Можно добавить логирование или обработать иначе
                System.err.println("Player is not a ServerPlayer, cannot send packet.");
            }
            event.setCanceled(true); // Отменяем стандартное взаимодействие
        }
    }

    // --- Клиентская логика ---
    @EventBusSubscriber(modid = Universe3090Mod.MODID, bus = EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ClientHandler {

        private static Entity dashTarget = null;
        private static int dashState = 0; // 0: idle, 1: locked, 2: flying
        private static int dashTimer = 0;
        private static final int LOCK_DURATION = 20; // 1 секунда
        private static final int FLY_DURATION = 10; // Продолжительность "полёта"
        private static final Random random = new Random();

        @SubscribeEvent
        @OnlyIn(Dist.CLIENT)
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;

            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null || mc.player == null) return;

            if (dashState == 0) return;

            dashTimer++;

            if (dashState == 1) { // Закреплён
                if (dashTimer >= LOCK_DURATION) {
                    dashState = 2;
                    dashTimer = 0;
                    startFlying(mc.player, mc.level);
                }
            } else if (dashState == 2) { // Полёт
                if (dashTimer >= FLY_DURATION) {
                    finishDash();
                    int numAttacks = 3 + random.nextInt(4); // 3-6 атак
                    ExecuteAttacksMessage attackMsg = new ExecuteAttacksMessage(dashTarget.getId(), numAttacks);
                    INSTANCE.sendToServer(attackMsg);
                    FinishDashMessage finishMsg = new FinishDashMessage();
                    INSTANCE.sendToServer(finishMsg);
                } else {
                    continueFlying(mc.player);
                }
            }
        }

        // Обработка ПКМ по сущности на клиенте (для визуального эффекта)
        @SubscribeEvent
        @OnlyIn(Dist.CLIENT)
        public static void onPlayerRightClickEntityClient(PlayerInteractEvent.EntityInteract event) {
            if (event.getHand() != net.minecraft.world.InteractionHand.MAIN_HAND || !event.getEntity().level().isClientSide) return;

            // --- ИСПРАВЛЕНО: Явное приведение к LocalPlayer ---
            LocalPlayer player = (LocalPlayer) event.getEntity();
            if (isElissClaws(player.getItemInHand(event.getHand())) && event.getTarget() instanceof net.minecraft.world.entity.LivingEntity target) {
                 // Начинаем визуальный налёт на клиенте
                 startDash(target.getId());
                 event.setCanceled(true); // Отменяем стандартное взаимодействие
            }
        }

        public static void startDash(int targetEntityId) {
            Minecraft mc = Minecraft.getInstance();
            ClientLevel level = mc.level;
            if (level == null) return;

            Entity target = level.getEntity(targetEntityId);
            if (target instanceof LivingEntity) {
                dashTarget = target;
                dashState = 1;
                dashTimer = 0;
                level.playSound(mc.player, mc.player.getX(), mc.player.getY(), mc.player.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.5F);
            }
        }

        private static void startFlying(LocalPlayer player, ClientLevel level) {
            level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.ELYTRA_FLYING, SoundSource.PLAYERS, 1.0F, 0.8F);
        }

        private static void continueFlying(LocalPlayer player) {
            if (dashTarget != null) {
                double dx = dashTarget.getX() - player.getX();
                double dy = dashTarget.getY() - player.getY();
                double dz = dashTarget.getZ() - player.getZ();
                double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (distance > 0.5) {
                    double speed = 0.5;
                    player.setPos(player.getX() + dx / distance * speed, player.getY() + dy / distance * speed, player.getZ() + dz / distance * speed);
                }
            }
        }

        public static void playAttackSounds(int numAttacks) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null && mc.player != null) {
                for (int i = 0; i < numAttacks; i++) {
                    mc.level.playSound(mc.player, mc.player.getX(), mc.player.getY(), mc.player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 0.8F + random.nextFloat() * 0.4F);
                }
            }
        }

        public static void finishDash() {
            dashTarget = null;
            dashState = 0;
            dashTimer = 0;
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                // mc.setCameraEntity(mc.player); // Вернуть вид к игроку, если был захвачен
            }
        }
    }

    // --- Вспомогательные методы ---
    private static boolean isElissClaws(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() == Universe3090ModItems.ELISS_CLAWS.get();
    }

    // --- Сетевые сообщения ---
    public static class StartDashMessage {
        private final int targetEntityId;

        public StartDashMessage(int targetEntityId) { this.targetEntityId = targetEntityId; }
        public StartDashMessage(FriendlyByteBuf buf) { this.targetEntityId = buf.readInt(); }
        public void encode(FriendlyByteBuf buf) { buf.writeInt(targetEntityId); }

        public static class Handler {
            public static void handle(final StartDashMessage message, Supplier<NetworkEvent.Context> ctx) {
                ctx.get().enqueueWork(() -> ClientHandler.startDash(message.targetEntityId));
                ctx.get().setPacketHandled(true);
            }
        }
    }

    public static class ExecuteAttacksMessage {
        private final int targetEntityId;
        private final int numberOfAttacks;

        public ExecuteAttacksMessage(int targetEntityId, int numberOfAttacks) {
            this.targetEntityId = targetEntityId;
            this.numberOfAttacks = numberOfAttacks;
        }
        public ExecuteAttacksMessage(FriendlyByteBuf buf) {
            this.targetEntityId = buf.readInt();
            this.numberOfAttacks = buf.readInt();
        }
        public void encode(FriendlyByteBuf buf) {
            buf.writeInt(targetEntityId);
            buf.writeInt(numberOfAttacks);
        }

        public static class Handler {
            public static void handle(final ExecuteAttacksMessage message, Supplier<NetworkEvent.Context> ctx) {
                ctx.get().enqueueWork(() -> ClientHandler.playAttackSounds(message.numberOfAttacks));
                ctx.get().setPacketHandled(true);
            }
        }
    }

    public static class FinishDashMessage {
        public FinishDashMessage() {}
        public FinishDashMessage(FriendlyByteBuf buf) {}
        public void encode(FriendlyByteBuf buf) {}

        public static class Handler {
            public static void handle(final FinishDashMessage message, Supplier<NetworkEvent.Context> ctx) {
                ctx.get().enqueueWork(ClientHandler::finishDash);
                ctx.get().setPacketHandled(true);
            }
        }
    }
}