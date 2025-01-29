
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.wowamod.init;

import net.wowamod.WowamodMod;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.particles.ParticleType;

public class WowamodModParticleTypes {
	public static final DeferredRegister<ParticleType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, WowamodMod.MODID);
	public static final RegistryObject<SimpleParticleType> TRAILOFROSE_2 = REGISTRY.register("trailofrose_2", () -> new SimpleParticleType(true));
}
