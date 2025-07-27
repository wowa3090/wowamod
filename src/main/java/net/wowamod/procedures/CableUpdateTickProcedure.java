package net.wowamod.procedures;

import net.minecraft.world.level.LevelAccessor;

public class CableUpdateTickProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z) {
		if (!world.isClientSide()) {
			CableUpdateTickModelsProcedure.execute(world, x, y, z);
			CableUpdateTickEnergyProcedure.execute(world, x, y, z);
		}
	}
}
