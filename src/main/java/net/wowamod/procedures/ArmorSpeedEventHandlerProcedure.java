package net.wowamod.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.wowamod.Universe3090Mod; // Убедитесь, что имя мода совпадает
import net.wowamod.item.Diamond_lapis_armorArmorItem;

// Аннотация @Mod.EventBusSubscriber указывает Forge, что этот класс содержит обработчики событий
// modid - указывает ID вашего мода
// bus = Mod.EventBusSubscriber.Bus.FORGE - указывает, что слушатель будет использовать Forge EventBus
@Mod.EventBusSubscriber(modid = Universe3090Mod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ArmorSpeedEventHandlerProcedure {

    // Обработчик события смены экипировки
    @SubscribeEvent
    public static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        // Проверяем, изменился ли предмет в слоте брони
        if (event.getSlot().getType() == EquipmentSlot.Type.ARMOR) {
            Entity entity = event.getEntity(); // Получаем сущность (игрока)

            // Проверяем, связан ли старый или новый предмет с нашей броней
            boolean oldIsOurArmor = event.getFrom().getItem() instanceof Diamond_lapis_armorArmorItem;
            boolean newIsOurArmor = event.getTo().getItem() instanceof Diamond_lapis_armorArmorItem;

            // Если один из предметов - наша броня, пересчитываем бонус
            if (oldIsOurArmor || newIsOurArmor) {
                // Вызываем нашу процедуру, передав сущность
                ArmorbuffProcedure.execute(entity);
            }
        }
    }
}