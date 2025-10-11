package net.wowamod;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.wowamod.init.Universe3090ModItems;

import java.util.Random;
import java.util.function.Supplier;

@EventBusSubscriber(modid = Universe3090Mod.MODID, bus = EventBusSubscriber.Bus.FORGE)
public class ElissClawsSuperAbility {

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
        INSTANCE.registerMessage(id++, PlayAttackSoundsMessage.class, PlayAttackSoundsMessage::encode, PlayAttackSoundsMessage::new, PlayAttackSoundsMessage.Handler::handle);
    }

    /**
     * Новый обработчик активации способности.
     * Срабатывает при нажатии ПКМ с предметом в руке и ищет цель на расстоянии.
     */
    @SubscribeEvent
    public static void onPlayerRightClick(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        // Срабатываем только на сервере и для основной руки
        if (event.getHand() != InteractionHand.MAIN_HAND || player.level().isClientSide()) {
            return;
        }

        if (isElissClaws(player.getItemInHand(event.getHand()))) {
            // Новая увеличенная дистанция работы способности
            double range = 32.0;

            // Ищем сущность, на которую смотрит игрок
            Vec3 eyePosition = player.getEyePosition();
            Vec3 lookVector = player.getViewVector(1.0F);
            Vec3 traceEnd = eyePosition.add(lookVector.x * range, lookVector.y * range, lookVector.z * range);
            AABB searchBox = player.getBoundingBox().expandTowards(lookVector.scale(range)).inflate(1.0D);

            EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(
                    player.level(),
                    player, // Сущность, которую нужно игнорировать
                    eyePosition,
                    traceEnd,
                    searchBox,
                    (entity) -> !entity.isSpectator() && entity.isPickable()
            );

            // Если нашли живую сущность, активируем способность
            if (entityHitResult != null && entityHitResult.getEntity() instanceof LivingEntity target) {
                if (player instanceof ServerPlayer serverPlayer) {
                    INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new StartDashMessage(target.getId()));
                    event.setCanceled(true); // Отменяем стандартное действие предмета
                }
            }
        }
    }


    private static boolean isElissClaws(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() == Universe3090ModItems.ELISS_CLAWS.get();
    }

    @EventBusSubscriber(modid = Universe3090Mod.MODID, bus = EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ClientHandler {
        private static LivingEntity dashTarget = null;
        private static int dashState = 0; // 0: idle, 1: locked, 2: flying
        private static int dashTimer = 0;

        private static final int LOCK_DURATION = 20; // 1 секунда (20 тиков)
        private static final int FLY_DURATION = 20;  // Максимальная длительность полета (1 секунда)
        private static final double DASH_SPEED = 1.8; // Скорость налёта
        private static final Random random = new Random();

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END || dashState == 0) return;

            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null || mc.player == null) {
                finishDash();
                return;
            }

            if (dashTarget == null || !dashTarget.isAlive() || dashTarget.isRemoved()) {
                finishDash();
                return;
            }

            dashTimer++;

            // --- ФАЗА 1: ЗАХВАТ ЦЕЛИ И ЗАКРЕПЛЕНИЕ КАМЕРЫ ---
            if (dashState == 1) {
                // Принудительно поворачиваем камеру игрока на цель
                lockCameraOnTarget(mc.player, dashTarget);

                if (dashTimer >= LOCK_DURATION) {
                    dashState = 2; // Переходим к "полёту"
                    dashTimer = 0;
                    startFlying(mc.player);
                }
            // --- ФАЗА 2: НАЛЁТ НА ЦЕЛЬ ---
            } else if (dashState == 2) {
                // Продолжаем смотреть на цель во время полета
                lockCameraOnTarget(mc.player, dashTarget);
                continueFlying(mc.player);

                // Завершаем налёт, если время вышло или мы очень близко к цели
                if (dashTimer >= FLY_DURATION || mc.player.distanceToSqr(dashTarget) < 4.0) {
                    int numAttacks = 3 + random.nextInt(4); // 3-6 атак
                    INSTANCE.sendToServer(new ExecuteAttacksMessage(dashTarget.getId(), numAttacks));
                    finishDash();
                }
            }
        }
        
        private static void lockCameraOnTarget(Player player, Entity target) {
            Vec3 playerPos = player.getEyePosition();
            Vec3 targetPos = target.getEyePosition();
            Vec3 lookVec = targetPos.subtract(playerPos);

            double horizontalDistance = Math.sqrt(lookVec.x * lookVec.x + lookVec.z * lookVec.z);
            float yaw = (float) (Mth.atan2(lookVec.z, lookVec.x) * (180D / Math.PI)) - 90.0F;
            float pitch = (float) (-(Mth.atan2(lookVec.y, horizontalDistance) * (180D / Math.PI)));
            
            player.setYRot(yaw);
            player.setXRot(pitch);
            player.setYHeadRot(yaw);
        }

        public static void startDash(int targetEntityId) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return;
            Entity target = mc.level.getEntity(targetEntityId);
            if (target instanceof LivingEntity livingTarget) {
                dashTarget = livingTarget;
                dashState = 1;
                dashTimer = 0;
                mc.level.playSound(mc.player, mc.player.getX(), mc.player.getY(), mc.player.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.5F);
            }
        }

        private static void startFlying(Player player) {
            player.level().playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.ELYTRA_FLYING, SoundSource.PLAYERS, 1.0F, 0.8F);
        }

        private static void continueFlying(Player player) {
            if (dashTarget == null) return;
            
            // Вычисляем вектор направления к цели
            Vec3 direction = dashTarget.getEyePosition().subtract(player.getEyePosition()).normalize();
            
            // Устанавливаем скорость игрока, направленную точно на цель
            player.setDeltaMovement(direction.scale(DASH_SPEED));
            player.fallDistance = 0; // Предотвращаем урон от падения
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
            // Сбрасываем все состояния, возвращая управление игроку
            if (dashState != 0) {
                 Minecraft mc = Minecraft.getInstance();
                 if (mc.player != null) {
                     mc.player.setDeltaMovement(0, 0, 0); // Останавливаем движение
                 }
            }
            dashTarget = null;
            dashState = 0;
            dashTimer = 0;
        }
    }

    // --- КЛАССЫ СЕТЕВЫХ СООБЩЕНИЙ ---
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
        public ExecuteAttacksMessage(int targetEntityId, int numberOfAttacks) { this.targetEntityId = targetEntityId; this.numberOfAttacks = numberOfAttacks; }
        public ExecuteAttacksMessage(FriendlyByteBuf buf) { this.targetEntityId = buf.readInt(); this.numberOfAttacks = buf.readInt(); }
        public void encode(FriendlyByteBuf buf) { buf.writeInt(targetEntityId); buf.writeInt(numberOfAttacks); }
        public static class Handler {
            public static void handle(final ExecuteAttacksMessage message, Supplier<NetworkEvent.Context> ctx) {
                ctx.get().enqueueWork(() -> {
                    ServerPlayer player = ctx.get().getSender();
                    if (player == null) return;
                    Entity target = player.level().getEntity(message.targetEntityId);
                    if (target instanceof LivingEntity livingTarget && player.distanceToSqr(target) < 25.0) {
                        DamageSource source = player.level().damageSources().playerAttack(player);
                        float damage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
                        for (int i = 0; i < message.numberOfAttacks; i++) {
                            livingTarget.hurt(source, damage * 0.75F);
                        }
                        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new PlayAttackSoundsMessage(message.numberOfAttacks));
                    }
                });
                ctx.get().setPacketHandled(true);
            }
        }
    }

    public static class PlayAttackSoundsMessage {
        private final int numberOfAttacks;
        public PlayAttackSoundsMessage(int numAttacks) { this.numberOfAttacks = numAttacks; }
        public PlayAttackSoundsMessage(FriendlyByteBuf buf) { this.numberOfAttacks = buf.readInt(); }
        public void encode(FriendlyByteBuf buf) { buf.writeInt(numberOfAttacks); }
        public static class Handler {
            public static void handle(final PlayAttackSoundsMessage message, Supplier<NetworkEvent.Context> ctx) {
                ctx.get().enqueueWork(() -> ClientHandler.playAttackSounds(message.numberOfAttacks));
                ctx.get().setPacketHandled(true);
            }
        }
    }
}

