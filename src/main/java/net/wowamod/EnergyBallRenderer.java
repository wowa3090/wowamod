package net.wowamod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.wowamod.entity.EnergyBallEntity;

public class EnergyBallRenderer extends EntityRenderer<EnergyBallEntity> {
    // Я заменил Universe3090Mod.MOD_ID на строку, чтобы избежать ошибки "cannot find symbol"
    private static final ResourceLocation TEXTURE = new ResourceLocation("universe3090", "textures/entity/energy_ball.png");

    public EnergyBallRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(EnergyBallEntity entity, float entityYaw, float partialTicks, 
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        
        // Позиционирование
        poseStack.translate(0.0, 0.2, 0.0);
        
        // Вращение шара (теперь tickCount публичный благодаря наследованию от Entity)
        poseStack.mulPose(Axis.YP.rotationDegrees(entity.tickCount * 10));
        poseStack.mulPose(Axis.XP.rotationDegrees(entity.tickCount * 5));

        // Отрисовка
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(EnergyBallEntity entity) {
        return TEXTURE;
    }
}