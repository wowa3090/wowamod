package net.wowamod.entity.model;

import software.bernie.geckolib.model.GeoModel;

import net.wowamod.entity.DarkHeadEntity;

import net.minecraft.resources.ResourceLocation;

public class DarkHeadModel extends GeoModel<DarkHeadEntity> {
	@Override
	public ResourceLocation getAnimationResource(DarkHeadEntity entity) {
		return new ResourceLocation("universe3090", "animations/darkheadmyplace.animation.json");
	}

	@Override
	public ResourceLocation getModelResource(DarkHeadEntity entity) {
		return new ResourceLocation("universe3090", "geo/darkheadmyplace.geo.json");
	}

	@Override
	public ResourceLocation getTextureResource(DarkHeadEntity entity) {
		return new ResourceLocation("universe3090", "textures/entities/" + entity.getTexture() + ".png");
	}

}
