
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.wowamod.init;

import net.wowamod.WowamodMod;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;

public class WowamodModSounds {
	public static final DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, WowamodMod.MODID);
	public static final RegistryObject<SoundEvent> FADINGWORLD_2FAZA = REGISTRY.register("fadingworld-2faza", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("wowamod", "fadingworld-2faza")));
	public static final RegistryObject<SoundEvent> FAZA2PEREXOD = REGISTRY.register("faza2perexod", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("wowamod", "faza2perexod")));
	public static final RegistryObject<SoundEvent> HAPPYANDSAD = REGISTRY.register("happyandsad", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("wowamod", "happyandsad")));
	public static final RegistryObject<SoundEvent> DISASSEMBLY_REQUIRED = REGISTRY.register("disassembly_required", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("wowamod", "disassembly_required")));
	public static final RegistryObject<SoundEvent> NIGHTMARE_TROJANSOUND = REGISTRY.register("nightmare.trojansound", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("wowamod", "nightmare.trojansound")));
	public static final RegistryObject<SoundEvent> AMBIENTMYPLACEZONE = REGISTRY.register("ambientmyplacezone", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("wowamod", "ambientmyplacezone")));
}
