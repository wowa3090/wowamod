package net.wowamod.item.model;

import software.bernie.geckolib.model.GeoModel;

import net.wowamod.item.MTWtestanimItem;

import net.minecraft.resources.ResourceLocation;

public class MTWtestanimItemModel extends GeoModel<MTWtestanimItem> {
	@Override
	public ResourceLocation getAnimationResource(MTWtestanimItem animatable) {
		return new ResourceLocation("universe3090", "animations/m.t.w_animated.animation.json");
	}

	@Override
	public ResourceLocation getModelResource(MTWtestanimItem animatable) {
		return new ResourceLocation("universe3090", "geo/m.t.w_animated.geo.json");
	}

	@Override
	public ResourceLocation getTextureResource(MTWtestanimItem animatable) {
		return new ResourceLocation("universe3090", "textures/item/texture_mtw.png");
	}
}
