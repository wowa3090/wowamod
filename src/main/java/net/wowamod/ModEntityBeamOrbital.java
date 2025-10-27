package net.wowamod.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.eventbus.api.IEventBus;

public class ModEntityBeamOrbital {
  // MODID определен как "universe3090" на основе MtwmodeorbitalItemModel.java
  public static final DeferredRegister<EntityType<?>> ENTITIES =
      DeferredRegister.create(Registries.ENTITY_TYPE, "universe3090"); // ИСПРАВЛЕНО

  public static final RegistryObject<EntityType<OrbitalBeamEntity>> ORBITAL_BEAM =
      ENTITIES.register("orbital_beam",
          () -> EntityType.Builder.<OrbitalBeamEntity>of(OrbitalBeamEntity::new, MobCategory.MISC)
              .sized(0.5f, 0.5f)
              .clientTrackingRange(64) // Добавлено, чтобы сущность рендерилась на расстоянии
              .updateInterval(1)
              .noSummon()
              .fireImmune()
              .build(new ResourceLocation("universe3090", "orbital_beam").toString())); // ИСПРАВЛЕНО

  public static void register(IEventBus bus) {
    ENTITIES.register(bus);
  }
}