
/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.wowamod.init;

import net.wowamod.potion.TimerMobEffect;
import net.wowamod.potion.SuperFormMobEffect;
import net.wowamod.potion.NightmareishMobEffect;
import net.wowamod.potion.NeuyazvimostMobEffect;
import net.wowamod.potion.BlagoslovleniestarefMobEffect;
import net.wowamod.Universe3090Mod;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.world.effect.MobEffect;

public class Universe3090ModMobEffects {
	public static final DeferredRegister<MobEffect> REGISTRY = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Universe3090Mod.MODID);
	public static final RegistryObject<MobEffect> NEUYAZVIMOST = REGISTRY.register("neuyazvimost", () -> new NeuyazvimostMobEffect());
	public static final RegistryObject<MobEffect> NIGHTMAREISH = REGISTRY.register("nightmareish", () -> new NightmareishMobEffect());
	public static final RegistryObject<MobEffect> BLAGOSLOVLENIESTAREF = REGISTRY.register("blagoslovleniestaref", () -> new BlagoslovleniestarefMobEffect());
	public static final RegistryObject<MobEffect> TIMER = REGISTRY.register("timer", () -> new TimerMobEffect());
	public static final RegistryObject<MobEffect> SUPER_FORM = REGISTRY.register("super_form", () -> new SuperFormMobEffect());
}
