package net.wowamod.handlers;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.wowamod.client.KeyBindings;
import net.wowamod.network.EnergyActionPacket;
import net.wowamod.network.ModMessages;
import net.wowamod.capability.EnergyCapability; // Не забудьте этот импорт
import net.wowamod.core.AbilityConfig;         // И этот
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = "universe3090", value = Dist.CLIENT)
public class EnergyInputHandler {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        // Если нажата кнопка способности
        if (KeyBindings.ENERGY_KEY.consumeClick()) {
            Player player = Minecraft.getInstance().player;
            if (player == null) return;
            
            // 1. Отправляем команду на СЕРВЕР (чтобы он начал считать таймер и спавнить шар)
            ModMessages.sendToServer(new EnergyActionPacket(0)); // 0 = старт

            // 2. [ИСПРАВЛЕНИЕ] Мгновенно включаем зарядку на КЛИЕНТЕ
            // Теперь клиент тоже будет знать, что он заряжается!
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

            // Теперь эта проверка пройдет, так как мы вручную включили зарядку на клиенте выше
            if (net.wowamod.procedures.EnergyBallSposobkaProcedure.isPlayerCharging(player)) {
                 
                 // 1. Отправляем команду на СЕРВЕР (чтобы он заспавнил шар)
                 ModMessages.sendToServer(new EnergyActionPacket(1)); // 1 = выстрел
                 
                 // 2. Сбрасываем зарядку на КЛИЕНТЕ
                 player.getCapability(EnergyCapability.ENERGY_CAPABILITY).ifPresent(cap -> {
                    cap.setCharging(false);
                    cap.setChargeTimer(0);
                 });
            }
        }
    }
}