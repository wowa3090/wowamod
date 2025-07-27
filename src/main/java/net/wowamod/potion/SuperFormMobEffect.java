
package net.wowamod.potion;

import net.wowamod.procedures.SuperFormOnEffectActiveTickProcedure;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffect;

public class SuperFormMobEffect extends MobEffect {
	public SuperFormMobEffect() {
		super(MobEffectCategory.NEUTRAL, -393472);
	}

	@Override
	public String getDescriptionId() {
		return "effect.universe3090.super_form";
	}

	@Override
	public boolean isInstantenous() {
		return true;
	}

	@Override
	public void applyEffectTick(LivingEntity entity, int amplifier) {
		SuperFormOnEffectActiveTickProcedure.execute(entity);
	}

	@Override
	public boolean isDurationEffectTick(int duration, int amplifier) {
		return true;
	}
}
