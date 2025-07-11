package net.wowamod.procedures;

import net.wowamod.network.Universe3090ModVariables;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Mth;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.CommandSource;

public class STBHswordLivingEntityIsHitWithToolProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		entity.hurt(new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("universe3090:soulsworddamage")))),
				(float) ((entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1) / 2));
		entity.hurt(new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("universe3090:soulsworddamage")))),
				(float) ((entity.getCapability(Universe3090ModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new Universe3090ModVariables.PlayerVariables())).wowaswordupgrade / 5));
		if ((entity.getCapability(Universe3090ModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new Universe3090ModVariables.PlayerVariables())).wowaswordupgrade >= 550) {
			entity.hurt(new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("universe3090:soulsworddamage")))),
					(float) Mth.nextDouble(RandomSource.create(), 33, 99));
			entity.hurt(new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("universe3090:soulsworddamage")))),
					(float) ((entity instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1) / 1.33));
			if (world instanceof Level _level && !_level.isClientSide())
				_level.explode(entity, new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("universe3090:soulsworddamage")))), null, x, y, z,
						5, true, Level.ExplosionInteraction.MOB);
		}
		if (world instanceof ServerLevel _level) {
			_level.getServer().getCommands().performPrefixedCommand(new CommandSourceStack(CommandSource.NULL, new Vec3(x, (y + 1.333), z), Vec2.ZERO, _level, 4, "", Component.literal(""), _level.getServer(), null).withSuppressedOutput(),
					"execute positioned ~ ~ ~ run particlecircle small_ritual_circle 0.1");
		}
	}
}
