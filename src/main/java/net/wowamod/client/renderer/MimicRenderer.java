
package net.wowamod.client.renderer;

import net.wowamod.entity.MimicEntity;
import net.wowamod.client.model.Modelmimic;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

import com.mojang.blaze3d.vertex.PoseStack;

public class MimicRenderer extends MobRenderer<MimicEntity, Modelmimic<MimicEntity>> {
	public MimicRenderer(EntityRendererProvider.Context context) {
		super(context, new Modelmimic(context.bakeLayer(Modelmimic.LAYER_LOCATION)), 0.8f);
	}

	@Override
	protected void scale(MimicEntity entity, PoseStack poseStack, float f) {
		poseStack.scale(1.3f, 1.3f, 1.3f);
	}

	@Override
	public ResourceLocation getTextureLocation(MimicEntity entity) {
		return new ResourceLocation("universe3090:textures/entities/texturemimic.png");
	}
}
