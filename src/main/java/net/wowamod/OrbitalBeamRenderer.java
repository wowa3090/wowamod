package net.wowamod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.wowamod.entity.OrbitalBeamEntity;
import net.wowamod.item.MtwmodeorbitalItem; // <--- ДОБАВЛЕНО
import org.joml.Matrix4f;
import org.joml.Matrix3f;

public class OrbitalBeamRenderer extends EntityRenderer<OrbitalBeamEntity> {
  // Используем ванильную текстуру луча маяка
  private static final ResourceLocation BEAM_TEX = new ResourceLocation("textures/entity/beacon_beam.png");
  // Высота луча
  private static final float BEAM_HEIGHT = 256.0f; 

  public OrbitalBeamRenderer(EntityRendererProvider.Context ctx) {
    super(ctx);
    this.shadowRadius = 0.0f; // У луча нет тени
  }

  @Override
  public void render(OrbitalBeamEntity entity, float yaw, float partialTicks,
                     PoseStack poseStack, MultiBufferSource buffers, int light) {
    
    poseStack.pushPose();

    float radius = MtwmodeorbitalItem.STRIKE_RADIUS * 0.4f; // Луч делаем уже, чем область
    float life = entity.getLife() + partialTicks;

    // Рассчитываем альфу (прозрачность) на основе таймингов
    float alpha;
    if (life < OrbitalBeamEntity.WARMUP) {
      // 1. Появление (Fade-in)
      alpha = life / (float)OrbitalBeamEntity.WARMUP;
    } else if (life < OrbitalBeamEntity.WARMUP + OrbitalBeamEntity.PEAK) {
      // 2. Пик
      alpha = 1.0f;
    } else {
      // 3. Исчезание (Fade-out)
      float fadeTime = life - (OrbitalBeamEntity.WARMUP + OrbitalBeamEntity.PEAK);
      alpha = 1.0f - (fadeTime / (float)OrbitalBeamEntity.FADE);
    }
    alpha = Mth.clamp(alpha, 0.0f, 1.0f);
    
    // Применяем легкое мерцание на альфу
    alpha *= (0.75f + Mth.sin(life * 0.5f) * 0.25f);
    if (alpha <= 0.0f) {
      poseStack.popPose();
      return; // Не рисуем, если невидимый
    }
    
    int color = (int)(alpha * 255);

    // Получаем VertexConsumer для нужного RenderType
    VertexConsumer vc = buffers.getBuffer(RenderType.beaconBeam(BEAM_TEX, true));
    Matrix4f pose = poseStack.last().pose();
    Matrix3f normal = poseStack.last().normal();

    // Рисуем 4 плоскости (квада) для создания "цилиндра"
    drawQuad(vc, pose, normal, 1.0f, 0.8f, 0.2f, alpha, -radius, 0, -radius, -radius, BEAM_HEIGHT, -radius, 0.0f, 1.0f);
    drawQuad(vc, pose, normal, 1.0f, 0.8f, 0.2f, alpha, radius, 0, -radius, radius, BEAM_HEIGHT, -radius, 0.0f, 1.0f);
    drawQuad(vc, pose, normal, 1.0f, 0.8f, 0.2f, alpha, radius, 0, radius, radius, BEAM_HEIGHT, radius, 0.0f, 1.0f);
    drawQuad(vc, pose, normal, 1.0f, 0.8f, 0.2f, alpha, -radius, 0, radius, -radius, BEAM_HEIGHT, radius, 0.0f, 1.0f);
    
    poseStack.popPose();
    super.render(entity, yaw, partialTicks, poseStack, buffers, light);
  }

  // Вспомогательный метод для отрисовки одной грани луча
  private void drawQuad(VertexConsumer vc, Matrix4f pose, Matrix3f normal, float r, float g, float b, float a,
                        float x1, float y1, float z1,
                        float x2, float y2, float z2,
                        float u1, float v1) {
    
    // (x1, y1, z1) - нижний левый
    // (x1, y2, z1) - верхний левый
    // (x2, y2, z2) - верхний правый
    // (x2, y1, z2) - нижний правый

    vc.vertex(pose, x1, y1, z1).color(r, g, b, a).uv(u1, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
    vc.vertex(pose, x1, y2, z1).color(r, g, b, a).uv(u1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
    vc.vertex(pose, x2, y2, z2).color(r, g, b, a).uv(v1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
    vc.vertex(pose, x2, y1, z2).color(r, g, b, a).uv(v1, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
  }


  @Override
  public ResourceLocation getTextureLocation(OrbitalBeamEntity entity) {
    return BEAM_TEX;
  }
}

