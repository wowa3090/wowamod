package net.wowamod.procedures;

import net.wowamod.init.Universe3090ModMobEffects;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

public class NightmarerisingUsloviiePokazaNalozhieniiaProcedure {
	public static boolean execute(Entity entity) {
		if (entity == null)
			return false;
		if (entity instanceof LivingEntity _livEnt0 && _livEnt0.hasEffect(Universe3090ModMobEffects.NIGHTMAREISH.get())) {
			return true;
		}
		return false;
	}
}
