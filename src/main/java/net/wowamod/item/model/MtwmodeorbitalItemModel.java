package net.wowamod.item.model;

import software.bernie.geckolib.model.GeoModel;

import net.wowamod.item.MtwmodeorbitalItem;

import net.minecraft.resources.ResourceLocation;

public class MtwmodeorbitalItemModel extends GeoModel<MtwmodeorbitalItem> {
	@Override
	public ResourceLocation getAnimationResource(MtwmodeorbitalItem animatable) {
		return new ResourceLocation("universe3090", "animations/m.t.w_animated.animation.json");
	}

	@Override
	public ResourceLocation getModelResource(MtwmodeorbitalItem animatable) {
		return new ResourceLocation("universe3090", "geo/m.t.w_animated.geo.json");
	}

	@Override
	public ResourceLocation getTextureResource(MtwmodeorbitalItem animatable) {
		return new ResourceLocation("universe3090", "textures/item/texture_mtw.png");
	}
}
