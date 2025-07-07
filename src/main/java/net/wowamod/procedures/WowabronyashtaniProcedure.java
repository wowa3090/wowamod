package net.wowamod.procedures;

import net.wowamod.network.Universe3090ModVariables;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;

public class WowabronyashtaniProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
			_entity.addEffect(new MobEffectInstance(MobEffects.JUMP, 60, 0, false, false));
		if ((entity.getCapability(Universe3090ModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new Universe3090ModVariables.PlayerVariables())).wowaswordupgrade > 49) {
			if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
				_entity.addEffect(new MobEffectInstance(MobEffects.JUMP, 60, 1, false, false));
		} else if ((entity.getCapability(Universe3090ModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new Universe3090ModVariables.PlayerVariables())).wowaswordupgrade >= 100) {
			if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
				_entity.addEffect(new MobEffectInstance(MobEffects.JUMP, 60, 2, false, false));
		} else if ((entity.getCapability(Universe3090ModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new Universe3090ModVariables.PlayerVariables())).wowaswordupgrade >= 200) {
			if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
				_entity.addEffect(new MobEffectInstance(MobEffects.JUMP, 60, 3, false, false));
		}
	}
}
