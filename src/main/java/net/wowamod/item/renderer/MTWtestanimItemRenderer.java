package net.wowamod.item.renderer;

import software.bernie.geckolib.renderer.GeoItemRenderer;
import net.wowamod.item.model.MTWtestanimItemModel;
import net.wowamod.item.MTWtestanimItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource;

public class MTWtestanimItemRenderer extends GeoItemRenderer<MTWtestanimItem> {
	
	public MTWtestanimItemRenderer() {
		super(new MTWtestanimItemModel());
	}

	@Override
	public RenderType getRenderType(MTWtestanimItem animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
		return RenderType.entityTranslucent(getTextureLocation(animatable));
	}
}