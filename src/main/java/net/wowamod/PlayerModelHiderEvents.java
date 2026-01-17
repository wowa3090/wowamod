package net.wowamod.client.events;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.wowamod.Universe3090Mod;
import net.wowamod.init.Universe3090ModItems;

/**
 * Этот класс отвечает за события, связанные с рендерингом игрока на стороне клиента.
 * Он скрывает стандартные части модели игрока (включая внешние слои), когда на него надета кастомная броня.
 */
@Mod.EventBusSubscriber(modid = Universe3090Mod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class PlayerModelHiderEvents {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        PlayerModel<AbstractClientPlayer> model = event.getRenderer().getModel();

        // Получаем предметы, надетые в слоты брони
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        ItemStack leggings = player.getItemBySlot(EquipmentSlot.LEGS);
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);

        // 1. Обработка головы (основной слой + шляпа)
        // ИСПРАВЛЕНО: Добавлен вызов .get() и правильное сравнение для WOWABRONYA шлема
        boolean shouldHideHead = helmet.getItem() == Universe3090ModItems.ELISSAARMOR_HELMET.get() || 
                                 helmet.getItem() == Universe3090ModItems.WOWABRONYA_HELMET.get();
        model.head.visible = !shouldHideHead;
        model.hat.visible = !shouldHideHead;

        // 2. Обработка тела и рук (основной слой + куртка/рукава)
        // ИСПРАВЛЕНО: Добавлен вызов .get() и правильное сравнение для WOWABRONYA нагрудника
        boolean shouldHideBodyAndArms = chestplate.getItem() == Universe3090ModItems.ELISSAARMOR_CHESTPLATE.get() || 
                                        chestplate.getItem() == Universe3090ModItems.WOWABRONYA_CHESTPLATE.get();
        model.body.visible = !shouldHideBodyAndArms;
        model.rightArm.visible = !shouldHideBodyAndArms;
        model.leftArm.visible = !shouldHideBodyAndArms;
        // ИСПРАВЛЕНО: Добавляем скрытие внешнего слоя (куртки и рукавов)
        model.jacket.visible = !shouldHideBodyAndArms;
        model.rightSleeve.visible = !shouldHideBodyAndArms;
        model.leftSleeve.visible = !shouldHideBodyAndArms;


        // 3. Обработка ног (основной слой + штаны)
        // ИСПРАВЛЕНО: Добавлены вызовы .get() и правильные сравнения для WOWABRONYA понож и ботинок
        boolean shouldHideLegs = leggings.getItem() == Universe3090ModItems.ELISSAARMOR_LEGGINGS.get() ||
                                 boots.getItem() == Universe3090ModItems.ELISSAARMOR_BOOTS.get() ||
                                 leggings.getItem() == Universe3090ModItems.WOWABRONYA_LEGGINGS.get() ||
                                 boots.getItem() == Universe3090ModItems.WOWABRONYA_BOOTS.get();
        model.rightLeg.visible = !shouldHideLegs;
        model.leftLeg.visible = !shouldHideLegs;
        // ИСПРАВЛЕНО: Добавляем скрытие внешнего слоя (штанин)
        model.rightPants.visible = !shouldHideLegs;
        model.leftPants.visible = !shouldHideLegs;
    }
}