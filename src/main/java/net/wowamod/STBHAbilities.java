package net.wowamod.procedures;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.nbt.CompoundTag;

import java.util.List;

public class STBHAbilities {

    // --- ОБЩИЕ НАСТРОЙКИ ---
    private static final double LASER_RANGE = 96.0;
    private static final float LASER_DAMAGE = 30.0f;
    private static final int LASER_COOLDOWN_TICKS = 17;
    private static final double PARTICLE_SPACING = 0.3;

    private static final float AOE_DAMAGE = 17.5f;
    private static final double SHOCKWAVE_RADIUS = 6.5;
    private static final int SHOCKWAVE_PARTICLE_DENSITY = 45;
    
    // --- НАСТРОЙКИ УСИЛЕНИЯ ---
    private static final int EMPOWER_DURATION_TICKS = 300;
    private static final int EMPOWER_COOLDOWN_TICKS = 600;
    private static final int SPEED_AMPLIFIER = 1;
    private static final int STRENGTH_AMPLIFIER = 1;
    private static final int RESISTANCE_AMPLIFIER = 0;

    /**
     * Способность 1: Выпускает лазерный луч (ЛКМ в воздухе).
     */
    public static void fireLaserBeam(LevelAccessor world, Entity entity, ItemStack itemstack) {
        if (!(entity instanceof Player player) || !(world instanceof Level level)) return;
        
        long currentTime = level.getGameTime();
        CompoundTag tag = itemstack.getOrCreateTag();
        if (currentTime < tag.getLong("laser_cooldown")) {
            return;
        }

        if (!level.isClientSide()) {
            ServerLevel serverLevel = (ServerLevel) level;
            Vec3 eyePosition = player.getEyePosition(1.0F);
            Vec3 lookVector = player.getLookAngle();
            Vec3 endPosition = eyePosition.add(lookVector.scale(LASER_RANGE));
            BlockHitResult blockHitResult = level.clip(new ClipContext(eyePosition, endPosition, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
            Vec3 finalHitPosition = blockHitResult.getLocation();
            EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(level, player, eyePosition, endPosition, new AABB(eyePosition, endPosition).inflate(1.0D), (e) -> !e.isSpectator() && e.isPickable());
            if (entityHitResult != null) {
                Vec3 entityHitLocation = entityHitResult.getLocation();
                if (eyePosition.distanceToSqr(entityHitLocation) < eyePosition.distanceToSqr(finalHitPosition)) {
                    finalHitPosition = entityHitLocation;
                    Entity targetEntity = entityHitResult.getEntity();
                    targetEntity.hurt(level.damageSources().indirectMagic(player, player), LASER_DAMAGE);
                }
            }
            double distance = eyePosition.distanceTo(finalHitPosition);
            for (double d = 0; d < distance; d += PARTICLE_SPACING) {
                Vec3 particlePos = eyePosition.add(lookVector.scale(d));
                serverLevel.sendParticles(ParticleTypes.END_ROD, particlePos.x, particlePos.y, particlePos.z, 1, 0, 0, 0, 0);
            }
            tag.putLong("laser_cooldown", currentTime + LASER_COOLDOWN_TICKS);
        }
    }

    /**
     * Способность 2: Призывает небесный удар по цели (ЛКМ по врагу).
     */
    public static void executeHeavenlyStrike(LevelAccessor world, Entity targetEntity, Entity sourceEntity) {
        if (!(sourceEntity instanceof Player player) || !(targetEntity instanceof LivingEntity)) return;
        if (world instanceof ServerLevel serverLevel) {
            Vec3 targetPos = targetEntity.position();
            // ИСПРАВЛЕНО: Используем level() цели для получения damageSources
            targetEntity.hurt(targetEntity.level().damageSources().indirectMagic(player, player), AOE_DAMAGE);
            for (double y = targetPos.y + 10; y >= targetPos.y; y -= 0.5) {
                serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, targetPos.x, y, targetPos.z, 5, 0.1, 0.5, 0.1, 0.01);
            }
            executeShockwave(serverLevel, targetPos, player);
        }
    }

    /**
     * Способность 3: Усиление игрока (ПКМ).
     */
    public static void executeEmpowerment(LevelAccessor world, Entity entity, ItemStack itemstack) {
        if (!(entity instanceof Player player) || !(world instanceof Level level)) return;

        long currentTime = level.getGameTime();
        CompoundTag tag = itemstack.getOrCreateTag();
        if (currentTime < tag.getLong("empower_cooldown")) {
            return;
        }
        
        if (world instanceof ServerLevel serverLevel) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, EMPOWER_DURATION_TICKS, SPEED_AMPLIFIER));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, EMPOWER_DURATION_TICKS, STRENGTH_AMPLIFIER));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, EMPOWER_DURATION_TICKS, RESISTANCE_AMPLIFIER));
            serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY() + 1, player.getZ(), 30, 0.5, 0.5, 0.5, 0.1);
            tag.putLong("empower_cooldown", currentTime + EMPOWER_COOLDOWN_TICKS);
        }
    }

    private static void executeShockwave(ServerLevel world, Vec3 center, Player sourcePlayer) {
        for (int i = 0; i < SHOCKWAVE_PARTICLE_DENSITY; i++) {
            float angle = (i / (float) SHOCKWAVE_PARTICLE_DENSITY) * 360f;
            for (double r = 1.0; r <= SHOCKWAVE_RADIUS; r += 0.5) {
                float xOffset = Mth.cos(angle) * (float) r;
                float zOffset = Mth.sin(angle) * (float) r;
                Vec3 particlePos = center.add(xOffset, 0.5, zOffset);
                world.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, particlePos.x, particlePos.y, particlePos.z, 1, 0, 0, 0, 0);
            }
        }
        AABB area = new AABB(center.subtract(SHOCKWAVE_RADIUS, 2, SHOCKWAVE_RADIUS), center.add(SHOCKWAVE_RADIUS, 2, SHOCKWAVE_RADIUS));
        List<LivingEntity> entities = world.getEntitiesOfClass(LivingEntity.class, area, (e) -> e != sourcePlayer && e.isAlive());
        for (LivingEntity entity : entities) {
            entity.hurt(world.damageSources().indirectMagic(sourcePlayer, sourcePlayer), AOE_DAMAGE);
        }
    }
}
