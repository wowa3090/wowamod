package net.wowamod.procedures;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.TickEvent;
import net.wowamod.core.AbilityConfig;
import net.wowamod.capability.EnergyCapability;

@Mod.EventBusSubscriber
public class EnergyBallSposobkaProcedure {

    public static void execute(Player player) {
        // Проверка условий
        if (!AbilityConfig.hasWowaArmor(player)) {
            player.displayClientMessage(
                net.minecraft.network.chat.Component.translatable("message.wowamod.need_wowa_armor"), 
                true
            );
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        // Выполняем логику только в конце тика, чтобы избежать двойного срабатывания
        if (event.phase == TickEvent.Phase.END) {
            Player player = event.player;
            Level level = player.level(); // В 1.20.1 метод называется level()

            player.getCapability(EnergyCapability.ENERGY_CAPABILITY).ifPresent(cap -> {
                if (cap.isCharging()) {
                    // Увеличение таймера зарядки
                    int chargeSpeed = 1;
                    
                    // Голубой изумруд - ускорение зарядки на 50%
                    if (AbilityConfig.hasEmerald(player, AbilityConfig.EMERALD_CYAN)) {
                        chargeSpeed = 2;
                    }
                    
                    cap.setChargeTimer(cap.getChargeTimer() + chargeSpeed);

                    // Голубой изумруд - ускорение игрока во время зарядки
                    if (AbilityConfig.hasEmerald(player, AbilityConfig.EMERALD_CYAN)) {
                        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED, 10, 1));
                    }

                    // Проверка завершения зарядки
                    if (cap.getChargeTimer() >= 40) {
                        // Звук готовности
                        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                                net.minecraft.sounds.SoundEvents.BEACON_ACTIVATE,
                                net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 1.5f);
                    }
                }
            });
        }
    }
}