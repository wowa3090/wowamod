package net.wowamod.procedures;

import net.wowamod.network.WowamodModVariables;
import net.wowamod.init.WowamodModItems;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.damagesource.DamageSource;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class SoulsgivingProcedure {
	@SubscribeEvent
	public static void onEntityDeath(LivingDeathEvent event) {
		if (event != null && event.getEntity() != null) {
			execute(event, event.getSource(), event.getSource().getEntity());
		}
	}

	public static void execute(DamageSource damagesource, Entity sourceentity) {
		execute(null, damagesource, sourceentity);
	}

	private static void execute(@Nullable Event event, DamageSource damagesource, Entity sourceentity) {
		if (damagesource == null || sourceentity == null)
			return;
		if (damagesource.is(DamageTypes.PLAYER_ATTACK)) {
			if ((sourceentity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem() == Items.NETHERITE_SWORD
					|| (sourceentity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem() == Items.STONE_SWORD
					|| (sourceentity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem() == Items.DIAMOND_SWORD
					|| (sourceentity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem() == Items.IRON_SWORD
					|| (sourceentity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem() == WowamodModItems.NETHERSTARSWORD.get()
					|| (sourceentity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem() == WowamodModItems.WOWASWORD.get()
					|| (sourceentity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem() == WowamodModItems.SHADOWBLADE.get()) {
				{
					double _setval = (sourceentity.getCapability(WowamodModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new WowamodModVariables.PlayerVariables())).wowaswordupgrade + 1;
					sourceentity.getCapability(WowamodModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
						capability.wowaswordupgrade = _setval;
						capability.syncPlayerVariables(sourceentity);
					});
				}
			}
		}
	}
}
