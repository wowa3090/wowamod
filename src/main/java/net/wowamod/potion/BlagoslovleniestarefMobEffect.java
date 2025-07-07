
package net.wowamod.potion;

import net.wowamod.procedures.BlagoslovleniestarProcedure;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffect;

public class BlagoslovleniestarefMobEffect extends MobEffect {
	public BlagoslovleniestarefMobEffect() {
		super(MobEffectCategory.BENEFICIAL, -1);
	}

	@Override
	public String getDescriptionId() {
		return "effect.universe3090.blagoslovleniestaref";
	}

	@Override
	public void applyEffectTick(LivingEntity entity, int amplifier) {
		BlagoslovleniestarProcedure.execute(entity);
	}

	@Override
	public boolean isDurationEffectTick(int duration, int amplifier) {
		return true;
	}
}
