package net.wowamod.procedures;

import net.wowamod.init.Universe3090ModMobEffects;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

public class BlagoslovleniestarProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		if (entity instanceof LivingEntity _livEnt0 && _livEnt0.hasEffect(Universe3090ModMobEffects.BLAGOSLOVLENIESTAREF.get())) {
			if (entity instanceof Player _player) {
				_player.getAbilities().invulnerable = true;
				_player.onUpdateAbilities();
			}
			if (4 >= (entity instanceof LivingEntity _livEnt && _livEnt.hasEffect(Universe3090ModMobEffects.BLAGOSLOVLENIESTAREF.get()) ? _livEnt.getEffect(Universe3090ModMobEffects.BLAGOSLOVLENIESTAREF.get()).getDuration() : 0)) {
				if (entity instanceof Player _player) {
					_player.getAbilities().invulnerable = false;
					_player.onUpdateAbilities();
				}
			}
		}
	}
}
