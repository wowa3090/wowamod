package net.wowamod.procedures;

import net.wowamod.network.WowamodModVariables;

import net.minecraft.world.entity.Entity;

public class SoulsviewProcedure {
	public static boolean execute(Entity entity) {
		if (entity == null)
			return false;
		if ((entity.getCapability(WowamodModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new WowamodModVariables.PlayerVariables())).soulsview == true) {
			return true;
		}
		return false;
	}
}
