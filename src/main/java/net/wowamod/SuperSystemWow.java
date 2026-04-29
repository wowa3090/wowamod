package net.wowamod;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.lwjgl.glfw.GLFW;

import net.wowamod.init.Universe3090ModParticleTypes;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class SuperSystemWow {

    private static final ResourceLocation SUPER_FORM_EFFECT_ID = new ResourceLocation("universe3090", "super_form");

    public SuperSystemWow() {
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        public static KeyMapping SUPER_FORM_KEY;

        @SubscribeEvent
        public static void onKeyRegister(RegisterKeyMappingsEvent event) {
            SUPER_FORM_KEY = new KeyMapping("key.universe3090.super_form", GLFW.GLFW_KEY_K, "key.categories.wowamod");
            event.register(SUPER_FORM_KEY);
        }

        // Инициализация интеграции с RyoamicLights
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                // Если мод на свет установлен - включаем наш совместимый класс
                if (ModList.get().isLoaded("ryoamiclights")) {
                    SuperFormLightCompat.init();
                }
            });
        }
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ClientForgeEvents {

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;

            while (ClientModEvents.SUPER_FORM_KEY != null && ClientModEvents.SUPER_FORM_KEY.consumeClick()) {
                mc.player.connection.sendCommand("superform toggle");
            }
        }

        @SubscribeEvent
        public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            Player player = event.player;
            Level level = player.level();

            // Золотые искры 
            if (level.isClientSide()) {
                MobEffect superFormEffect = ForgeRegistries.MOB_EFFECTS.getValue(SUPER_FORM_EFFECT_ID);
                
                if (superFormEffect != null && player.hasEffect(superFormEffect)) {
                    // Увеличена частота: спавним партиклы каждый тик. Цикл добавляет плотности (2 искры за тик)
                    for (int i = 0; i < 2; i++) {
                        double x = player.getX() + (level.random.nextDouble() - 0.5) * 1.5;
                        double y = player.getY() + level.random.nextDouble() * 2.0;
                        double z = player.getZ() + (level.random.nextDouble() - 0.5) * 1.5;
                        
                        // Задаем им легкое рассеивание и движение вверх.
                        double vx = (level.random.nextDouble() - 0.5) * 0.1;
                        double vy = level.random.nextDouble() * 0.1;
                        double vz = (level.random.nextDouble() - 0.5) * 0.1;
                        
                        level.addParticle(Universe3090ModParticleTypes.SUPERFORMPARTICLE.get(), x, y, z, vx, vy, vz);
                    }
                }
            }
        }
    }
}