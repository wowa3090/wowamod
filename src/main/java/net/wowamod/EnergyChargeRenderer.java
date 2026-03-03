package net.wowamod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.wowamod.procedures.EnergyBallSposobkaProcedure;
import net.wowamod.capability.EnergyCapability;
import net.wowamod.core.AbilityConfig;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

@Mod.EventBusSubscriber(modid = "universe3090", value = Dist.CLIENT)
public class EnergyChargeRenderer {

    private static final ResourceLocation TEXTURE = new ResourceLocation("universe3090", "textures/entity/energy_ball.png");

    private static float chargeIntensity = 0.0f;
    private static float rotationAngle = 0.0f;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;
            boolean isCharging = player != null && EnergyBallSposobkaProcedure.isPlayerCharging(player);
            
            // Плавное появление и исчезновение
            if (isCharging) chargeIntensity = Mth.lerp(0.1f, chargeIntensity, 1.0f);
            else chargeIntensity = 0.0f; // При выстреле пропадает сразу

            // Вращаем шар постоянно
            if (chargeIntensity > 0.01f) rotationAngle += 12.0f;
        }
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        // Рисуем после прозрачных блоков
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        if (chargeIntensity < 0.01f) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        // Получаем цвет изумруда
        int[] color = new int[]{0, 200, 255, 180}; // Дефолт (Голубой)
        var cap = player.getCapability(EnergyCapability.ENERGY_CAPABILITY);
        if (cap.isPresent()) {
            color = AbilityConfig.getEmeraldColor(cap.resolve().get().getEmeraldFlags());
        }

        // Позиция камеры и руки
        Vec3 camPos = event.getCamera().getPosition();
        float partialTick = event.getPartialTick();
        Vec3 handPos = getHandPosition(player, partialTick);
        
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        poseStack.pushPose();
        poseStack.translate(handPos.x - camPos.x, handPos.y - camPos.y, handPos.z - camPos.z);

        // Размер зависит от времени зарядки
        float size = 0.2f + (chargeIntensity * 0.5f);
        poseStack.scale(size, size, size);
        
        // Общее вращение сферы (чтобы она вся крутилась в руке)
        float time = rotationAngle + partialTick * 12;
        poseStack.mulPose(Axis.YP.rotationDegrees(time));
        poseStack.mulPose(Axis.XP.rotationDegrees(time * 0.5f));
        poseStack.mulPose(Axis.ZP.rotationDegrees(time * 0.2f));

        // ВАЖНО: Мы убрали cameraOrientation() (Билборд), теперь это честное 3D

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(TEXTURE));
        
        // Рисуем объемную сферу из пересекающихся плоскостей
        // 1. Цветная оболочка
        draw3DSphere(poseStack, consumer, 1.0f, color[0], color[1], color[2], color[3]);
        
        // 2. Белое горячее ядро (меньше и ярче)
        poseStack.mulPose(Axis.YP.rotationDegrees(45)); // Повернем ядро, чтобы грани не совпадали с оболочкой
        draw3DSphere(poseStack, consumer, 0.6f, 255, 255, 255, 255);

        poseStack.popPose();
    }

    private static void draw3DSphere(PoseStack poseStack, VertexConsumer consumer, float scale, int r, int g, int b, int a) {
        // Рисуем 3 основные плоскости (X, Y, Z) + 2 диагональные для объема
        // Это создает эффект "Атома" или шара энергии
        
        // Плоскость 1 (Вертикальная)
        poseStack.pushPose();
        addQuad(consumer, poseStack.last().pose(), scale, r, g, b, a);
        poseStack.popPose();

        // Плоскость 2 (Поворот 60 градусов)
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(60));
        addQuad(consumer, poseStack.last().pose(), scale, r, g, b, a);
        poseStack.popPose();

        // Плоскость 3 (Поворот 120 градусов)
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(120));
        addQuad(consumer, poseStack.last().pose(), scale, r, g, b, a);
        poseStack.popPose();

        // Плоскость 4 (Горизонтальная/Наклонная)
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(90));
        addQuad(consumer, poseStack.last().pose(), scale, r, g, b, a);
        poseStack.popPose();
        
        // Плоскость 5 (Диагональная)
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(45));
        poseStack.mulPose(Axis.ZP.rotationDegrees(45));
        addQuad(consumer, poseStack.last().pose(), scale, r, g, b, a);
        poseStack.popPose();
    }

    private static Vec3 getHandPosition(Player player, float partialTick) {
        Vec3 eyePos = player.getEyePosition(partialTick);
        Vec3 lookVec = player.getViewVector(partialTick);
        Vec3 upVec = player.getUpVector(partialTick); 
        Vec3 rightVec = lookVec.cross(upVec).normalize();
        
        // Позиция в правой руке
        return eyePos
                .add(lookVec.scale(0.8))    // Чуть дальше от лица
                .add(rightVec.scale(0.4))   // Вправо
                .add(upVec.scale(-0.3));    // Вниз
    }

    private static void addQuad(VertexConsumer consumer, Matrix4f matrix, float size, int r, int g, int b, int a) {
        float h = size / 2.0f;
        
        // Рисуем квадрат с двух сторон, чтобы он не исчезал при вращении
        // Сторона А
        vertex(consumer, matrix, -h, -h, 0, 0, 1, r, g, b, a);
        vertex(consumer, matrix, h, -h, 0, 1, 1, r, g, b, a);
        vertex(consumer, matrix, h, h, 0, 1, 0, r, g, b, a);
        vertex(consumer, matrix, -h, h, 0, 0, 0, r, g, b, a);
        
        // Сторона Б (Обратная)
        vertex(consumer, matrix, -h, h, 0, 0, 0, r, g, b, a);
        vertex(consumer, matrix, h, h, 0, 1, 0, r, g, b, a);
        vertex(consumer, matrix, h, -h, 0, 1, 1, r, g, b, a);
        vertex(consumer, matrix, -h, -h, 0, 0, 1, r, g, b, a);
    }
    
    private static void vertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z, float u, float v, int r, int g, int b, int a) {
        consumer.vertex(matrix, x, y, z)
                .color(r, g, b, a)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(240, 240) // Полная яркость (светится в темноте)
                .normal(0, 1, 0)
                .endVertex();
    }
}