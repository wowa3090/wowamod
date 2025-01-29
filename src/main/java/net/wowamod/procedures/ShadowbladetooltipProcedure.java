package net.wowamod.procedures;

import net.wowamod.network.WowamodModVariables;

import net.minecraft.world.entity.Entity;

public class ShadowbladetooltipProcedure {
	public static String execute(Entity entity) {
		if (entity == null)
			return "";
		return "\u041D\u0430\u043D\u043E\u0441\u0438\u0442 "
				+ new java.text.DecimalFormat("##.##").format((entity.getCapability(WowamodModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new WowamodModVariables.PlayerVariables())).wowaswordupgrade / 15)
				+ " \u0414\u043E\u043F\u043E\u043B\u043D\u0438\u0442\u0435\u043B\u044C\u043D\u043E\u0433\u043E \u0443\u0440\u043E\u043D\u0430";
	}
}
