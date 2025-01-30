package net.wowamod;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.wowamod.item.WowabronyaItem;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Testelementes {

@SubscribeEvent
public static void onPlayerHurt(LivingHurtEvent event) {
    if (event.getEntity() instanceof Player) {
        Player player = (Player) event.getEntity();
        
        boolean hasFullSet = true;
        for (ItemStack stack : player.getInventory().armor) {
            if (stack.isEmpty() || !isWowabronyaArmor(stack)) {
                hasFullSet = false;
                break;
            }
        }
        
        if (hasFullSet) {
            event.setAmount(event.getAmount() * 0.15f); // 85% поглощение
        }
    }
}
private static boolean isWowabronyaArmor(ItemStack stack) {
    return stack.getItem() instanceof WowabronyaItem.Helmet ||
           stack.getItem() instanceof WowabronyaItem.Chestplate ||
           stack.getItem() instanceof WowabronyaItem.Leggings ||
           stack.getItem() instanceof WowabronyaItem.Boots;
}
}