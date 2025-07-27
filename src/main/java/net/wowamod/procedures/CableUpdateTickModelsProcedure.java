package net.wowamod.procedures;

import net.wowamod.init.Universe3090ModBlocks;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.core.BlockPos;

public class CableUpdateTickModelsProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z) {
		if (!world.isClientSide()) {
			if (!((world.getBlockState(BlockPos.containing(x, y, z))).getBlock() == Universe3090ModBlocks.CABLE_N.get())) {
				CableUpdateTickModelsNProcedure.execute(world, x, y, z);
			}
			if (!((world.getBlockState(BlockPos.containing(x, y, z))).getBlock() == Universe3090ModBlocks.CABLE_E.get())) {
				CableUpdateTickModelsEProcedure.execute(world, x, y, z);
			}
			if (!((world.getBlockState(BlockPos.containing(x, y, z))).getBlock() == Universe3090ModBlocks.CABLE_I.get())) {
				CableUpdateTickModelsIProcedure.execute(world, x, y, z);
			}
			if (!((world.getBlockState(BlockPos.containing(x, y, z))).getBlock() == Universe3090ModBlocks.CABLE_L.get())) {
				CableUpdateTickModelsLProcedure.execute(world, x, y, z);
			}
			if (!((world.getBlockState(BlockPos.containing(x, y, z))).getBlock() == Universe3090ModBlocks.CABLE_LC.get())) {
				CableUpdateTickModelsLCProcedure.execute(world, x, y, z);
			}
			if (!((world.getBlockState(BlockPos.containing(x, y, z))).getBlock() == Universe3090ModBlocks.CABLE_LCC.get())) {
				CableUpdateTickModelsLCCProcedure.execute(world, x, y, z);
			}
			if (!((world.getBlockState(BlockPos.containing(x, y, z))).getBlock() == Universe3090ModBlocks.CABLE_LT.get())) {
				CableUpdateTickModelsLTProcedure.execute(world, x, y, z);
			}
			if (!((world.getBlockState(BlockPos.containing(x, y, z))).getBlock() == Universe3090ModBlocks.CABLE_LTC.get())) {
				CableUpdateTickModelsLTCProcedure.execute(world, x, y, z);
			}
			if (!((world.getBlockState(BlockPos.containing(x, y, z))).getBlock() == Universe3090ModBlocks.CABLE_T.get())) {
				CableUpdateTickModelsTProcedure.execute(world, x, y, z);
			}
			if (!((world.getBlockState(BlockPos.containing(x, y, z))).getBlock() == Universe3090ModBlocks.CABLE_TC.get())) {
				CableUpdateTickModelsTCProcedure.execute(world, x, y, z);
			}
			if (!((world.getBlockState(BlockPos.containing(x, y, z))).getBlock() == Universe3090ModBlocks.CABLE_TX.get())) {
				CableUpdateTickModelsTXProcedure.execute(world, x, y, z);
			}
			if (!((world.getBlockState(BlockPos.containing(x, y, z))).getBlock() == Universe3090ModBlocks.CABLE_TXC.get())) {
				CableUpdateTickModelsTXCProcedure.execute(world, x, y, z);
			}
			if (!((world.getBlockState(BlockPos.containing(x, y, z))).getBlock() == Universe3090ModBlocks.CABLE_TXCC.get())) {
				CableUpdateTickModelsTXCCProcedure.execute(world, x, y, z);
			}
			if (!((world.getBlockState(BlockPos.containing(x, y, z))).getBlock() == Universe3090ModBlocks.CABLE_X.get())) {
				CableUpdateTickModelsXProcedure.execute(world, x, y, z);
			}
			if (!((world.getBlockState(BlockPos.containing(x, y, z))).getBlock() == Universe3090ModBlocks.CABLE_XC.get())) {
				CableUpdateTickModelsXCProcedure.execute(world, x, y, z);
			}
			if (!((world.getBlockState(BlockPos.containing(x, y, z))).getBlock() == Universe3090ModBlocks.CABLE_F.get())) {
				CableUpdateTickModelsFProcedure.execute(world, x, y, z);
			}
			if (!((world.getBlockState(BlockPos.containing(x, y, z))).getBlock() == Universe3090ModBlocks.CABLE_S.get())) {
				CableUpdateTickModelsSProcedure.execute(world, x, y, z);
			}
		}
	}
}
