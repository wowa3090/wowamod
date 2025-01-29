
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.wowamod.init;

import net.wowamod.client.renderer.NightmareRenderer;
import net.wowamod.client.renderer.MimicRenderer;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.api.distmarker.Dist;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class WowamodModEntityRenderers {
	@SubscribeEvent
	public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(WowamodModEntities.MIMIC.get(), MimicRenderer::new);
		event.registerEntityRenderer(WowamodModEntities.NIGHTMARE.get(), NightmareRenderer::new);
	}
}
