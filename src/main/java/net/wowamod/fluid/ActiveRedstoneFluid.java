
package net.wowamod.fluid;

import net.wowamod.init.Universe3090ModItems;
import net.wowamod.init.Universe3090ModFluids;
import net.wowamod.init.Universe3090ModFluidTypes;
import net.wowamod.init.Universe3090ModBlocks;

import net.minecraftforge.fluids.ForgeFlowingFluid;

import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.LiquidBlock;

public abstract class ActiveRedstoneFluid extends ForgeFlowingFluid {
	public static final ForgeFlowingFluid.Properties PROPERTIES = new ForgeFlowingFluid.Properties(() -> Universe3090ModFluidTypes.ACTIVE_REDSTONE_TYPE.get(), () -> Universe3090ModFluids.ACTIVE_REDSTONE.get(),
			() -> Universe3090ModFluids.FLOWING_ACTIVE_REDSTONE.get()).explosionResistance(100f).bucket(() -> Universe3090ModItems.ACTIVE_REDSTONE_BUCKET.get()).block(() -> (LiquidBlock) Universe3090ModBlocks.ACTIVE_REDSTONE.get());

	private ActiveRedstoneFluid() {
		super(PROPERTIES);
	}

	public static class Source extends ActiveRedstoneFluid {
		public int getAmount(FluidState state) {
			return 8;
		}

		public boolean isSource(FluidState state) {
			return true;
		}
	}

	public static class Flowing extends ActiveRedstoneFluid {
		protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
			super.createFluidStateDefinition(builder);
			builder.add(LEVEL);
		}

		public int getAmount(FluidState state) {
			return state.getValue(LEVEL);
		}

		public boolean isSource(FluidState state) {
			return false;
		}
	}
}
