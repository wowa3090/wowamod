package net.wowamod.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderStateShard; // Импортируем RenderStateShard
import net.minecraft.util.Mth;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.wowamod.item.MtwmodeorbitalItem; // Импорт нашего предмета

import java.util.OptionalDouble;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = "universe3090", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AreaRendererMTW {

    // Создаём кастомный RenderType для заливки (диск) с POSITION_COLOR
    private static final RenderType AREA_FILL_TYPE = RenderType.create(
            "area_fill",
            com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_COLOR,
            com.mojang.blaze3d.vertex.VertexFormat.Mode.QUADS,
            256, // vertex count
            false, // does it have a texture? No
            false, // does it have a lightmap? No
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderType.ShaderStateShard(net.minecraft.client.renderer.GameRenderer::getPositionColorShader))
                    .setTransparencyState(new RenderType.TransparencyStateShard("translucent_transparency", () -> {
                        RenderSystem.enableBlend();
                        RenderSystem.defaultBlendFunc();
                    }, () -> {
                        RenderSystem.disableBlend();
                    }))
                    .setWriteMaskState(new RenderType.WriteMaskStateShard(true, true)) // (redGreenBlue, alpha)
                    .createCompositeState(false)
    );

    // Кастомный RenderType для контура (линии) с POSITION_COLOR - БЕЗ толщины линии
    private static final RenderType AREA_OUTLINE_TYPE = RenderType.create(
            "area_outline",
            com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_COLOR,
            com.mojang.blaze3d.vertex.VertexFormat.Mode.LINES,
            256, // vertex count
            false, // does it have a texture? No
            false, // does it have a lightmap? No
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderType.ShaderStateShard(net.minecraft.client.renderer.GameRenderer::getPositionColorShader))
                    .setTransparencyState(new RenderType.TransparencyStateShard("translucent_transparency", () -> {
                        RenderSystem.enableBlend();
                        RenderSystem.defaultBlendFunc();
                    }, () -> {
                        RenderSystem.disableBlend();
                    }))
                    .setWriteMaskState(new RenderType.WriteMaskStateShard(true, true)) // (redGreenBlue, alpha)
                    // .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.of(2.0))) // УБРАНО
                    .createCompositeState(false)
    );

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        // Рисуем ПОСЛЕ твердых блоков, чтобы кольцо было "на" земле
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SOLID_BLOCKS) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.level == null) return;

        // Проверяем, что предмет в ЛЮБОЙ руке
        boolean holdingItem = player.getMainHandItem().getItem() instanceof MtwmodeorbitalItem ||
                player.getOffhandItem().getItem() instanceof MtwmodeorbitalItem;
        if (!holdingItem) return;

        // Используем public static raycast из MtwmodeorbitalItem
        BlockHitResult hit = MtwmodeorbitalItem.raycast(player, mc.level, MtwmodeorbitalItem.MAX_RANGE);
        if (hit == null) return;

        BlockPos pos = hit.getBlockPos();

        // Рисуем кольцо, используя радиус из предмета
        drawRing(event.getPoseStack(), mc, pos, MtwmodeorbitalItem.STRIKE_RADIUS);
    }

    private static void drawRing(PoseStack poseStack, Minecraft mc, BlockPos center, float radius) {
        Camera cam = mc.gameRenderer.getMainCamera();
        double camX = cam.getPosition().x;
        double camY = cam.getPosition().y;
        double camZ = cam.getPosition().z;

        poseStack.pushPose();
        // Сдвигаем на 0.015, чтобы избежать Z-файтинга (мерцания) с блоками
        poseStack.translate(center.getX() - camX + 0.5, center.getY() - camY + 0.015, center.getZ() - camZ + 0.5);

        // Анимация пульсации
        float time = (mc.level.getGameTime() % 30) / 30.0f; // 1.5 сек на цикл
        float alpha = 0.4f + 0.3f * Mth.sin(time * Mth.TWO_PI);
        float r = 0.1f, g = 0.8f, b = 1.0f;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest(); // Отключаем тест глубины
        RenderSystem.disableCull(); // Отключаем отсечение задних граней

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        // --- РИСОВАНИЕ ДИСКА (ЗАЛИВКА) ---
        VertexConsumer vc_fill = bufferSource.getBuffer(AREA_FILL_TYPE);
        int segments = 64;
        float y = 0.0f;
        float inner = radius * (0.9f + Mth.sin(time * Mth.TWO_PI) * 0.05f); // Пульсирующий внутренний радиус

        // Диск (кольцо) как QUADS
        for (int i = 0; i < segments; i++) {
            float a0 = (float) (2 * Math.PI * i / segments);
            float a1 = (float) (2 * Math.PI * (i + 1) / segments);

            float x0o = radius * Mth.cos(a0), z0o = radius * Mth.sin(a0);
            float x1o = radius * Mth.cos(a1), z1o = radius * Mth.sin(a1);
            float x0i = inner * Mth.cos(a0), z0i = inner * Mth.sin(a0);
            float x1i = inner * Mth.cos(a1), z1i = inner * Mth.sin(a1);

            // Вершина 0: внутренняя, сегмент i
            vc_fill.vertex(poseStack.last().pose(), x0i, y, z0i).color(r, g, b, alpha).endVertex();
            // Вершина 1: внутренняя, сегмент i+1
            vc_fill.vertex(poseStack.last().pose(), x1i, y, z1i).color(r, g, b, alpha).endVertex();
            // Вершина 2: внешняя, сегмент i+1
            vc_fill.vertex(poseStack.last().pose(), x1o, y, z1o).color(r, g, b, alpha).endVertex();
            // Вершина 3: внешняя, сегмент i
            vc_fill.vertex(poseStack.last().pose(), x0o, y, z0o).color(r, g, b, alpha).endVertex();
        }

        // Завершаем батч для заливки
        bufferSource.endBatch(AREA_FILL_TYPE); // Используем наш кастомный тип


        // --- РИСОВАНИЕ КОНТУРА ---
        float outer = radius + 0.1f;
        VertexConsumer vc_outline = bufferSource.getBuffer(AREA_OUTLINE_TYPE);

        // Устанавливаем толщину линии перед отрисовкой контура (альтернатива LineStateShard)
        RenderSystem.lineWidth(2.0f); // Устанавливаем толщину линии

        // Внешний контур (линии)
        for (int i = 0; i < segments; i++) {
            float a0 = (float) (2 * Math.PI * i / segments);
            float a1 = (float) (2 * Math.PI * (i + 1) / segments);

            float x0o = outer * Mth.cos(a0), z0o = outer * Mth.sin(a0);
            float x1o = outer * Mth.cos(a1), z1o = outer * Mth.sin(a1);
            float x0r = radius * Mth.cos(a0), z0r = radius * Mth.sin(a0);
            float x1r = radius * Mth.cos(a1), z1r = radius * Mth.sin(a1);

            // Рисуем линию от внутреннего кольца к внешнему (один сегмент контура)
            vc_outline.vertex(poseStack.last().pose(), x0r, y, z0r).color(r, g, b, alpha + 0.3f).endVertex();
            vc_outline.vertex(poseStack.last().pose(), x0o, y, z0o).color(r, g, b, alpha + 0.3f).endVertex();

            // Рисуем линию внешнего кольца (для сплошного контура) - Опционально
            // vc_outline.vertex(poseStack.last().pose(), x0o, y, z0o).color(r, g, b, alpha + 0.3f).endVertex();
            // vc_outline.vertex(poseStack.last().pose(), x1o, y, z1o).color(r, g, b, alpha + 0.3f).endVertex();
        }

        // Сбрасываем толщину линии (желательно, но RenderSystem может сам сбросить после батча)
        // RenderSystem.lineWidth(1.0f);

        // Завершаем батч для контура
        bufferSource.endBatch(AREA_OUTLINE_TYPE); // Используем наш кастомный тип


        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();

        poseStack.popPose();
    }
}