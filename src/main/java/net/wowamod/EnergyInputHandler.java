package net.wowamod.handlers;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.wowamod.core.AbilityConfig;
import net.wowamod.entity.EnergyBallEntity;
import net.wowamod.capability.EnergyCapability;
import net.minecraft.world.level.Level;

public class EnergyInputHandler {
    
    private static final int CHARGE_TIME_TICKS = 40; // Время зарядки (2 секунды)

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        // Проверка клавиши способности (нужно зарегистрировать KeyMapping)
        if (event.getKey() == 34) { // Пример: клавиша V (код 34)
            startCharging();
        }
    }

    @SubscribeEvent
    public static void onMouseInput(InputEvent.MouseButton event) {
        // ПКМ = 1
        if (event.getButton() == 1) {
            Player player = Minecraft.getInstance().player;
            if (player == null) return;

            player.getCapability(EnergyCapability.ENERGY_CAPABILITY).ifPresent(cap -> {
                if (cap.isCharging()) {
                    fireBall(player, cap);
                    cap.setCharging(false);
                    cap.setChargeTimer(0);
                }
            });
        }
    }

    private static void startCharging() {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        // Требования
        if (!player.getItemInHand(InteractionHand.OFF_HAND).isEmpty()) {
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                    "message.wowamod.need_empty_offhand"), true);
            return;
        }

        if (!AbilityConfig.hasWowaArmor(player)) {
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                    "message.wowamod.need_wowa_armor"), true);
            return;
        }

        player.getCapability(EnergyCapability.ENERGY_CAPABILITY).ifPresent(cap -> {
            cap.setCharging(true);
            cap.setChargeTimer(0);
            cap.setEmeraldFlags(AbilityConfig.getEmeraldFlags(player));
        });
    }

    private static void fireBall(Player player, EnergyCapability cap) {
        Level level = player.level();
        int emeraldFlags = cap.getEmeraldFlags();

        // Поиск цели
        LivingEntity target = null;
        HitResult hit = player.pick(64.0, 1.0f, false);
        if (hit.getType() == HitResult.Type.ENTITY) {
            target = (LivingEntity) ((EntityHitResult) hit).getEntity();
        }

        // Создание основного шара
        EnergyBallEntity ball = new EnergyBallEntity(level, player, target, emeraldFlags);
        ball.setPos(player.getX(), player.getEyeHeight() - 0.2, player.getZ());

        Vec3 look = player.getLookAngle();
        float power = (emeraldFlags & 4) != 0 ? 2.5f : 1.5f; // Голубой - быстрее
        ball.shoot(look.x, look.y, look.z, power, 1.0f);
        level.addFreshEntity(ball);

        // Синий изумруд - клоны
        if ((emeraldFlags & 8) != 0) {
            int clones = player.getRandom().nextInt(3) + 3; // 3-5
            for (int i = 0; i < clones; i++) {
                EnergyBallEntity clone = new EnergyBallEntity(level, player, target, emeraldFlags);
                clone.setPos(player.getX(), player.getEyeHeight() - 0.2, player.getZ());
                
                // Разброс вектора для клонов
                Vec3 spread = new Vec3(
                        look.x + (player.getRandom().nextFloat() - 0.5) * 0.3,
                        look.y + (player.getRandom().nextFloat() - 0.5) * 0.3,
                        look.z + (player.getRandom().nextFloat() - 0.5) * 0.3
                );
                clone.shoot(spread.x, spread.y, spread.z, power * 0.9f, 1.0f);
                level.addFreshEntity(clone);
            }
        }

        // Звук выстрела
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                net.minecraft.sounds.SoundEvents.ENDER_EYE_LAUNCH,
                net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    // Метод для проверки состояния зарядки (для рендера и тиков)
    public static boolean isPlayerCharging(Player player) {
        return player.getCapability(EnergyCapability.ENERGY_CAPABILITY)
                .map(cap -> cap.isCharging()).orElse(false);
    }

    public static int getChargeTimer(Player player) {
        return player.getCapability(EnergyCapability.ENERGY_CAPABILITY)
                .map(cap -> cap.getChargeTimer()).orElse(0);
    }
}