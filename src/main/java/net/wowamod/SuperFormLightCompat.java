package net.wowamod;

import org.thinkingstudio.ryoamiclights.api.DynamicLightHandlers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;

/**
 * Изолированный класс для динамического освещения.
 * Загружается ТОЛЬКО если установлен мод RyoamicLights.
 */
public class SuperFormLightCompat {

    private static final ResourceLocation SUPER_FORM_EFFECT_ID = new ResourceLocation("universe3090", "super_form");

    public static void init() {
        // Регистрируем источник света для игрока
        DynamicLightHandlers.registerDynamicLightHandler(
            EntityType.PLAYER,
            (Player player) -> {
                // Если у игрока есть эффект Суперформы -> излучаем свет 15 уровня (максимум)
                var superFormEffect = BuiltInRegistries.MOB_EFFECT.get(SUPER_FORM_EFFECT_ID);
                if (superFormEffect != null && player.hasEffect(superFormEffect)) {
                    return 15; 
                }
                return 0; // В обычном состоянии света нет
            }
        );
    }
}