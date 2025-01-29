
/*
 * MCreator note: This file will be REGENERATED on each build.
 */
package net.wowamod.init;

import net.wowamod.fluid.ExtractgolubaysoulFluid;
import net.wowamod.WowamodMod;

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

public class WowamodModFluids {
	public static final DeferredRegister<Fluid> REGISTRY = DeferredRegister.create(ForgeRegistries.FLUIDS, WowamodMod.MODID);
	public static final RegistryObject<FlowingFluid> EXTRACTGOLUBAYSOUL = REGISTRY.register("extractgolubaysoul", () -> new ExtractgolubaysoulFluid.Source());
	public static final RegistryObject<FlowingFluid> FLOWING_EXTRACTGOLUBAYSOUL = REGISTRY.register("flowing_extractgolubaysoul", () -> new ExtractgolubaysoulFluid.Flowing());

	@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
	public static class ClientSideHandler {
		@SubscribeEvent
		public static void clientSetup(FMLClientSetupEvent event) {
			ItemBlockRenderTypes.setRenderLayer(EXTRACTGOLUBAYSOUL.get(), RenderType.translucent());
			ItemBlockRenderTypes.setRenderLayer(FLOWING_EXTRACTGOLUBAYSOUL.get(), RenderType.translucent());
		}
	}
}
