package net.wowamod.procedures;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

public class YellowsoulRightclickedProcedure {

    public static void execute(Map<String, Object> dependencies) {
        if (dependencies.get("world") == null || dependencies.get("entity") == null) {
            System.err.println("Failed to execute YellowsoulRightclickedProcedure: Missing world or entity dependency.");
            return;
        }
        LevelAccessor world = (LevelAccessor) dependencies.get("world");
        Entity entity = (Entity) dependencies.get("entity");

        // --- CONFIGURABLE PARAMETERS ---
        final double laserRange = 128.0; // Range increased to 128 blocks
        final float laserDamage = 10.0f;
        final int cooldownTicks = 100; // 5 seconds
        final String cooldownTag = "yellowsoul_cooldown";

        if (!(world instanceof ServerLevel level) || !(entity instanceof Player player)) {
            return;
        }

        // --- COOLDOWN MECHANISM ---
        CompoundTag persistentData = player.getPersistentData();
        long currentTime = level.getGameTime();
        long lastUsedTime = persistentData.getLong(cooldownTag);

        if (currentTime < lastUsedTime) {
            // --- DISPLAY COOLDOWN ---
            // If the ability is on cooldown, show the remaining time in the action bar.
            long timeLeftTicks = lastUsedTime - currentTime;
            double timeLeftSeconds = timeLeftTicks / 20.0;
            // Use displayClientMessage as it's more reliable in some build environments.
            player.displayClientMessage(Component.literal(String.format("Перезарядка: %.1fс", timeLeftSeconds)), true);
            return;
        }

        // --- SOUND EFFECT ---
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GUARDIAN_ATTACK, SoundSource.PLAYERS, 1.0F, 1.0F);

        // --- RAY TRACING LOGIC ---
        Vec3 eyePosition = player.getEyePosition(1.0f);
        Vec3 lookVector = player.getViewVector(1.0f);
        Vec3 traceEnd = eyePosition.add(lookVector.scale(laserRange));
        HitResult hitResult = player.level().clip(new ClipContext(eyePosition, traceEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        Vec3 targetPosition = hitResult.getLocation();

        // --- DAMAGE APPLICATION ---
        EntityHitResult entityHitResult = getEntityHitResult(player, eyePosition, targetPosition, laserRange);
        if (entityHitResult != null && entityHitResult.getEntity() instanceof LivingEntity hitEntity) {
            if (hitEntity != player) {
                DamageSource damageSource = player.damageSources().magic();
                hitEntity.hurt(damageSource, laserDamage);
                targetPosition = entityHitResult.getLocation();
            }
        }

        // --- VISUAL EFFECT (Particles) ---
        spawnLaserParticles(level, eyePosition, targetPosition);

        // --- SET COOLDOWN ---
        persistentData.putLong(cooldownTag, currentTime + cooldownTicks);
    }

    /**
     * Spawns a line of particles to create the laser beam effect.
     * This method is much more reliable than using BlockDisplay entities.
     *
     * @param level The server level to spawn particles in.
     * @param start The starting position of the laser.
     * @param end   The ending position of the laser.
     */
    private static void spawnLaserParticles(ServerLevel level, Vec3 start, Vec3 end) {
        Vec3 difference = end.subtract(start);
        double distance = difference.length();
        Vec3 direction = difference.normalize();
        
        // Loop along the length of the beam and spawn a particle at each step.
        // A smaller step value creates a denser, more solid-looking beam.
        for (double i = 0; i < distance; i += 0.25) {
            Vec3 particlePos = start.add(direction.scale(i));
            level.sendParticles(ParticleTypes.END_ROD, particlePos.x(), particlePos.y(), particlePos.z(), 1, 0, 0, 0, 0);
        }
    }

    private static EntityHitResult getEntityHitResult(Player player, Vec3 start, Vec3 end, double range) {
        return net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(player.level(), player, start, end,
                player.getBoundingBox().expandTowards(player.getViewVector(1.0f).scale(range)).inflate(1.0D, 1.0D, 1.0D), (entity) -> !entity.isSpectator() && entity.isPickable());
    }
}
