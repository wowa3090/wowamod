package net.wowamod.entity.model;

import software.bernie.geckolib.model.GeoModel;

import net.wowamod.entity.MimicEntity;

import net.minecraft.resources.ResourceLocation;

public class MimicModel extends GeoModel<MimicEntity> {
	@Override
	public ResourceLocation getAnimationResource(MimicEntity entity) {
		return new ResourceLocation("universe3090", "animations/mimic.animation.json");
	}

	@Override
	public ResourceLocation getModelResource(MimicEntity entity) {
		return new ResourceLocation("universe3090", "geo/mimic.geo.json");
	}

	@Override
	public ResourceLocation getTextureResource(MimicEntity entity) {
		return new ResourceLocation("universe3090", "textures/entities/" + entity.getTexture() + ".png");
	}

}
