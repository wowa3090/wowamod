package net.wowamod; // ИСПРАВЛЕНО: Пакет изменен, чтобы соответствовать пути из лога ошибки

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap; // ИСПРАВЛЕНО: Импорт изменен на Int2IntOpenHashMap
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource; // ИСПРАВЛЕНО: Пакет DamageSource изменен для 1.20.1
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.ClipContext;

import java.util.function.Predicate;

public final class OrbitalBeamDamageController {

    public enum FalloffType { NONE, LINEAR, QUADRATIC }

    public static final class DamageProfile {
        public final float baseDamage;
        public final float minDamageFactor;
        public final FalloffType distanceFalloff;
        public final boolean bypassArmor;
        public final boolean respectShields;
        public final boolean knockback;
        public final float knockbackStrength;
        public final int iframesTicks;
        public final boolean allowFriendlyFire;
        public final boolean requireLineOfSight;
        public final Predicate<Entity> targetFilter;
        public final float verticalBias;
        public final float radiusScale;

        private DamageProfile(Builder b) {
            this.baseDamage = b.baseDamage;
            this.minDamageFactor = b.minDamageFactor;
            this.distanceFalloff = b.distanceFalloff;
            this.bypassArmor = b.bypassArmor;
            this.respectShields = b.respectShields;
            this.knockback = b.knockback;
            this.knockbackStrength = b.knockbackStrength;
            this.iframesTicks = b.iframesTicks;
            this.allowFriendlyFire = b.allowFriendlyFire;
            this.requireLineOfSight = b.requireLineOfSight;
            this.targetFilter = b.targetFilter;
            this.verticalBias = b.verticalBias;
            this.radiusScale = b.radiusScale;
        }

        public static Builder builder() { return new Builder(); }

        public static final class Builder {
            private float baseDamage = 10f;
            private float minDamageFactor = 0.25f;
            private FalloffType distanceFalloff = FalloffType.LINEAR;
            private boolean bypassArmor = false;
            private boolean respectShields = true;
            private boolean knockback = true;
            private float knockbackStrength = 0.5f;
            private int iframesTicks = 8;
            private boolean allowFriendlyFire = true;
            private boolean requireLineOfSight = false;
            private Predicate<Entity> targetFilter = null;
            private float verticalBias = 0.0f;
            private float radiusScale = 1.0f;

            public Builder baseDamage(float v) { this.baseDamage = v; return this; }
            public Builder minDamageFactor(float v) { this.minDamageFactor = Mth.clamp(v, 0f, 1f); return this; }
            public Builder distanceFalloff(FalloffType t) { this.distanceFalloff = t; return this; }
            public Builder bypassArmor(boolean v) { this.bypassArmor = v; return this; }
            public Builder respectShields(boolean v) { this.respectShields = v; return this; }
            public Builder knockback(boolean v) { this.knockback = v; return this; }
            public Builder knockbackStrength(float v) { this.knockbackStrength = Math.max(0f, v); return this; }
            public Builder iframesTicks(int ticks) { this.iframesTicks = Math.max(0, ticks); return this; }
            public Builder allowFriendlyFire(boolean v) { this.allowFriendlyFire = v; return this; }
            public Builder requireLineOfSight(boolean v) { this.requireLineOfSight = v; return this; }
            public Builder targetFilter(Predicate<Entity> p) { this.targetFilter = p; return this; }
            public Builder verticalBias(float v) { this.verticalBias = v; return this; }
            public Builder radiusScale(float v) { this.radiusScale = Math.max(0.1f, v); return this; }
            public DamageProfile build() { return new DamageProfile(this); }
        }
    }

    // ИСПРАВЛЕНО: Тип карты изменен на Int2IntOpenHashMap для пар (int -> int)
    private static final Int2IntOpenHashMap lastHitTickByEntityId = new Int2IntOpenHashMap();

    private OrbitalBeamDamageController() {}

    public static int applyBeamStrike(Level level, Entity source, Vec3 center, float radius,
                                      DamageProfile profile, DamageSource damageSource) {
        if (!(level instanceof ServerLevel server)) return 0;

        float effectiveRadius = radius * profile.radiusScale;
        AABB box = new AABB(center.x - effectiveRadius, center.y - effectiveRadius, center.z - effectiveRadius,
                            center.x + effectiveRadius, center.y + effectiveRadius, center.z + effectiveRadius);

        int hitCount = 0;
        long tick = server.getGameTime();

        for (Entity e : server.getEntities((Entity) null, box, ent -> ent instanceof LivingEntity)) {
            LivingEntity target = (LivingEntity) e;

            if (profile.targetFilter != null && !profile.targetFilter.test(target)) continue;
            if (!profile.allowFriendlyFire && isFriendly(source, target)) continue;
            if (!canHitNow(target, tick, profile.iframesTicks)) continue;

            Vec3 targetPos = target.position().add(0, profile.verticalBias, 0);
            double dist = targetPos.distanceTo(center);
            if (dist > effectiveRadius) continue;

            if (profile.requireLineOfSight && !hasLineOfSight(level, target, center)) continue;

            float factor = damageFalloffFactor((float) dist, effectiveRadius, profile);
            float damage = Math.max(0f, profile.baseDamage * factor);

            boolean damaged = target.hurt(damageSource, damage);
            if (damaged) {
                if (profile.knockback) applyKnockback(target, center, profile.knockbackStrength);
                int currentTickAsInt = (int) tick;
                // ИСПРАВЛЕНО: Этот вызов .put() теперь однозначно соответствует put(int, int)
                lastHitTickByEntityId.put(target.getId(), currentTickAsInt);
                hitCount++;
            }
        }
        return hitCount;
    }

    private static boolean canHitNow(LivingEntity target, long tick, int iframesTicks) {
        if (iframesTicks <= 0) return true;
        
        int defaultIntVal = Integer.MIN_VALUE; 
        
        // ИСПРАВЛЕНО: Этот вызов .getOrDefault() теперь однозначно соответствует getOrDefault(int, int)
        int lastStoredTick = lastHitTickByEntityId.getOrDefault(target.getId(), defaultIntVal);

        // Если lastStoredTick остался MIN_VALUE, значит, сущность еще не была ударена
        if (lastStoredTick == Integer.MIN_VALUE) {
            return true;
        }

        int currentTickAsInt = (int) tick;
        int tickDiff = currentTickAsInt - lastStoredTick;
        
        return tickDiff >= iframesTicks;
    }

    private static boolean isFriendly(Entity source, LivingEntity target) {
        if (source == null) return false;
        if (source == target) return true;
        if (source instanceof Player sp && target instanceof Player tp) {
            var sTeam = sp.getTeam();
            var tTeam = tp.getTeam();
            if (sTeam != null && tTeam != null && sTeam.equals(tTeam)) return true;
        }
        return false;
    }

    private static float damageFalloffFactor(float dist, float radius, DamageProfile p) {
        float t = Mth.clamp(dist / radius, 0f, 1f);
        float f;
        switch (p.distanceFalloff) {
            case NONE -> f = 1f;
            case LINEAR -> f = 1f - t;
            case QUADRATIC -> { float lin = 1f - t; f = lin * lin; }
            default -> f = 1f;
        }
        return Mth.clamp(f, p.minDamageFactor, 1f);
    }

    private static void applyKnockback(LivingEntity target, Vec3 center, float strength) {
        Vec3 dir = target.position().subtract(center);
        if (dir.lengthSqr() < 1e-6) return;
        dir = dir.normalize();
        target.push(dir.x * strength, 0.15f * strength, dir.z * strength);
        target.hasImpulse = true;
    }

    private static boolean hasLineOfSight(Level level, LivingEntity target, Vec3 point) {
        Vec3 eye = target.getEyePosition();
        ClipContext ctx = new ClipContext(eye, point, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, target);
        BlockHitResult hit = level.clip(ctx);
        if (hit.getType() == HitResult.Type.MISS) return true;
        return hit.getLocation().distanceToSqr(point) < 0.01;
    }

    public static int lightningStrike(Level level, Entity source, Vec3 center, float radius, DamageProfile profile) {
        return applyBeamStrike(level, source, center, radius, profile, level.damageSources().lightningBolt());
    }
}