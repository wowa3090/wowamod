package net.wowamod.procedures;

import net.wowamod.network.WowamodModVariables;

import net.minecraft.world.entity.Entity;

public class SoulsvivodProcedure {
	public static String execute(Entity entity) {
		if (entity == null)
			return "";
		return "Souls : " + (entity.getCapability(WowamodModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new WowamodModVariables.PlayerVariables())).wowaswordupgrade;
	}
}
