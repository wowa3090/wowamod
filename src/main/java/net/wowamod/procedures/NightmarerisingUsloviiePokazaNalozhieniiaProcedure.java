package net.wowamod.procedures;

import net.wowamod.init.WowamodModMobEffects;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

public class NightmarerisingUsloviiePokazaNalozhieniiaProcedure {
	public static boolean execute(Entity entity) {
		if (entity == null)
			return false;
		if (entity instanceof LivingEntity _livEnt0 && _livEnt0.hasEffect(WowamodModMobEffects.NIGHTMAREISH.get())) {
			return true;
		}
		return false;
	}
}
