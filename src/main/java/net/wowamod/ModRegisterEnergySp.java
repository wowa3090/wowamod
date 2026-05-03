package net.wowamod.registration;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.client.gui.screens.MenuScreens;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

import net.wowamod.capability.EnergyCapability;
import net.wowamod.entity.EnergyBallEntity;
import net.wowamod.client.renderer.EnergyBallRenderer;
import net.wowamod.handlers.EnergyInputHandler;

// ИЗМЕНЕНО: Импортируем нашу независимую сеть
import net.wowamod.network.MWNetwork;
import net.wowamod.inventory.EmitterMenu;
import net.wowamod.client.gui.EmitterScreen;

@Mod.EventBusSubscriber(modid = "universe3090", bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModRegisterEnergySp {
    
    public static EntityType<EnergyBallEntity> ENERGY_BALL_TYPE;
    public static MenuType<EmitterMenu> MW_EMITTER_MENU;

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

        if (event.getRegistryKey().equals(Registries.MENU)) {
            MW_EMITTER_MENU = IForgeMenuType.create((windowId, inv, data) -> {
                net.minecraft.core.BlockPos pos = data.readBlockPos();
                return new EmitterMenu(windowId, inv, pos);
            });
            event.register(Registries.MENU, 
                new ResourceLocation("universe3090", "mw_emitter_menu"), 
                () -> MW_EMITTER_MENU);
        }
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // ИЗМЕНЕНО: Запускаем регистрацию НАШИХ пакетов через кастомный класс
            MWNetwork.register(); 
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

    @Mod.EventBusSubscriber(modid = "universe3090", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                MenuScreens.register(MW_EMITTER_MENU, EmitterScreen::new);
            });
        }
    }
}