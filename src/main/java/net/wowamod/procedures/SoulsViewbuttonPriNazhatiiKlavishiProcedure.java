package net.wowamod.procedures;

import net.wowamod.network.WowamodModVariables;

import net.minecraft.world.entity.Entity;

public class SoulsViewbuttonPriNazhatiiKlavishiProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		if ((entity.getCapability(WowamodModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new WowamodModVariables.PlayerVariables())).soulsview == false) {
			{
				boolean _setval = true;
				entity.getCapability(WowamodModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
					capability.soulsview = _setval;
					capability.syncPlayerVariables(entity);
				});
			}
		} else {
			{
				boolean _setval = false;
				entity.getCapability(WowamodModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
					capability.soulsview = _setval;
					capability.syncPlayerVariables(entity);
				});
			}
		}
	}
}
