package net.wowamod.procedures;

import net.wowamod.init.WowamodModItems;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.event.level.BlockEvent;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class EmeraldsgivingProcedure {
	@SubscribeEvent
	public static void onBlockBreak(BlockEvent.BreakEvent event) {
		execute(event, event.getLevel(), event.getPos().getX(), event.getPos().getY(), event.getPos().getZ());
	}

	public static void execute(LevelAccessor world, double x, double y, double z) {
		execute(null, world, x, y, z);
	}

	private static void execute(@Nullable Event event, LevelAccessor world, double x, double y, double z) {
		if ((Blocks.DEEPSLATE_REDSTONE_ORE == (world.getBlockState(BlockPos.containing(x, y, z))).getBlock() || Blocks.REDSTONE_ORE == (world.getBlockState(BlockPos.containing(x, y, z))).getBlock()) && Math.random() <= 0.2) {
			if (world instanceof ServerLevel _level) {
				ItemEntity entityToSpawn = new ItemEntity(_level, x, y, z, new ItemStack(WowamodModItems.REDEMERALD.get()));
				entityToSpawn.setPickUpDelay(10);
				_level.addFreshEntity(entityToSpawn);
			}
		} else if ((Blocks.LAPIS_ORE == (world.getBlockState(BlockPos.containing(x, y, z))).getBlock() || Blocks.DEEPSLATE_LAPIS_ORE == (world.getBlockState(BlockPos.containing(x, y, z))).getBlock()) && Math.random() <= 0.2) {
			if (world instanceof ServerLevel _level) {
				ItemEntity entityToSpawn = new ItemEntity(_level, x, y, z, new ItemStack(WowamodModItems.BLUEEMERALD.get()));
				entityToSpawn.setPickUpDelay(10);
				_level.addFreshEntity(entityToSpawn);
			}
		} else if ((Blocks.DIAMOND_ORE == (world.getBlockState(BlockPos.containing(x, y, z))).getBlock() || Blocks.DEEPSLATE_DIAMOND_ORE == (world.getBlockState(BlockPos.containing(x, y, z))).getBlock()) && Math.random() <= 0.4) {
			if (world instanceof ServerLevel _level) {
				ItemEntity entityToSpawn = new ItemEntity(_level, x, y, z, new ItemStack(WowamodModItems.LIGHTBLUEEMERALD.get()));
				entityToSpawn.setPickUpDelay(10);
				_level.addFreshEntity(entityToSpawn);
			}
		} else if ((Blocks.IRON_ORE == (world.getBlockState(BlockPos.containing(x, y, z))).getBlock() || Blocks.DEEPSLATE_IRON_ORE == (world.getBlockState(BlockPos.containing(x, y, z))).getBlock()) && Math.random() <= 0.1) {
			if (world instanceof ServerLevel _level) {
				ItemEntity entityToSpawn = new ItemEntity(_level, x, y, z, new ItemStack(WowamodModItems.WHITEEMERALD.get()));
				entityToSpawn.setPickUpDelay(10);
				_level.addFreshEntity(entityToSpawn);
			}
		} else if ((Blocks.EMERALD_ORE == (world.getBlockState(BlockPos.containing(x, y, z))).getBlock() || Blocks.DEEPSLATE_EMERALD_ORE == (world.getBlockState(BlockPos.containing(x, y, z))).getBlock()) && Math.random() <= 0.55) {
			if (world instanceof ServerLevel _level) {
				ItemEntity entityToSpawn = new ItemEntity(_level, x, y, z, new ItemStack(WowamodModItems.GREENEMERALD.get()));
				entityToSpawn.setPickUpDelay(10);
				_level.addFreshEntity(entityToSpawn);
			}
		} else if ((Blocks.GOLD_ORE == (world.getBlockState(BlockPos.containing(x, y, z))).getBlock() || Blocks.DEEPSLATE_GOLD_ORE == (world.getBlockState(BlockPos.containing(x, y, z))).getBlock()) && Math.random() <= 0.3) {
			if (world instanceof ServerLevel _level) {
				ItemEntity entityToSpawn = new ItemEntity(_level, x, y, z, new ItemStack(WowamodModItems.YELLOWEMERALD.get()));
				entityToSpawn.setPickUpDelay(10);
				_level.addFreshEntity(entityToSpawn);
			}
		} else if ((Blocks.AMETHYST_BLOCK == (world.getBlockState(BlockPos.containing(x, y, z))).getBlock() || Blocks.DEEPSLATE_GOLD_ORE == (world.getBlockState(BlockPos.containing(x, y, z))).getBlock()) && Math.random() <= 0.15) {
			if (world instanceof ServerLevel _level) {
				ItemEntity entityToSpawn = new ItemEntity(_level, x, y, z, new ItemStack(WowamodModItems.PURPLEEMERALD.get()));
				entityToSpawn.setPickUpDelay(10);
				_level.addFreshEntity(entityToSpawn);
			}
		}
	}
}
