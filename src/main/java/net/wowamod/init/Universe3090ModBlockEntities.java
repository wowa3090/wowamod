
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.wowamod.init;

import net.wowamod.block.entity.SolarpanelgeneratorwBlockEntity;
import net.wowamod.block.entity.PortconstructorBlockEntity;
import net.wowamod.block.entity.InterfaceconstructorBlockEntity;
import net.wowamod.block.entity.ExtractorBlockEntity;
import net.wowamod.block.entity.EmeraldCombinerBlockEntity;
import net.wowamod.block.entity.CrafterbroniBlockEntity;
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

	private static RegistryObject<BlockEntityType<?>> register(String registryname, RegistryObject<Block> block, BlockEntityType.BlockEntitySupplier<?> supplier) {
		return REGISTRY.register(registryname, () -> BlockEntityType.Builder.of(supplier, block.get()).build(null));
	}
}
