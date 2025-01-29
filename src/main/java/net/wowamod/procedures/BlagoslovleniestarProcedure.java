package net.wowamod.procedures;

import net.wowamod.init.WowamodModMobEffects;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

public class BlagoslovleniestarProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		if (entity instanceof LivingEntity _livEnt0 && _livEnt0.hasEffect(WowamodModMobEffects.BLAGOSLOVLENIESTAREF.get())) {
			if (entity instanceof Player _player) {
				_player.getAbilities().invulnerable = true;
				_player.onUpdateAbilities();
			}
			if (4 >= (entity instanceof LivingEntity _livEnt && _livEnt.hasEffect(WowamodModMobEffects.BLAGOSLOVLENIESTAREF.get()) ? _livEnt.getEffect(WowamodModMobEffects.BLAGOSLOVLENIESTAREF.get()).getDuration() : 0)) {
				if (entity instanceof Player _player) {
					_player.getAbilities().invulnerable = false;
					_player.onUpdateAbilities();
				}
			}
		}
	}
}
