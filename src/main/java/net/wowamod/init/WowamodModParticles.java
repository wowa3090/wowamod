
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.wowamod.init;

import net.wowamod.client.particle.Trailofrose2Particle;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.api.distmarker.Dist;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class WowamodModParticles {
	@SubscribeEvent
	public static void registerParticles(RegisterParticleProvidersEvent event) {
		event.registerSpriteSet(WowamodModParticleTypes.TRAILOFROSE_2.get(), Trailofrose2Particle::provider);
	}
}
