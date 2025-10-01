
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.wowamod.init;

import net.wowamod.client.renderer.NightmareRenderer;
import net.wowamod.client.renderer.MimicRenderer;
import net.wowamod.client.renderer.DarkHeadRenderer;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.api.distmarker.Dist;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class Universe3090ModEntityRenderers {
	@SubscribeEvent
	public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(Universe3090ModEntities.NIGHTMARE.get(), NightmareRenderer::new);
		event.registerEntityRenderer(Universe3090ModEntities.MIMIC.get(), MimicRenderer::new);
		event.registerEntityRenderer(Universe3090ModEntities.DARK_HEAD.get(), DarkHeadRenderer::new);
	}
}
