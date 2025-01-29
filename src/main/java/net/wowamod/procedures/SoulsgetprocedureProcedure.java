package net.wowamod.procedures;

import net.wowamod.network.WowamodModVariables;

import net.minecraft.world.entity.Entity;
import net.minecraft.commands.CommandSourceStack;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.arguments.DoubleArgumentType;

public class SoulsgetprocedureProcedure {
	public static void execute(CommandContext<CommandSourceStack> arguments, Entity entity) {
		if (entity == null)
			return;
		{
			double _setval = (entity.getCapability(WowamodModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new WowamodModVariables.PlayerVariables())).wowaswordupgrade + DoubleArgumentType.getDouble(arguments, "souls");
			entity.getCapability(WowamodModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
				capability.wowaswordupgrade = _setval;
				capability.syncPlayerVariables(entity);
			});
		}
	}
}
