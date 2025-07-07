package net.wowamod.procedures;

import net.wowamod.init.Universe3090ModBlocks;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.core.BlockPos;

public class RedstoneportRiedstounVkliuchionProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z) {
		if ((world.getBlockState(BlockPos.containing(x + 1, y + 1, z))).getBlock() == Universe3090ModBlocks.REDSTONECOMMANDBLOCK.get()
				|| (world.getBlockState(BlockPos.containing(x - 1, y + 1, z))).getBlock() == Universe3090ModBlocks.REDSTONECOMMANDBLOCK.get()
				|| (world.getBlockState(BlockPos.containing(x, y + 1, z + 1))).getBlock() == Universe3090ModBlocks.REDSTONECOMMANDBLOCK.get()
				|| (world.getBlockState(BlockPos.containing(x, y + 1, z - 1))).getBlock() == Universe3090ModBlocks.REDSTONECOMMANDBLOCK.get()) {
			if ((world.getBlockState(BlockPos.containing(x, y + 1, z))).getBlock() == Universe3090ModBlocks.INTERFACECONSTRUCTOR.get()) {
				ConstructorcreatewowaswordProcedure.execute(world, x, y, z);
			}
		}
	}
}
