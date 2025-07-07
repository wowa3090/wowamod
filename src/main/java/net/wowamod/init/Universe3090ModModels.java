
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.wowamod.init;

import net.wowamod.client.model.Modelwbronyaleg;
import net.wowamod.client.model.Modelwbronyahead;
import net.wowamod.client.model.Modelwbronyabotinki;
import net.wowamod.client.model.Modelnightmarefixed;
import net.wowamod.client.model.Modelnightmare;
import net.wowamod.client.model.Modelmimic;
import net.wowamod.client.model.Modelfullwowa;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.api.distmarker.Dist;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = {Dist.CLIENT})
public class Universe3090ModModels {
	@SubscribeEvent
	public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(Modelnightmarefixed.LAYER_LOCATION, Modelnightmarefixed::createBodyLayer);
		event.registerLayerDefinition(Modelwbronyahead.LAYER_LOCATION, Modelwbronyahead::createBodyLayer);
		event.registerLayerDefinition(Modelwbronyaleg.LAYER_LOCATION, Modelwbronyaleg::createBodyLayer);
		event.registerLayerDefinition(Modelmimic.LAYER_LOCATION, Modelmimic::createBodyLayer);
		event.registerLayerDefinition(Modelwbronyabotinki.LAYER_LOCATION, Modelwbronyabotinki::createBodyLayer);
		event.registerLayerDefinition(Modelfullwowa.LAYER_LOCATION, Modelfullwowa::createBodyLayer);
		event.registerLayerDefinition(Modelnightmare.LAYER_LOCATION, Modelnightmare::createBodyLayer);
	}
}
