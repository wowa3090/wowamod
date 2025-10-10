package net.wowamod;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.wowamod.init.Universe3090ModItems; // Импортируем класс с зарегистрированными предметами
import net.wowamod.Universe3090Mod; // Импортируем основной класс мода

// Подписываемся на Forge EventBus с указанием modid
@Mod.EventBusSubscriber(modid = Universe3090Mod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ElissClawsDoubleArms {

    // Уникальные UUID для AttributeModifier ускорения атаки и урона
    private static final java.util.UUID SPEED_BOOST_MODIFIER_ID = java.util.UUID.fromString("12345678-1234-1234-1234-123456789abc");
    private static final String SPEED_BOOST_MODIFIER_NAME = "eliss_claws_attack_speed_boost";
    
    private static final java.util.UUID DAMAGE_BOOST_MODIFIER_ID = java.util.UUID.fromString("12345678-1234-1234-1234-123456789def");
    private static final String DAMAGE_BOOST_MODIFIER_NAME = "eliss_claws_attack_damage_boost";

    /**
     * Событие Tick игрока (фаза END).
     * Используется для проверки состояния предметов в руках и управления атрибутами.
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) {
            return; // Обрабатываем на сервере, фаза END
        }

        Player player = event.player;
        ItemStack mainHandStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack offHandStack = player.getItemInHand(InteractionHand.OFF_HAND);

        boolean hasMainClaw = isElissClaws(mainHandStack);
        boolean hasOffClaw = isElissClaws(offHandStack);

        // Применяем или убираем ускорение и урон в зависимости от наличия ElissClaws в обеих руках
        applyBoosts(player, hasMainClaw && hasOffClaw);
    }

    /**
     * Применяет или убирает модификаторы атаки (скорости и урона) на основе состояния.
     *
     * @param player Игрок, к которому применяются модификаторы.
     * @param active True, если модификаторы должны быть активны (ElissClaws в обеих руках), false - если должны быть убраны.
     */
    private static void applyBoosts(Player player, boolean active) {
        // Управление ATTACK_SPEED
        var attackSpeedAttribute = player.getAttribute(Attributes.ATTACK_SPEED);
        if (attackSpeedAttribute != null) {
            var existingSpeedModifier = attackSpeedAttribute.getModifier(SPEED_BOOST_MODIFIER_ID);
            if (active && existingSpeedModifier == null) {
                var speedModifier = new AttributeModifier(
                    SPEED_BOOST_MODIFIER_ID,
                    SPEED_BOOST_MODIFIER_NAME,
                    3.0, // Увеличение на 1.0 (например, с 4 до 5)
                    AttributeModifier.Operation.ADDITION
                );
                attackSpeedAttribute.addTransientModifier(speedModifier);
            } else if (!active && existingSpeedModifier != null) {
                attackSpeedAttribute.removeModifier(SPEED_BOOST_MODIFIER_ID);
            }
        }

        // Управление ATTACK_DAMAGE
        var attackDamageAttribute = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamageAttribute != null) {
            var existingDamageModifier = attackDamageAttribute.getModifier(DAMAGE_BOOST_MODIFIER_ID);
            if (active && existingDamageModifier == null) {
                var damageModifier = new AttributeModifier(
                    DAMAGE_BOOST_MODIFIER_ID,
                    DAMAGE_BOOST_MODIFIER_NAME,
                    5.0, // Увеличение на 2.0 (например, с 1 до 3)
                    AttributeModifier.Operation.ADDITION
                );
                attackDamageAttribute.addTransientModifier(damageModifier);
            } else if (!active && existingDamageModifier != null) {
                attackDamageAttribute.removeModifier(DAMAGE_BOOST_MODIFIER_ID);
            }
        }
    }

    // --- ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ---

    /**
     * Проверяет, является ли ItemStack ELISS_CLAWS.
     */
    private static boolean isElissClaws(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.getItem() == Universe3090ModItems.ELISS_CLAWS.get();
    }
}