
package net.wowamod.potion;

import net.wowamod.procedures.NeuyazvimostPASSIVEffiektProcedure;

import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffect;

public class NeuyazvimostMobEffect extends MobEffect {
	public NeuyazvimostMobEffect() {
		super(MobEffectCategory.BENEFICIAL, -720896);
	}

	@Override
	public String getDescriptionId() {
		return "effect.wowamod.neuyazvimost";
	}

	@Override
	public void addAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
		NeuyazvimostPASSIVEffiektProcedure.execute(entity.level(), entity);
	}

	@Override
	public void applyEffectTick(LivingEntity entity, int amplifier) {
		NeuyazvimostPASSIVEffiektProcedure.execute(entity.level(), entity);
	}

	@Override
	public boolean isDurationEffectTick(int duration, int amplifier) {
		return true;
	}
}
