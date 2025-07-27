package net.wowamod.procedures;

import net.wowamod.network.Universe3090ModVariables;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.event.TickEvent;

import net.minecraft.world.level.LevelAccessor;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class ForgeFlowTimerProcedure {
	@SubscribeEvent
	public static void onWorldTick(TickEvent.LevelTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			execute(event, event.level);
		}
	}

	public static void execute(LevelAccessor world) {
		execute(null, world);
	}

	private static void execute(@Nullable Event event, LevelAccessor world) {
		if (!world.isClientSide()) {
			if (Universe3090ModVariables.MapVariables.get(world).forgeFlow == 0) {
				Universe3090ModVariables.MapVariables.get(world).forgeFlow = 300;
				Universe3090ModVariables.MapVariables.get(world).syncData(world);
			} else {
				Universe3090ModVariables.MapVariables.get(world).forgeFlow = Universe3090ModVariables.MapVariables.get(world).forgeFlow - 1;
				Universe3090ModVariables.MapVariables.get(world).syncData(world);
			}
		}
	}
}
