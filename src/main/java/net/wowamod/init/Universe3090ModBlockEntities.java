
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.wowamod.init;

import net.wowamod.block.entity.SolarpanelgeneratorwBlockEntity;
import net.wowamod.block.entity.SmelterblockBlockEntity;
import net.wowamod.block.entity.PortconstructorBlockEntity;
import net.wowamod.block.entity.InterfaceconstructorBlockEntity;
import net.wowamod.block.entity.ExtractorBlockEntity;
import net.wowamod.block.entity.EmeraldCombinerBlockEntity;
import net.wowamod.block.entity.CrafterbroniBlockEntity;
import net.wowamod.block.entity.CableXCBlockEntity;
import net.wowamod.block.entity.CableXBlockEntity;
import net.wowamod.block.entity.CableTXCCBlockEntity;
import net.wowamod.block.entity.CableTXBlockEntity;
import net.wowamod.block.entity.CableTVCBlockEntity;
import net.wowamod.block.entity.CableTCBlockEntity;
import net.wowamod.block.entity.CableTBlockEntity;
import net.wowamod.block.entity.CableSBlockEntity;
import net.wowamod.block.entity.CableNBlockEntity;
import net.wowamod.block.entity.CableLTCBlockEntity;
import net.wowamod.block.entity.CableLTBlockEntity;
import net.wowamod.block.entity.CableLCCBlockEntity;
import net.wowamod.block.entity.CableLCBlockEntity;
import net.wowamod.block.entity.CableLBlockEntity;
import net.wowamod.block.entity.CableIBlockEntity;
import net.wowamod.block.entity.CableFBlockEntity;
import net.wowamod.block.entity.CableEBlockEntity;
import net.wowamod.block.entity.BigBatterywBlockEntity;
import net.wowamod.Universe3090Mod;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.Block;

public class Universe3090ModBlockEntities {
	public static final DeferredRegister<BlockEntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Universe3090Mod.MODID);
	public static final RegistryObject<BlockEntityType<?>> CRAFTERBRONI = register("crafterbroni", Universe3090ModBlocks.CRAFTERBRONI, CrafterbroniBlockEntity::new);
	public static final RegistryObject<BlockEntityType<?>> EMERALD_COMBINER = register("emerald_combiner", Universe3090ModBlocks.EMERALD_COMBINER, EmeraldCombinerBlockEntity::new);
	public static final RegistryObject<BlockEntityType<?>> BIG_BATTERYW = register("big_batteryw", Universe3090ModBlocks.BIG_BATTERYW, BigBatterywBlockEntity::new);
	public static final RegistryObject<BlockEntityType<?>> PORTCONSTRUCTOR = register("portconstructor", Universe3090ModBlocks.PORTCONSTRUCTOR, PortconstructorBlockEntity::new);
	public static final RegistryObject<BlockEntityType<?>> INTERFACECONSTRUCTOR = register("interfaceconstructor", Universe3090ModBlocks.INTERFACECONSTRUCTOR, InterfaceconstructorBlockEntity::new);
	public static final RegistryObject<BlockEntityType<?>> EXTRACTOR = register("extractor", Universe3090ModBlocks.EXTRACTOR, ExtractorBlockEntity::new);
	public static final RegistryObject<BlockEntityType<?>> SOLARPANELGENERATORW = register("solarpanelgeneratorw", Universe3090ModBlocks.SOLARPANELGENERATORW, SolarpanelgeneratorwBlockEntity::new);
	public static final RegistryObject<BlockEntityType<?>> CABLE_N = register("cable_n", Universe3090ModBlocks.CABLE_N, CableNBlockEntity::new);
	public static final RegistryObject<BlockEntityType<?>> CABLE_E = register("cable_e", Universe3090ModBlocks.CABLE_E, CableEBlockEntity::new);
	public static final RegistryObject<BlockEntityType<?>> CABLE_I = register("cable_i", Universe3090ModBlocks.CABLE_I, CableIBlockEntity::new);
	public static final RegistryObject<BlockEntityType<?>> CABLE_F = register("cable_f", Universe3090ModBlocks.CABLE_F, CableFBlockEntity::new);
	public static final RegistryObject<BlockEntityType<?>> CABLE_L = register("cable_l", Universe3090ModBlocks.CABLE_L, CableLBlockEntity::new);
	public static final RegistryObject<BlockEntityType<?>> CABLE_LC = register("cable_lc", Universe3090ModBlocks.CABLE_LC, CableLCBlockEntity::new);
	public static final RegistryObject<BlockEntityType<?>> CABLE_LCC = register("cable_lcc", Universe3090ModBlocks.CABLE_LCC, CableLCCBlockEntity::new);
	public static final RegistryObject<BlockEntityType<?>> CABLE_LT = register("cable_lt", Universe3090ModBlocks.CABLE_LT, CableLTBlockEntity::new);
	public static final RegistryObject<BlockEntityType<?>> CABLE_LTC = register("cable_ltc", Universe3090ModBlocks.CABLE_LTC, CableLTCBlockEntity::new);
	public static final RegistryObject<BlockEntityType<?>> CABLE_T = register("cable_t", Universe3090ModBlocks.CABLE_T, CableTBlockEntity::new);
	public static final RegistryObject<BlockEntityType<?>> CABLE_TC = register("cable_tc", Universe3090ModBlocks.CABLE_TC, CableTCBlockEntity::new);
	public static final RegistryObject<BlockEntityType<?>> CABLE_TX = register("cable_tx", Universe3090ModBlocks.CABLE_TX, CableTXBlockEntity::new);
	public static final RegistryObject<BlockEntityType<?>> CABLE_TXC = register("cable_txc", Universe3090ModBlocks.CABLE_TXC, CableTVCBlockEntity::new);
	public static final RegistryObject<BlockEntityType<?>> CABLE_TXCC = register("cable_txcc", Universe3090ModBlocks.CABLE_TXCC, CableTXCCBlockEntity::new);
	public static final RegistryObject<BlockEntityType<?>> CABLE_X = register("cable_x", Universe3090ModBlocks.CABLE_X, CableXBlockEntity::new);
	public static final RegistryObject<BlockEntityType<?>> CABLE_XC = register("cable_xc", Universe3090ModBlocks.CABLE_XC, CableXCBlockEntity::new);
	public static final RegistryObject<BlockEntityType<?>> CABLE_S = register("cable_s", Universe3090ModBlocks.CABLE_S, CableSBlockEntity::new);
	public static final RegistryObject<BlockEntityType<?>> SMELTERBLOCK = register("smelterblock", Universe3090ModBlocks.SMELTERBLOCK, SmelterblockBlockEntity::new);

	private static RegistryObject<BlockEntityType<?>> register(String registryname, RegistryObject<Block> block, BlockEntityType.BlockEntitySupplier<?> supplier) {
		return REGISTRY.register(registryname, () -> BlockEntityType.Builder.of(supplier, block.get()).build(null));
	}
}
