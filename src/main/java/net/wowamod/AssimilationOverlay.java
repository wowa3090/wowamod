package net.wowamod.custom;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.api.distmarker.Dist;
import net.wowamod.AssimilationDarkBiome;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AssimilationOverlay {

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        // Рендерим в Post после Hotbar, чтобы быть уверенными, что данные уже синхронизированы
        if (event.getOverlay().id().equals(VanillaGuiOverlay.HOTBAR.id())) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.options.hideGui) return;

            mc.player.getCapability(AssimilationDarkBiome.ASSIMILATION_CAP).ifPresent(data -> {
                int level = data.getLevel();
                if (level > 0) {
                    renderEffect(event.getGuiGraphics(), event.getWindow().getGuiScaledWidth(), event.getWindow().getGuiScaledHeight(), level);
                }
            });
        }
    }

    private static void renderEffect(GuiGraphics guiGraphics, int width, int height, int level) {
        float alpha = Math.min(0.85f, 0.2f + (level * 0.12f));
        int color = ((int)(alpha * 255) << 24); // Черный с альфой

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        // Рисуем основной слой
        guiGraphics.fill(0, 0, width, height, color);

        // Если уровень высокий, добавляем "грязную" рамку
        if (level >= 4) {
            int bloodColor = (0x55 << 24) | 0x330000;
            guiGraphics.fill(0, 0, width, 20, bloodColor);
            guiGraphics.fill(0, height - 20, width, height, bloodColor);
        }

        if (level >= 5) {
            String msg = level == 6 ? "S" : "ТЬМА ПОГЛОЩАЕТ ВАС";
            guiGraphics.drawString(Minecraft.getInstance().font, msg, 
                (width - Minecraft.getInstance().font.width(msg)) / 2, height / 2, 0xFF990000, true);
        }

        RenderSystem.disableBlend();
    }
}