package net.wowamod.item.model;

import software.bernie.geckolib.model.GeoModel;

import net.wowamod.item.ElissaarmorItem;

import net.minecraft.resources.ResourceLocation;

public class ElissaarmorModel extends GeoModel<ElissaarmorItem> {
	@Override
	public ResourceLocation getAnimationResource(ElissaarmorItem object) {
		return new ResourceLocation("universe3090", "animations/elissaarmor.animation.json");
	}

	@Override
	public ResourceLocation getModelResource(ElissaarmorItem object) {
		return new ResourceLocation("universe3090", "geo/elissaarmor.geo.json");
	}

	@Override
	public ResourceLocation getTextureResource(ElissaarmorItem object) {
		return new ResourceLocation("universe3090", "textures/item/texture_elissa_armor.png");
	}
}
