
/*
 * MCreator note: This file will be REGENERATED on each build.
 */
package net.wowamod.init;

import net.wowamod.fluid.types.ExtractredsoulfluidFluidType;
import net.wowamod.fluid.types.ExtractgolubaysoulFluidType;
import net.wowamod.fluid.types.ExtractYellowSoulFluidFluidType;
import net.wowamod.fluid.types.ExtractPurpleSoulFluidFluidType;
import net.wowamod.fluid.types.ExtractOrangeSoulFluidFluidType;
import net.wowamod.fluid.types.ExtractGreenSoulFluidFluidType;
import net.wowamod.fluid.types.ExtractBlueSoulFluidFluidType;
import net.wowamod.fluid.types.ActiveRedstoneFluidType;
import net.wowamod.Universe3090Mod;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.fluids.FluidType;

public class Universe3090ModFluidTypes {
	public static final DeferredRegister<FluidType> REGISTRY = DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, Universe3090Mod.MODID);
	public static final RegistryObject<FluidType> ACTIVE_REDSTONE_TYPE = REGISTRY.register("active_redstone", () -> new ActiveRedstoneFluidType());
	public static final RegistryObject<FluidType> EXTRACTREDSOULFLUID_TYPE = REGISTRY.register("extractredsoulfluid", () -> new ExtractredsoulfluidFluidType());
	public static final RegistryObject<FluidType> EXTRACT_ORANGE_SOUL_FLUID_TYPE = REGISTRY.register("extract_orange_soul_fluid", () -> new ExtractOrangeSoulFluidFluidType());
	public static final RegistryObject<FluidType> EXTRACT_YELLOW_SOUL_FLUID_TYPE = REGISTRY.register("extract_yellow_soul_fluid", () -> new ExtractYellowSoulFluidFluidType());
	public static final RegistryObject<FluidType> EXTRACT_GREEN_SOUL_FLUID_TYPE = REGISTRY.register("extract_green_soul_fluid", () -> new ExtractGreenSoulFluidFluidType());
	public static final RegistryObject<FluidType> EXTRACTGOLUBAYSOUL_TYPE = REGISTRY.register("extractgolubaysoul", () -> new ExtractgolubaysoulFluidType());
	public static final RegistryObject<FluidType> EXTRACT_BLUE_SOUL_FLUID_TYPE = REGISTRY.register("extract_blue_soul_fluid", () -> new ExtractBlueSoulFluidFluidType());
	public static final RegistryObject<FluidType> EXTRACT_PURPLE_SOUL_FLUID_TYPE = REGISTRY.register("extract_purple_soul_fluid", () -> new ExtractPurpleSoulFluidFluidType());
}
