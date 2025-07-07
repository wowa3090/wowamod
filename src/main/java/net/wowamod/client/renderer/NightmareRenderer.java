
package net.wowamod.client.renderer;

import net.wowamod.entity.NightmareEntity;
import net.wowamod.client.model.Modelnightmarefixed;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

import com.mojang.blaze3d.vertex.PoseStack;

public class NightmareRenderer extends MobRenderer<NightmareEntity, Modelnightmarefixed<NightmareEntity>> {
	public NightmareRenderer(EntityRendererProvider.Context context) {
		super(context, new Modelnightmarefixed(context.bakeLayer(Modelnightmarefixed.LAYER_LOCATION)), 0.5f);
	}

	@Override
	protected void scale(NightmareEntity entity, PoseStack poseStack, float f) {
		poseStack.scale(1.25f, 1.25f, 1.25f);
	}

	@Override
	public ResourceLocation getTextureLocation(NightmareEntity entity) {
		return new ResourceLocation("universe3090:textures/entities/texture_nightmare.png");
	}
}
