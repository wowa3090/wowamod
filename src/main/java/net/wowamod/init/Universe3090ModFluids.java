
/*
 * MCreator note: This file will be REGENERATED on each build.
 */
package net.wowamod.init;

import net.wowamod.fluid.ExtractredsoulfluidFluid;
import net.wowamod.fluid.ExtractgolubaysoulFluid;
import net.wowamod.fluid.ExtractYellowSoulFluidFluid;
import net.wowamod.fluid.ExtractPurpleSoulFluidFluid;
import net.wowamod.fluid.ExtractOrangeSoulFluidFluid;
import net.wowamod.fluid.ExtractGreenSoulFluidFluid;
import net.wowamod.fluid.ExtractBlueSoulFluidFluid;
import net.wowamod.fluid.ActiveRedstoneFluid;
import net.wowamod.Universe3090Mod;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ItemBlockRenderTypes;

public class Universe3090ModFluids {
	public static final DeferredRegister<Fluid> REGISTRY = DeferredRegister.create(ForgeRegistries.FLUIDS, Universe3090Mod.MODID);
	public static final RegistryObject<FlowingFluid> ACTIVE_REDSTONE = REGISTRY.register("active_redstone", () -> new ActiveRedstoneFluid.Source());
	public static final RegistryObject<FlowingFluid> FLOWING_ACTIVE_REDSTONE = REGISTRY.register("flowing_active_redstone", () -> new ActiveRedstoneFluid.Flowing());
	public static final RegistryObject<FlowingFluid> EXTRACTREDSOULFLUID = REGISTRY.register("extractredsoulfluid", () -> new ExtractredsoulfluidFluid.Source());
	public static final RegistryObject<FlowingFluid> FLOWING_EXTRACTREDSOULFLUID = REGISTRY.register("flowing_extractredsoulfluid", () -> new ExtractredsoulfluidFluid.Flowing());
	public static final RegistryObject<FlowingFluid> EXTRACT_ORANGE_SOUL_FLUID = REGISTRY.register("extract_orange_soul_fluid", () -> new ExtractOrangeSoulFluidFluid.Source());
	public static final RegistryObject<FlowingFluid> FLOWING_EXTRACT_ORANGE_SOUL_FLUID = REGISTRY.register("flowing_extract_orange_soul_fluid", () -> new ExtractOrangeSoulFluidFluid.Flowing());
	public static final RegistryObject<FlowingFluid> EXTRACT_YELLOW_SOUL_FLUID = REGISTRY.register("extract_yellow_soul_fluid", () -> new ExtractYellowSoulFluidFluid.Source());
	public static final RegistryObject<FlowingFluid> FLOWING_EXTRACT_YELLOW_SOUL_FLUID = REGISTRY.register("flowing_extract_yellow_soul_fluid", () -> new ExtractYellowSoulFluidFluid.Flowing());
	public static final RegistryObject<FlowingFluid> EXTRACT_GREEN_SOUL_FLUID = REGISTRY.register("extract_green_soul_fluid", () -> new ExtractGreenSoulFluidFluid.Source());
	public static final RegistryObject<FlowingFluid> FLOWING_EXTRACT_GREEN_SOUL_FLUID = REGISTRY.register("flowing_extract_green_soul_fluid", () -> new ExtractGreenSoulFluidFluid.Flowing());
	public static final RegistryObject<FlowingFluid> EXTRACTGOLUBAYSOUL = REGISTRY.register("extractgolubaysoul", () -> new ExtractgolubaysoulFluid.Source());
	public static final RegistryObject<FlowingFluid> FLOWING_EXTRACTGOLUBAYSOUL = REGISTRY.register("flowing_extractgolubaysoul", () -> new ExtractgolubaysoulFluid.Flowing());
	public static final RegistryObject<FlowingFluid> EXTRACT_BLUE_SOUL_FLUID = REGISTRY.register("extract_blue_soul_fluid", () -> new ExtractBlueSoulFluidFluid.Source());
	public static final RegistryObject<FlowingFluid> FLOWING_EXTRACT_BLUE_SOUL_FLUID = REGISTRY.register("flowing_extract_blue_soul_fluid", () -> new ExtractBlueSoulFluidFluid.Flowing());
	public static final RegistryObject<FlowingFluid> EXTRACT_PURPLE_SOUL_FLUID = REGISTRY.register("extract_purple_soul_fluid", () -> new ExtractPurpleSoulFluidFluid.Source());
	public static final RegistryObject<FlowingFluid> FLOWING_EXTRACT_PURPLE_SOUL_FLUID = REGISTRY.register("flowing_extract_purple_soul_fluid", () -> new ExtractPurpleSoulFluidFluid.Flowing());

	@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
	public static class ClientSideHandler {
		@SubscribeEvent
		public static void clientSetup(FMLClientSetupEvent event) {
			ItemBlockRenderTypes.setRenderLayer(ACTIVE_REDSTONE.get(), RenderType.translucent());
			ItemBlockRenderTypes.setRenderLayer(FLOWING_ACTIVE_REDSTONE.get(), RenderType.translucent());
			ItemBlockRenderTypes.setRenderLayer(EXTRACTREDSOULFLUID.get(), RenderType.translucent());
			ItemBlockRenderTypes.setRenderLayer(FLOWING_EXTRACTREDSOULFLUID.get(), RenderType.translucent());
			ItemBlockRenderTypes.setRenderLayer(EXTRACT_ORANGE_SOUL_FLUID.get(), RenderType.translucent());
			ItemBlockRenderTypes.setRenderLayer(FLOWING_EXTRACT_ORANGE_SOUL_FLUID.get(), RenderType.translucent());
			ItemBlockRenderTypes.setRenderLayer(EXTRACT_YELLOW_SOUL_FLUID.get(), RenderType.translucent());
			ItemBlockRenderTypes.setRenderLayer(FLOWING_EXTRACT_YELLOW_SOUL_FLUID.get(), RenderType.translucent());
			ItemBlockRenderTypes.setRenderLayer(EXTRACT_GREEN_SOUL_FLUID.get(), RenderType.translucent());
			ItemBlockRenderTypes.setRenderLayer(FLOWING_EXTRACT_GREEN_SOUL_FLUID.get(), RenderType.translucent());
			ItemBlockRenderTypes.setRenderLayer(EXTRACTGOLUBAYSOUL.get(), RenderType.translucent());
			ItemBlockRenderTypes.setRenderLayer(FLOWING_EXTRACTGOLUBAYSOUL.get(), RenderType.translucent());
			ItemBlockRenderTypes.setRenderLayer(EXTRACT_BLUE_SOUL_FLUID.get(), RenderType.translucent());
			ItemBlockRenderTypes.setRenderLayer(FLOWING_EXTRACT_BLUE_SOUL_FLUID.get(), RenderType.translucent());
			ItemBlockRenderTypes.setRenderLayer(EXTRACT_PURPLE_SOUL_FLUID.get(), RenderType.translucent());
			ItemBlockRenderTypes.setRenderLayer(FLOWING_EXTRACT_PURPLE_SOUL_FLUID.get(), RenderType.translucent());
		}
	}
}
