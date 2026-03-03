package net.wowamod.registration;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.wowamod.capability.EnergyCapability;
import net.wowamod.entity.EnergyBallEntity;
import net.wowamod.client.renderer.EnergyBallRenderer;
import net.wowamod.handlers.EnergyInputHandler;
// ВОТ ЭТОТ ИМПОРТ БЫЛ НУЖЕН:
import net.wowamod.network.ModMessages;

@Mod.EventBusSubscriber(modid = "universe3090", bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModRegisterEnergySp {
    
    public static EntityType<EnergyBallEntity> ENERGY_BALL_TYPE;

    @SubscribeEvent
    public static void onRegister(RegisterEvent event) {
        if (event.getRegistryKey().equals(ForgeRegistries.Keys.ENTITY_TYPES)) {
            ENERGY_BALL_TYPE = EntityType.Builder.<EnergyBallEntity>of(EnergyBallEntity::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .setShouldReceiveVelocityUpdates(true)
                    .build("energy_ball");

            event.register(ForgeRegistries.Keys.ENTITY_TYPES, 
                new ResourceLocation("universe3090", "energy_ball"), 
                () -> ENERGY_BALL_TYPE);
        }
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // Регистрация пакетов
            ModMessages.register(); 
        });
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(EnergyCapability.class);
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        if (ENERGY_BALL_TYPE != null) {
            event.registerEntityRenderer(ENERGY_BALL_TYPE, EnergyBallRenderer::new);
        }
    }
}