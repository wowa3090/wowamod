
package net.wowamod.block;

import org.checkerframework.checker.units.qual.s;

import net.wowamod.init.Universe3090ModFluids;

import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.LiquidBlock;

public class ActiveRedstoneBlock extends LiquidBlock {
	public ActiveRedstoneBlock() {
		super(() -> Universe3090ModFluids.ACTIVE_REDSTONE.get(),
				BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(100f).lightLevel(s -> 3).noCollission().noLootTable().liquid().pushReaction(PushReaction.DESTROY).sound(SoundType.EMPTY).replaceable());
	}
}
