package net.wowamod.procedures;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.TickEvent;
import net.wowamod.core.AbilityConfig;
import net.wowamod.capability.EnergyCapability;

// ДОБАВЛЕНЫ НЕДОСТАЮЩИЕ ИМПОРТЫ
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

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

    public static boolean isPlayerCharging(Player player) {
        return player.getCapability(EnergyCapability.ENERGY_CAPABILITY)
                .map(cap -> cap.isCharging()).orElse(false);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Player player = event.player;
            Level level = player.level();

            player.getCapability(EnergyCapability.ENERGY_CAPABILITY).ifPresent(cap -> {
                if (cap.isCharging()) {
                    // Обновляем флаги каждый тик
                    int flags = AbilityConfig.getEmeraldFlags(player);
                    cap.setEmeraldFlags(flags);

                    // Базовая скорость: +1 за тик
                    int chargeAmount = 1;
                    
                    // ГОЛУБОЙ ИЗУМРУД: Ускорение набора энергии на 50%
                    if ((flags & 4) != 0) {
                        if (player.tickCount % 2 == 0) {
                            chargeAmount = 2;
                        }
                        // Ускорение игрока (~36-40%)
                        // Теперь ошибки не будет, так как импорты добавлены выше
                        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 5, 1, false, false));
                    }
                    
                    cap.setChargeTimer(cap.getChargeTimer() + chargeAmount);

                    // Проверка завершения
                    if (cap.getChargeTimer() == 40 || cap.getChargeTimer() == 41) {
                        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                                net.minecraft.sounds.SoundEvents.BEACON_ACTIVATE,
                                net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 1.5f);
                        
                    }
                }
            });
        }
    }
}