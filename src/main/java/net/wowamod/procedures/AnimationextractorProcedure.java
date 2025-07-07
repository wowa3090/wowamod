package net.wowamod.procedures;

import net.wowamod.network.Universe3090ModVariables;

import net.minecraft.world.entity.Entity;

public class AnimationextractorProcedure {
	public static String execute(Entity entity) {
		if (entity == null)
			return "";
		return "" + (entity.getCapability(Universe3090ModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new Universe3090ModVariables.PlayerVariables())).extractor;
	}
}
