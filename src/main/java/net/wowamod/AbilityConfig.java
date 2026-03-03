package net.wowamod.core;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class AbilityConfig {
    // ИСПРАВЛЕНО: Теперь ID совпадает с реальным ID предметов в игре
    public static final String MOD_ID = "universe3090";

    // Изумруды Хаоса
    public static final String EMERALD_GREEN = MOD_ID + ":greenemerald";
    public static final String EMERALD_YELLOW = MOD_ID + ":yellowemerald";
    public static final String EMERALD_CYAN = MOD_ID + ":lightblueemerald";
    public static final String EMERALD_BLUE = MOD_ID + ":blueemerald";
    public static final String EMERALD_PURPLE = MOD_ID + ":purpleemerald";
    public static final String EMERALD_WHITE = MOD_ID + ":whiteemerald";
    public static final String EMERALD_RED = MOD_ID + ":redemerald";

    // Броня Wowabronya
    public static final String WOWA_HELMET = MOD_ID + ":wowabronya_helmet";
    public static final String WOWA_CHEST = MOD_ID + ":wowabronya_chestplate";
    public static final String WOWA_LEGS = MOD_ID + ":wowabronya_leggings";
    public static final String WOWA_BOOTS = MOD_ID + ":wowabronya_boots";

    public static Item getItem(String id) {
        return ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
    }

    public static boolean hasEmerald(Player player, String id) {
        Item target = getItem(id);
        if (target == null) return false;
        
        // Проверка основного инвентаря
        for (ItemStack slot : player.getInventory().items) {
            if (!slot.isEmpty() && slot.getItem() == target) return true;
        }
        
        // Проверка левой руки (Offhand)
        for (ItemStack slot : player.getInventory().offhand) {
            if (!slot.isEmpty() && slot.getItem() == target) return true;
        }
        
        return false;
    }

    public static boolean hasWowaArmor(Player player) {
        // Проверка: надет ли хотя бы один предмет брони Wowabronya
        for (ItemStack slot : player.getArmorSlots()) {
            if (slot.isEmpty()) continue;
            
            ResourceLocation key = ForgeRegistries.ITEMS.getKey(slot.getItem());
            if (key != null) {
                String registryName = key.toString();
                if (registryName.equals(WOWA_HELMET) || 
                    registryName.equals(WOWA_CHEST) || 
                    registryName.equals(WOWA_LEGS) || 
                    registryName.equals(WOWA_BOOTS)) {
                    return true;
                }
            }
        }
        return false;
    }

    // Получить все активные флаги изумрудов
    public static int getEmeraldFlags(Player player) {
        int flags = 0;
        if (hasEmerald(player, EMERALD_GREEN)) flags |= 1;
        if (hasEmerald(player, EMERALD_YELLOW)) flags |= 2;
        if (hasEmerald(player, EMERALD_CYAN)) flags |= 4;
        if (hasEmerald(player, EMERALD_BLUE)) flags |= 8;
        if (hasEmerald(player, EMERALD_PURPLE)) flags |= 16;
        if (hasEmerald(player, EMERALD_WHITE)) flags |= 32;
        if (hasEmerald(player, EMERALD_RED)) flags |= 64;
        return flags;
    }

    public static int[] getEmeraldColor(int flags) {
        if ((flags & 64) != 0) return new int[]{255, 20, 20, 200};   // Красный
        if ((flags & 32) != 0) return new int[]{255, 255, 255, 255}; // Белый (Яркий)
        if ((flags & 16) != 0) return new int[]{180, 0, 255, 200};   // Фиолетовый
        if ((flags & 8) != 0)  return new int[]{0, 0, 255, 200};     // Синий
        if ((flags & 4) != 0)  return new int[]{0, 255, 255, 200};   // Голубой
        if ((flags & 2) != 0)  return new int[]{255, 255, 0, 200};   // Жёлтый
        if ((flags & 1) != 0)  return new int[]{50, 255, 50, 200};   // Зелёный
        
        return new int[]{139, 20, 20, 200}; // Дефолтный (если изумрудов нет)
    }
}