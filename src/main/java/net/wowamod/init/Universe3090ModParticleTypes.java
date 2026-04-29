
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.wowamod.init;

import net.wowamod.Universe3090Mod;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.particles.ParticleType;

public class Universe3090ModParticleTypes {
	public static final DeferredRegister<ParticleType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Universe3090Mod.MODID);
	public static final RegistryObject<SimpleParticleType> TRAILOFROSE_2 = REGISTRY.register("trailofrose_2", () -> new SimpleParticleType(false));
	public static final RegistryObject<SimpleParticleType> TESTLASERPARTICLE = REGISTRY.register("testlaserparticle", () -> new SimpleParticleType(true));
	public static final RegistryObject<SimpleParticleType> SUPERFORMPARTICLE = REGISTRY.register("superformparticle", () -> new SimpleParticleType(true));
}
