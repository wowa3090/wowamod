package net.wowamod.handlers;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.wowamod.client.KeyBindings;
import net.wowamod.network.EnergyActionPacket;
import net.wowamod.network.ModMessages;
import net.wowamod.capability.EnergyCapability;
import net.wowamod.core.AbilityConfig;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = "universe3090", value = Dist.CLIENT)
public class EnergyInputHandler {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        // Если нажата кнопка способности
        if (KeyBindings.ENERGY_KEY.consumeClick()) {
            Player player = Minecraft.getInstance().player;
            if (player == null) return;
            
            // --- ПРОВЕРКИ НА СТОРОНЕ КЛИЕНТА ---
            
            // 1. Проверяем пустую левую руку
            if (!player.getItemInHand(InteractionHand.OFF_HAND).isEmpty()) {
                player.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                        "message.wowamod.need_empty_offhand"), true);
                return; // Отменяем зарядку
            }

            // 2. Проверяем наличие брони
            if (!AbilityConfig.hasWowaArmor(player)) {
                player.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                        "message.wowamod.need_wowa_armor"), true);
                return; // Отменяем зарядку
            }
            // ------------------------------------

            // Отправляем команду на СЕРВЕР (чтобы он начал считать таймер и спавнить шар)
            ModMessages.sendToServer(new EnergyActionPacket(0)); // 0 = старт

            // Мгновенно включаем зарядку на КЛИЕНТЕ (для красивой отрисовки)
            player.getCapability(EnergyCapability.ENERGY_CAPABILITY).ifPresent(cap -> {
                cap.setCharging(true);
                cap.setChargeTimer(0);
                cap.setEmeraldFlags(AbilityConfig.getEmeraldFlags(player));
            });
        }
    }

    @SubscribeEvent
    public static void onMouseInput(InputEvent.MouseButton event) {
        if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_RIGHT && event.getAction() == GLFW.GLFW_PRESS) {
            Player player = Minecraft.getInstance().player;
            if (player == null) return;

            // Если игрок в режиме зарядки
            if (net.wowamod.procedures.EnergyBallSposobkaProcedure.isPlayerCharging(player)) {
                 
                 // Отправляем команду на СЕРВЕР на выстрел
                 ModMessages.sendToServer(new EnergyActionPacket(1)); // 1 = выстрел
                 
                 // Сбрасываем зарядку на КЛИЕНТЕ, чтобы шар пропал из руки
                 player.getCapability(EnergyCapability.ENERGY_CAPABILITY).ifPresent(cap -> {
                    cap.setCharging(false);
                    cap.setChargeTimer(0);
                 });
            }
        }
    }
}