package net.wowamod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.wowamod.entity.EnergyBallEntity;
import net.wowamod.core.AbilityConfig;
import org.joml.Matrix4f;

public class EnergyBallRenderer extends EntityRenderer<EnergyBallEntity> {

    // Укажите путь к любой белой текстуре. Если её нет, шар может быть черно-фиолетовым.
    // Если текстуры нет, временно можно использовать текстуру снежка: new ResourceLocation("textures/item/snowball.png")
    private static final ResourceLocation TEXTURE = new ResourceLocation("universe3090", "textures/entity/energy_ball.png"); 

    public EnergyBallRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(EnergyBallEntity entity, float entityYaw, float partialTicks, 
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0, 0.25, 0.0);

        // Получаем цвет (RGBA)
        int[] color = AbilityConfig.getEmeraldColor(entity.getEmeraldFlags());

        float scale = 0.8f;
        if ((entity.getEmeraldFlags() & 64) != 0) scale *= 2.65f; // Красный
        poseStack.scale(scale, scale, scale);

        float time = entity.tickCount + partialTicks;
        poseStack.mulPose(Axis.YP.rotationDegrees(time * 20));
        poseStack.mulPose(Axis.XP.rotationDegrees(time * 10));

        // ИСПРАВЛЕНО: Используем entityTranslucent, чтобы цвет был виден
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(getTextureLocation(entity)));
        
        // Рисуем ядро (белое, яркое)
        drawPolySphere(poseStack, consumer, 0.4f, 255, 255, 255, 255);
        
        // Рисуем оболочку (Цветную)
        drawPolySphere(poseStack, consumer, 1.0f, color[0], color[1], color[2], color[3]);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private static void drawPolySphere(PoseStack poseStack, VertexConsumer consumer, float scale, int r, int g, int b, int a) {
        int planes = 12;
        for (int i = 0; i < planes; i++) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees((360.0f / planes) * i));
            poseStack.mulPose(Axis.XP.rotationDegrees(45.0f)); 
            addQuad(consumer, poseStack.last().pose(), scale, r, g, b, a);
            poseStack.popPose();
        }
    }

    private static void addQuad(VertexConsumer consumer, Matrix4f matrix, float size, int r, int g, int b, int a) {
        float h = size / 2.0f;
        // Важно: OverlayTexture.NO_OVERLAY и свет 240 (полная яркость)
        consumer.vertex(matrix, -h, 0, -h).color(r, g, b, a).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240, 240).normal(0, 1, 0).endVertex();
        consumer.vertex(matrix, h, 0, -h).color(r, g, b, a).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240, 240).normal(0, 1, 0).endVertex();
        consumer.vertex(matrix, h, 0, h).color(r, g, b, a).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240, 240).normal(0, 1, 0).endVertex();
        consumer.vertex(matrix, -h, 0, h).color(r, g, b, a).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240, 240).normal(0, 1, 0).endVertex();
        
        // Обратная сторона
        consumer.vertex(matrix, -h, 0, h).color(r, g, b, a).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240, 240).normal(0, 1, 0).endVertex();
        consumer.vertex(matrix, h, 0, h).color(r, g, b, a).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240, 240).normal(0, 1, 0).endVertex();
        consumer.vertex(matrix, h, 0, -h).color(r, g, b, a).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240, 240).normal(0, 1, 0).endVertex();
        consumer.vertex(matrix, -h, 0, -h).color(r, g, b, a).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240, 240).normal(0, 1, 0).endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(EnergyBallEntity entity) {
        return TEXTURE;
    }
}