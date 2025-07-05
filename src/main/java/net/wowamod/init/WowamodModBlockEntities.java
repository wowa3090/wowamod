
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.wowamod.init;

import net.wowamod.block.entity.PortconstructorBlockEntity;
import net.wowamod.block.entity.InterfaceconstructorBlockEntity;
import net.wowamod.block.entity.ExtractorBlockEntity;
import net.wowamod.block.entity.EmeraldCombinerBlockEntity;
import net.wowamod.block.entity.CrafterbroniBlockEntity;
import net.wowamod.block.entity.BigBatterywBlockEntity;
import net.wowamod.WowamodMod;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.Block;

public class WowamodModBlockEntities {
	public static final DeferredRegister<BlockEntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, WowamodMod.MODID);
	public static final RegistryObject<BlockEntityType<?>> CRAFTERBRONI = register("crafterbroni", WowamodModBlocks.CRAFTERBRONI, CrafterbroniBlockEntity::new);
	public static final RegistryObject<BlockEntityType<?>> EMERALD_COMBINER = register("emerald_combiner", WowamodModBlocks.EMERALD_COMBINER, EmeraldCombinerBlockEntity::new);
	public static final RegistryObject<BlockEntityType<?>> BIG_BATTERYW = register("big_batteryw", WowamodModBlocks.BIG_BATTERYW, BigBatterywBlockEntity::new);
	public static final RegistryObject<BlockEntityType<?>> PORTCONSTRUCTOR = register("portconstructor", WowamodModBlocks.PORTCONSTRUCTOR, PortconstructorBlockEntity::new);
	public static final RegistryObject<BlockEntityType<?>> INTERFACECONSTRUCTOR = register("interfaceconstructor", WowamodModBlocks.INTERFACECONSTRUCTOR, InterfaceconstructorBlockEntity::new);
	public static final RegistryObject<BlockEntityType<?>> EXTRACTOR = register("extractor", WowamodModBlocks.EXTRACTOR, ExtractorBlockEntity::new);

	private static RegistryObject<BlockEntityType<?>> register(String registryname, RegistryObject<Block> block, BlockEntityType.BlockEntitySupplier<?> supplier) {
		return REGISTRY.register(registryname, () -> BlockEntityType.Builder.of(supplier, block.get()).build(null));
	}
}
