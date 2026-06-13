
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

public abstract class ExtractredsoulfluidFluid extends ForgeFlowingFluid {
	public static final ForgeFlowingFluid.Properties PROPERTIES = new ForgeFlowingFluid.Properties(() -> Universe3090ModFluidTypes.EXTRACTREDSOULFLUID_TYPE.get(), () -> Universe3090ModFluids.EXTRACTREDSOULFLUID.get(),
			() -> Universe3090ModFluids.FLOWING_EXTRACTREDSOULFLUID.get()).explosionResistance(100f).tickRate(6).bucket(() -> Universe3090ModItems.EXTRACTREDSOULFLUID_BUCKET.get())
			.block(() -> (LiquidBlock) Universe3090ModBlocks.EXTRACTREDSOULFLUID.get());

	private ExtractredsoulfluidFluid() {
		super(PROPERTIES);
	}

	public static class Source extends ExtractredsoulfluidFluid {
		public int getAmount(FluidState state) {
			return 8;
		}

		public boolean isSource(FluidState state) {
			return true;
		}
	}

	public static class Flowing extends ExtractredsoulfluidFluid {
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
