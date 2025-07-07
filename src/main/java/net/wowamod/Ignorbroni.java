package net.wowamod.combat;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.wowamod.init.Universe3090ModItems;
import net.wowamod.item.WowabronyaItem;

@Mod.EventBusSubscriber(modid = "wowamod", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Ignorbroni {
    
    private static final float ARMOR_IGNORE_MULTIPLIER = 0.75f; // Игнорирование 75% брони
    
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!isValidAttack(event)) return;
        
        Player attacker = (Player) event.getSource().getDirectEntity();
        if (shouldApplyArmorIgnore(attacker)) {
            applyArmorIgnore(event);
        }
    }

    private static boolean isValidAttack(LivingHurtEvent event) {
        return event.getSource() != null 
            && event.getSource().getDirectEntity() instanceof Player
            && event.getAmount() > 0;
    }

    private static boolean shouldApplyArmorIgnore(Player attacker) {
        return hasWowASword(attacker) && hasFullWowABronyaSet(attacker);
    }

    private static boolean hasWowASword(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        return !mainHand.isEmpty() 
            && Universe3090ModItems.WOWASWORD.isPresent() 
            && mainHand.getItem() == Universe3090ModItems.WOWASWORD.get();
    }

    private static boolean hasFullWowABronyaSet(Player player) {
        for (EquipmentSlot slot : new EquipmentSlot[]{
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
        }) {
            ItemStack armor = player.getItemBySlot(slot);
            if (armor.isEmpty() || !(armor.getItem() instanceof WowabronyaItem)) {
                return false;
            }
        }
        return true;
    }

    private static void applyArmorIgnore(LivingHurtEvent event) {
        float originalDamage = event.getAmount();
        float modifiedDamage = originalDamage * (1 + ARMOR_IGNORE_MULTIPLIER);
        event.setAmount(modifiedDamage);
    }
}