package net.wowamod.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.wowamod.item.Diamond_lapis_armorArmorItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;

public class ArmorbuffProcedure {

    // Уникальный UUID для модификатора атрибута
    private static final UUID ARMOR_SPEED_MODIFIER_UUID = UUID.fromString("6d9e1c6a-4e9b-4c3b-8a1d-2e7f3a4c5d6e");
    private static final String ARMOR_SPEED_MODIFIER_NAME = "DiamondLapisArmorSpeedBoost";
    private static final Logger LOGGER = LogManager.getLogger(ArmorbuffProcedure.class);

    public static void execute(Entity entity) {
        if (entity == null || !(entity instanceof LivingEntity livingEntity)) {
            // LOGGER.debug("Entity is null or not LivingEntity, skipping armor buff.");
            return;
        }

        if (entity.level().isClientSide()) {
            // Логика атрибутов выполняется только на серверной стороне
            // LOGGER.debug("Client side, skipping armor buff logic.");
            return;
        }

        AttributeInstance movementSpeedAttribute = livingEntity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeedAttribute == null) {
            LOGGER.error("Movement speed attribute is null for entity: {}", entity.getName().getString());
            return; // Атрибут не найден, невозможно применить модификатор
        }

        // Удаляем предыдущий модификатор, если он существует
        AttributeModifier oldModifier = movementSpeedAttribute.getModifier(ARMOR_SPEED_MODIFIER_UUID);
        if (oldModifier != null) {
             // LOGGER.debug("Removing old armor speed modifier for entity: {}", livingEntity.getName().getString());
            movementSpeedAttribute.removeModifier(oldModifier);
        }

        // Рассчитываем новый бонус
        double totalBonus = calculateArmorBonus(livingEntity);

        if (totalBonus > 0) {
            // Создаем новый модификатор
            AttributeModifier newModifier = new AttributeModifier(
                ARMOR_SPEED_MODIFIER_UUID,
                ARMOR_SPEED_MODIFIER_NAME,
                totalBonus, // Значение бонуса (например, 0.05 для 5%)
                AttributeModifier.Operation.MULTIPLY_TOTAL // MULTIPLY_TOTAL для процентного увеличения
            );

             // LOGGER.debug("Adding armor speed modifier: {} ({}%) to entity: {}", newModifier.getName(), (totalBonus * 100), livingEntity.getName().getString());
            movementSpeedAttribute.addTransientModifier(newModifier); // Используем addTransientModifier для динамических эффектов
        } else {
             // LOGGER.debug("No armor pieces found or no bonus calculated for entity: {}", livingEntity.getName().getString());
        }
    }

    private static double calculateArmorBonus(LivingEntity entity) {
        double bonus = 0.0;

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.ARMOR) {
                continue; // Проверяем только слоты брони
            }

            ItemStack armorStack = entity.getItemBySlot(slot);
            if (armorStack.getItem() instanceof Diamond_lapis_armorArmorItem) {
                // LOGGER.debug("Found Diamond_lapis_armorArmorItem in slot: {}", slot.getName());
                switch (slot) {
                    case HEAD: // Шлем
                        bonus += 0.03; // 1%
                        break;
                    case CHEST: // Нагрудник
                        bonus += 0.06; // 2%
                        break;
                    case LEGS: // Поножи
                        bonus += 0.03; // 1%
                        break;
                    case FEET: // Ботинки
                        bonus += 0.03; // 1%
                        break;
                }
            }
            // else { LOGGER.debug("Slot {} does not contain Diamond_lapis_armorArmorItem.", slot.getName()); }
        }

        // LOGGER.debug("Calculated total armor bonus: {}%", bonus * 100);
        return bonus;
    }

    // Пример подписки на событие изменения брони - этот код обычно находится в главном классе мода
    /*
    public static class ArmorEquipHandler {
        @SubscribeEvent
        public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
            if (event.getSlot().getType() == EquipmentSlot.Type.ARMOR) {
                // Проверяем, связан ли старый или новый предмет с нашей броней
                boolean oldIsOurArmor = event.getFrom().getItem() instanceof Diamond_lapis_armorArmorItem;
                boolean newIsOurArmor = event.getTo().getItem() instanceof Diamond_lapis_armorArmorItem;

                if (oldIsOurArmor || newIsOurArmor) {
                    // Пересчитываем бонус
                    ArmorbuffProcedure.execute(event.getEntity());
                }
            }
        }
    }
    */
}