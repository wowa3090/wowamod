package net.wowamod.procedures;

import net.wowamod.init.Universe3090ModItems;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

@Mod.EventBusSubscriber
public class SoulcombineItemInInventoryTickProcedure {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            execute(event.player);
        }
    }

    public static void execute(Entity entity) {
        if (!(entity instanceof Player player)) {
            return;
        }

        // Не вмешиваемся в способности творческого режима или наблюдателя
        if (player.isCreative() || player.isSpectator()) {
            return;
        }

        // Получаем доступ к NBT данным игрока
        CompoundTag data = player.getPersistentData();

        // Проверка наличия предмета в инвентаре
        boolean hasFlightItem = false;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() == Universe3090ModItems.SOULCOMBINE.get()) {
                hasFlightItem = true;
                break;
            }
        }

        if (hasFlightItem) {
            // Если предмет найден, даем возможность полета (единоразово при активации)
            if (!player.getAbilities().mayfly) {
                player.getAbilities().mayfly = true;
                player.onUpdateAbilities();
                // Маркер того, что полет выдан именно этим модом
                data.putBoolean("SoulcombineFlightActive", true);
            }
            
            // Если игрок поймал предмет во время "безопасного падения", отключаем этот режим
            if (data.getBoolean("SoulcombineGraceFall")) {
                data.remove("SoulcombineGraceFall");
            }
            
        } else {
            // Если предмета нет, но полет был активен (выдан нами)
            if (data.getBoolean("SoulcombineFlightActive")) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.onUpdateAbilities();
                
                data.remove("SoulcombineFlightActive");

                // Если игрок в воздухе, включаем защиту от урона при падении
                if (!player.onGround()) {
                    data.putBoolean("SoulcombineGraceFall", true);
                }
            }

            // Обработка безопасного приземления
            if (data.getBoolean("SoulcombineGraceFall")) {
                // Аннулируем дистанцию падения каждый тик
                player.fallDistance = 0;
                
                // Приземление или попадание в жидкость прекращает действие защиты
                if (player.onGround() || player.isInWater() || player.isInLava()) {
                    data.remove("SoulcombineGraceFall");
                }
            }
        }
    }
}