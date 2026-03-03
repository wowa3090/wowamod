package net.wowamod.registration;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.wowamod.capability.EnergyCapability;
import net.wowamod.entity.EnergyBallEntity;
import net.wowamod.client.renderer.EnergyBallRenderer;
import net.wowamod.handlers.EnergyInputHandler;

// Заменил переменную мода на строковый литерал "universe3090"
@Mod.EventBusSubscriber(modid = "universe3090", bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModRegisterEnergySp {
    
    // ============================================
    // РЕГИСТРАЦИЯ СУЩНОСТЕЙ
    // ============================================
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = 
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, "universe3090");

    public static final RegistryObject<EntityType<EnergyBallEntity>> ENERGY_BALL = 
            ENTITY_TYPES.register("energy_ball", () -> 
                EntityType.Builder.<EnergyBallEntity>of(EnergyBallEntity::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .setShouldReceiveVelocityUpdates(true)
                    .build("energy_ball")
            );

    // ============================================
    // ИНИЦИАЛИЗАЦИЯ ПРИ ЗАГРУЗКЕ МОДА
    // ============================================
    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // Регистрация обработчика событий
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(EnergyInputHandler.class);
        });
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        // Регистрация Capability для хранения данных игрока
        event.register(EnergyCapability.class);
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // Правильная регистрация рендерера в 1.20.1 (убрал дубликат из ClientSetup)
        event.registerEntityRenderer(ENERGY_BALL.get(), EnergyBallRenderer::new);
    }

    // ============================================
    // МЕТОД ДЛЯ ВЫЗОВА ИЗ ГЛАВНОГО КЛАССА
    // ============================================
    public static void init() {
        // ИСПРАВЛЕНО: Правильный метод получения EventBus в Forge 1.20.1
        ENTITY_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}