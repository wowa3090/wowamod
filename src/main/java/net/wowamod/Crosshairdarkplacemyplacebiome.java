package net.wowamod;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class Crosshairdarkplacemyplacebiome {
    
    private static final String MODID = "universe3090";
    private static final ResourceLocation CUSTOM_CROSSHAIR = new ResourceLocation(MODID, "textures/gui/custom_crosshair.png");
    private static final ResourceLocation MY_DIMENSION = new ResourceLocation("universe3090", "myplacezone");

    public Crosshairdarkplacemyplacebiome() {
    }

    @SubscribeEvent
    public static void init(FMLCommonSetupEvent event) {
        new Crosshairdarkplacemyplacebiome();
    }

    @Mod.EventBusSubscriber
    private static class ForgeBusEvents {
        @OnlyIn(Dist.CLIENT)
        @SubscribeEvent
        public static void onRenderCrosshair(RenderGuiOverlayEvent.Pre event) {
            Minecraft mc = Minecraft.getInstance();
            
            if (mc.level != null && mc.level.dimension().location().equals(MY_DIMENSION)) {
                if (event.getOverlay().id().equals(VanillaGuiOverlay.CROSSHAIR.id())) {
                    event.setCanceled(true); 

                    GuiGraphics guiGraphics = event.getGuiGraphics();
                    int screenWidth = event.getWindow().getGuiScaledWidth();
                    int screenHeight = event.getWindow().getGuiScaledHeight();
                    
                    // --- НАСТРОЙКИ РАЗМЕРА ---
                    // Уменьшаем размер до 12 пикселей (стандарт — 16)
                    int renderSize = 12; 
                    int offset = renderSize / 2; // Центрирование (6 пикселей)
                    
                    int x = screenWidth / 2 - offset;
                    int y = screenHeight / 2 - offset;

                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();

                    // --- ДИНАМИЧЕСКАЯ АНИМАЦИЯ ---
                    // Вычисляем пульсацию прозрачности от 0.3 до 0.8
                    float gameTime = mc.level.getGameTime() + event.getPartialTick();
                    float alpha = 0.55F + (float) Math.sin(gameTime * 0.15F) * 0.25F;

                    // Применение цвета и прозрачности
                    if (isTargetingEntity(mc)) {
                        guiGraphics.setColor(1.0F, 0.0F, 0.0F, alpha); // Красный пульсирующий
                    } else {
                        guiGraphics.setColor(1.0F, 1.0F, 1.0F, alpha); // Белый пульсирующий
                    }

                    // Отрисовка: последние два аргумента (12, 12) заставляют Minecraft 
                    // сжать любое исходное изображение в наш размер 12x12.
                    guiGraphics.blit(CUSTOM_CROSSHAIR, x, y, 0, 0, renderSize, renderSize, renderSize, renderSize);

                    // Сброс состояния
                    guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                    RenderSystem.disableBlend();
                }
            }
        }

        @OnlyIn(Dist.CLIENT)
        private static boolean isTargetingEntity(Minecraft mc) {
            if (mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.ENTITY) {
                EntityHitResult entityHitResult = (EntityHitResult) mc.hitResult;
                return entityHitResult.getEntity() instanceof LivingEntity;
            }
            return false;
        }
    }
}