package net.wowamod.procedures;

import net.wowamod.network.Universe3090ModVariables;
import net.wowamod.init.Universe3090ModMobEffects;
import net.wowamod.init.Universe3090ModItems;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;

public class WowaswordPKMProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		if (entity instanceof Player _playerHasItem ? _playerHasItem.getInventory().contains(new ItemStack(Universe3090ModItems.RAINBOWEMERALD.get())) : false) {
			if (world instanceof ServerLevel _level) {
				LightningBolt entityToSpawn = EntityType.LIGHTNING_BOLT.create(_level);
				entityToSpawn.moveTo(Vec3.atBottomCenterOf(BlockPos.containing(x, y, z)));;
				_level.addFreshEntity(entityToSpawn);
			}
			if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
				_entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 5, false, false));
			if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
				_entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 5, false, false));
			if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
				_entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 160, 1, false, false));
			if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
				_entity.addEffect(new MobEffectInstance(Universe3090ModMobEffects.NEUYAZVIMOST.get(), 100, 0, false, false));
			if ((entity.getCapability(Universe3090ModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new Universe3090ModVariables.PlayerVariables())).wowaswordupgrade > 99) {
				if (entity instanceof Player _player)
					_player.getCooldowns().addCooldown(Universe3090ModItems.WOWASWORD.get(), 100);
			} else {
				if (entity instanceof Player _player)
					_player.getCooldowns().addCooldown(Universe3090ModItems.WOWASWORD.get(), 200);
			}
		} else {
			if (world instanceof ServerLevel _level) {
				LightningBolt entityToSpawn = EntityType.LIGHTNING_BOLT.create(_level);
				entityToSpawn.moveTo(Vec3.atBottomCenterOf(BlockPos.containing(x, y, z)));
				entityToSpawn.setVisualOnly(true);
				_level.addFreshEntity(entityToSpawn);
			}
			if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
				_entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 5, false, false));
			if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
				_entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 80, 4, false, false));
			if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
				_entity.addEffect(new MobEffectInstance(Universe3090ModMobEffects.NEUYAZVIMOST.get(), 20, 0, false, false));
			if ((entity.getCapability(Universe3090ModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new Universe3090ModVariables.PlayerVariables())).wowaswordupgrade >= 100) {
				if (entity instanceof Player _player)
					_player.getCooldowns().addCooldown(Universe3090ModItems.WOWASWORD.get(), 200);
			} else {
				if (entity instanceof Player _player)
					_player.getCooldowns().addCooldown(Universe3090ModItems.WOWASWORD.get(), 400);
			}
		}
	}
}
