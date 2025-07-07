
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

public abstract class ExtractgolubaysoulFluid extends ForgeFlowingFluid {
	public static final ForgeFlowingFluid.Properties PROPERTIES = new ForgeFlowingFluid.Properties(() -> Universe3090ModFluidTypes.EXTRACTGOLUBAYSOUL_TYPE.get(), () -> Universe3090ModFluids.EXTRACTGOLUBAYSOUL.get(),
			() -> Universe3090ModFluids.FLOWING_EXTRACTGOLUBAYSOUL.get()).explosionResistance(100f).tickRate(6).bucket(() -> Universe3090ModItems.EXTRACTGOLUBAYSOUL_BUCKET.get())
			.block(() -> (LiquidBlock) Universe3090ModBlocks.EXTRACTGOLUBAYSOUL.get());

	private ExtractgolubaysoulFluid() {
		super(PROPERTIES);
	}

	public static class Source extends ExtractgolubaysoulFluid {
		public int getAmount(FluidState state) {
			return 8;
		}

		public boolean isSource(FluidState state) {
			return true;
		}
	}

	public static class Flowing extends ExtractgolubaysoulFluid {
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
