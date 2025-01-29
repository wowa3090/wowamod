
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.wowamod.init;

import net.wowamod.block.TrueroseBlock;
import net.wowamod.block.RedstoneportBlock;
import net.wowamod.block.RedstonecommandblockBlock;
import net.wowamod.block.PortconstructorBlock;
import net.wowamod.block.MyplacezonePortalBlock;
import net.wowamod.block.MagicironblockBlock;
import net.wowamod.block.InterfaceconstructorBlock;
import net.wowamod.block.ExtractorBlock;
import net.wowamod.block.ExtractgolubaysoulBlock;
import net.wowamod.block.CrafterbroniBlock;
import net.wowamod.block.CorruptedSoulsBlockBlock;
import net.wowamod.block.BlockforprofessiaBlock;
import net.wowamod.block.BlackWallsMyplaceBlock;
import net.wowamod.WowamodMod;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.level.block.Block;

public class WowamodModBlocks {
	public static final DeferredRegister<Block> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS, WowamodMod.MODID);
	public static final RegistryObject<Block> CRAFTERBRONI = REGISTRY.register("crafterbroni", () -> new CrafterbroniBlock());
	public static final RegistryObject<Block> BLOCKFORPROFESSIA = REGISTRY.register("blockforprofessia", () -> new BlockforprofessiaBlock());
	public static final RegistryObject<Block> REDSTONECOMMANDBLOCK = REGISTRY.register("redstonecommandblock", () -> new RedstonecommandblockBlock());
	public static final RegistryObject<Block> MAGICIRONBLOCK = REGISTRY.register("magicironblock", () -> new MagicironblockBlock());
	public static final RegistryObject<Block> PORTCONSTRUCTOR = REGISTRY.register("portconstructor", () -> new PortconstructorBlock());
	public static final RegistryObject<Block> INTERFACECONSTRUCTOR = REGISTRY.register("interfaceconstructor", () -> new InterfaceconstructorBlock());
	public static final RegistryObject<Block> REDSTONEPORT = REGISTRY.register("redstoneport", () -> new RedstoneportBlock());
	public static final RegistryObject<Block> EXTRACTOR = REGISTRY.register("extractor", () -> new ExtractorBlock());
	public static final RegistryObject<Block> EXTRACTGOLUBAYSOUL = REGISTRY.register("extractgolubaysoul", () -> new ExtractgolubaysoulBlock());
	public static final RegistryObject<Block> CORRUPTED_SOULS_BLOCK = REGISTRY.register("corrupted_souls_block", () -> new CorruptedSoulsBlockBlock());
	public static final RegistryObject<Block> BLACK_WALLS_MYPLACE = REGISTRY.register("black_walls_myplace", () -> new BlackWallsMyplaceBlock());
	public static final RegistryObject<Block> MYPLACEZONE_PORTAL = REGISTRY.register("myplacezone_portal", () -> new MyplacezonePortalBlock());
	public static final RegistryObject<Block> TRUEROSE = REGISTRY.register("truerose", () -> new TrueroseBlock());

	@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
	public static class ClientSideHandler {
		@SubscribeEvent
		public static void blockColorLoad(RegisterColorHandlersEvent.Block event) {
			TrueroseBlock.blockColorLoad(event);
		}

		@SubscribeEvent
		public static void itemColorLoad(RegisterColorHandlersEvent.Item event) {
			TrueroseBlock.itemColorLoad(event);
		}
	}
}
