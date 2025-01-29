package net.wowamod.procedures;

import net.wowamod.network.WowamodModVariables;

import net.minecraft.world.entity.Entity;

public class SoulsremoveallProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		{
			double _setval = 0;
			entity.getCapability(WowamodModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
				capability.wowaswordupgrade = _setval;
				capability.syncPlayerVariables(entity);
			});
		}
	}
}
