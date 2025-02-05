package net.wowamod.procedures;

import net.wowamod.network.WowamodModVariables;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.particles.ParticleTypes;

public class WowabronyatapkiProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		if ((entity.getCapability(WowamodModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new WowamodModVariables.PlayerVariables())).wowaswordupgrade > 199) {
			world.addParticle(ParticleTypes.SOUL, x, (y - 1), z, 0, 1, 0);
		}
	}
}
