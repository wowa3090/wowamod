
package net.wowamod.fluid;

import net.wowamod.init.WowamodModItems;
import net.wowamod.init.WowamodModFluids;
import net.wowamod.init.WowamodModFluidTypes;
import net.wowamod.init.WowamodModBlocks;

import net.minecraftforge.fluids.ForgeFlowingFluid;

import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.LiquidBlock;

public abstract class ExtractgolubaysoulFluid extends ForgeFlowingFluid {
	public static final ForgeFlowingFluid.Properties PROPERTIES = new ForgeFlowingFluid.Properties(() -> WowamodModFluidTypes.EXTRACTGOLUBAYSOUL_TYPE.get(), () -> WowamodModFluids.EXTRACTGOLUBAYSOUL.get(),
			() -> WowamodModFluids.FLOWING_EXTRACTGOLUBAYSOUL.get()).explosionResistance(100f).tickRate(6).bucket(() -> WowamodModItems.EXTRACTGOLUBAYSOUL_BUCKET.get()).block(() -> (LiquidBlock) WowamodModBlocks.EXTRACTGOLUBAYSOUL.get());

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
