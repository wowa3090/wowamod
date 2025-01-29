package net.wowamod.procedures;

import net.wowamod.network.WowamodModVariables;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.CommandSource;

public class ShadowbladePriUdarieProcedure {
	public static void execute(LevelAccessor world, Entity entity, Entity sourceentity) {
		if (entity == null || sourceentity == null)
			return;
		entity.hurt(new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("wowamod:soulsworddamage")))),
				(float) ((sourceentity.getCapability(WowamodModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new WowamodModVariables.PlayerVariables())).wowaswordupgrade / 12));
		if (world instanceof ServerLevel _level) {
			_level.getServer().getCommands().performPrefixedCommand(new CommandSourceStack(CommandSource.NULL, new Vec3(0, 0, 0), Vec2.ZERO, _level, 4, "", Component.literal(""), _level.getServer(), null).withSuppressedOutput(),
					"execute as " + entity.getStringUUID() + " at " + entity.getStringUUID() + " anchored feet run particle " + "SOUL".replaceAll("(?i)CUSTOM:", "wowamod:").toLowerCase() + " ~ ~3 ~ 0 0 0 0.00001 10");
		}
	}
}
