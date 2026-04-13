package net.wowamod.custom;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Этот класс объединяет сущность и регистрацию в одном файле.
 * Размещен в пакете .custom, чтобы избежать перезаписи со стороны MCreator.
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class AssimilationClone extends Zombie {

    // Реестр для сущностей
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, "universe3090");

    // Регистрация самой сущности
    public static final RegistryObject<EntityType<AssimilationClone>> TYPE = ENTITIES.register("assimilation_clone",
            () -> EntityType.Builder.<AssimilationClone>of(AssimilationClone::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.95f)
                    .fireImmune() // Клон ассимиляции может быть невосприимчив к огню
                    .build("assimilation_clone"));

    public AssimilationClone(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
    }

    /**
     * Инициализация регистрации. 
     * Этот метод нужно вызвать один раз в основном конструкторе вашего мода или в FMLCommonSetupEvent.
     */
    public static void register() {
        ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    /**
     * Регистрация атрибутов для сущности.
     * Вызывается автоматически благодаря аннотации @EventBusSubscriber.
     */
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        AttributeSupplier.Builder attributes = Zombie.createAttributes();
        // Можно кастомизировать атрибуты (сделать сильнее/быстрее игрока)
        event.put(TYPE.get(), attributes.build());
    }
}