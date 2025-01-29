package net.wowamod.procedures;

import net.wowamod.init.WowamodModBlocks;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.core.BlockPos;

public class RedstoneportRiedstounVkliuchionProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z) {
		if ((world.getBlockState(BlockPos.containing(x + 1, y + 1, z))).getBlock() == WowamodModBlocks.REDSTONECOMMANDBLOCK.get() || (world.getBlockState(BlockPos.containing(x - 1, y + 1, z))).getBlock() == WowamodModBlocks.REDSTONECOMMANDBLOCK.get()
				|| (world.getBlockState(BlockPos.containing(x, y + 1, z + 1))).getBlock() == WowamodModBlocks.REDSTONECOMMANDBLOCK.get()
				|| (world.getBlockState(BlockPos.containing(x, y + 1, z - 1))).getBlock() == WowamodModBlocks.REDSTONECOMMANDBLOCK.get()) {
			if ((world.getBlockState(BlockPos.containing(x, y + 1, z))).getBlock() == WowamodModBlocks.INTERFACECONSTRUCTOR.get()) {
				ConstructorcreatewowaswordProcedure.execute(world, x, y, z);
			}
		}
	}
}
