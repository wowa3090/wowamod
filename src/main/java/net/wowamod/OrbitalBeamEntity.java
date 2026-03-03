package net.wowamod;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.wowamod.ModEntityBeamOrbital;
import net.wowamod.OrbitalBeamDamageController; // наш контроллер урона
import net.wowamod.OrbitalBeamDamageController.DamageProfile;
import net.wowamod.OrbitalBeamDamageController.FalloffType;

public class OrbitalBeamEntity extends Entity {
    private int life;
    private float radius;

    public static final int WARMUP = 20;
    public static final int PEAK = 5;
    public static final int FADE = 15;
    public static final int MAX_LIFE = WARMUP + PEAK + FADE;
    private static final float DAMAGE = 60.0f;

    public OrbitalBeamEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.radius = 6.0f;
    }

    public OrbitalBeamEntity(EntityType<?> type, Level level, double x, double y, double z, float radius) {
        this(type, level);
        this.setPos(x, y, z);
        this.radius = radius;
        this.life = 0;
    }

    public OrbitalBeamEntity(Level level, double x, double y, double z, float radius) {
        this(ModEntityBeamOrbital.ORBITAL_BEAM.get(), level);
        this.setPos(x, y, z);
        this.radius = radius;
        this.life = 0;
    }

    public int getLife() {
        return this.life;
    }

    public float getBeamRadius() {
        return this.radius;
    }

    @Override
    public void tick() {
        super.tick();
        life++;

        if (life == WARMUP && !level().isClientSide) {
            // Эффект удара молнии
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level());
            if (bolt != null) {
                bolt.moveTo(getX(), getY() - 1.0, getZ());
                level().addFreshEntity(bolt);
            }

            // Вызов урона через контроллер
            DamageProfile profile = DamageProfile.builder()
                    .baseDamage(DAMAGE)
                    .minDamageFactor(0.3f)
                    .distanceFalloff(FalloffType.QUADRATIC)
                    .knockback(true)
                    .knockbackStrength(0.6f)
                    .iframesTicks(10)
                    .allowFriendlyFire(false)
                    .requireLineOfSight(true)
                    .build();

            OrbitalBeamDamageController.lightningStrike(level(), this, new Vec3(getX(), getY(), getZ()), radius, profile);

            // Звуки
            level().playSound(null, blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 2.0f, 1.0f);
            level().playSound(null, blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 1.5f, 0.9f);
        }

        if (life > MAX_LIFE) {
            discard();
        }
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        life = tag.getInt("Life");
        radius = tag.getFloat("Radius");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Life", life);
        tag.putFloat("Radius", radius);
    }
}