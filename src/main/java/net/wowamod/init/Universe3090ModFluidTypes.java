
/*
 * MCreator note: This file will be REGENERATED on each build.
 */
package net.wowamod.init;

import net.wowamod.fluid.types.ExtractgolubaysoulFluidType;
import net.wowamod.Universe3090Mod;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.fluids.FluidType;

public class Universe3090ModFluidTypes {
	public static final DeferredRegister<FluidType> REGISTRY = DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, Universe3090Mod.MODID);
	public static final RegistryObject<FluidType> EXTRACTGOLUBAYSOUL_TYPE = REGISTRY.register("extractgolubaysoul", () -> new ExtractgolubaysoulFluidType());
}
