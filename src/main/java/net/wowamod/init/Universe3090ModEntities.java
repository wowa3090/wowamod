
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.wowamod.init;

import net.wowamod.entity.NightmareEntity;
import net.wowamod.entity.MimicEntity;
import net.wowamod.entity.DarkHeadEntity;
import net.wowamod.Universe3090Mod;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;

import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class Universe3090ModEntities {
	public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Universe3090Mod.MODID);
	public static final RegistryObject<EntityType<NightmareEntity>> NIGHTMARE = register("nightmare", EntityType.Builder.<NightmareEntity>of(NightmareEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(256)
			.setUpdateInterval(3).setCustomClientFactory(NightmareEntity::new).fireImmune().sized(0.7f, 1.9f));
	public static final RegistryObject<EntityType<MimicEntity>> MIMIC = register("mimic",
			EntityType.Builder.<MimicEntity>of(MimicEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3).setCustomClientFactory(MimicEntity::new)

					.sized(1.5f, 1.5f));
	public static final RegistryObject<EntityType<DarkHeadEntity>> DARK_HEAD = register("dark_head",
			EntityType.Builder.<DarkHeadEntity>of(DarkHeadEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3).setCustomClientFactory(DarkHeadEntity::new)

					.sized(0.6f, 1.8f));

	private static <T extends Entity> RegistryObject<EntityType<T>> register(String registryname, EntityType.Builder<T> entityTypeBuilder) {
		return REGISTRY.register(registryname, () -> (EntityType<T>) entityTypeBuilder.build(registryname));
	}

	@SubscribeEvent
	public static void init(FMLCommonSetupEvent event) {
		event.enqueueWork(() -> {
			NightmareEntity.init();
			MimicEntity.init();
			DarkHeadEntity.init();
		});
	}

	@SubscribeEvent
	public static void registerAttributes(EntityAttributeCreationEvent event) {
		event.put(NIGHTMARE.get(), NightmareEntity.createAttributes().build());
		event.put(MIMIC.get(), MimicEntity.createAttributes().build());
		event.put(DARK_HEAD.get(), DarkHeadEntity.createAttributes().build());
	}
}
