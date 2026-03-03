package net.wowamod.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import java.util.List;

public class EnergyBallEntity extends ThrowableProjectile {
    private LivingEntity target;
    private int emeraldFlags = 0;
    // Убрана переменная tickCount, так как она перекрывала стандартную из Entity и вызывала ошибку в рендерере

    // Исправлен дженерик конструктора для совместимости с регистрацией 1.20.1
    public EnergyBallEntity(EntityType<? extends EnergyBallEntity> type, Level level) {
        super(type, level);
    }

    public EnergyBallEntity(Level level, LivingEntity shooter, LivingEntity target, int flags) {
        super(EntityType.EGG, shooter, level); // THROWN_EGG заменен на EGG в 1.20.1
        this.target = target;
        this.emeraldFlags = flags;
        this.noPhysics = (flags & 1) != 0; // Зелёный - сквозное прохождение
    }

    // Обязательный метод для сущностей в 1.20.1
    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void tick() {
        super.tick();

        // Голубой изумруд - ускорение полёта
        float speedMultiplier = (emeraldFlags & 4) != 0 ? 2.0f : 1.0f;

        // Наведение на цель
        if (target != null && target.isAlive()) {
            Vec3 direction = target.position().add(0, target.getEyeHeight() / 2, 0)
                    .subtract(this.position()).normalize();
            this.setDeltaMovement(this.getDeltaMovement().add(direction.scale(0.08 * speedMultiplier)));
        }

        // Жёлтый изумруд - освещение
        if ((emeraldFlags & 2) != 0) {
            this.level().addParticle(net.minecraft.core.particles.ParticleTypes.END_ROD,
                    this.getX(), this.getY() + 0.5, this.getZ(), 0, 0, 0);
        }

        // Максимальное время жизни (используем встроенную this.tickCount)
        if (this.tickCount > 400) {
            this.discard();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        if (!this.level().isClientSide) {
            explode();
            this.discard();
        }
    }

    private void explode() {
        Level level = this.level();
        LivingEntity owner = (LivingEntity) this.getOwner();
        if (owner == null) return;

        double radius = 3.0;
        float damagePercent = 0.2f; // 20% от текущего HP

        // Красный изумруд - увеличенный радиус и урон
        if ((emeraldFlags & 64) != 0) {
            radius *= 2.65;
            damagePercent *= 1.5f;
        }
        // Жёлтый изумруд - радиус
        if ((emeraldFlags & 2) != 0) {
            radius *= 1.5;
        }

        List<LivingEntity> entities = level.getEntitiesOfClass(
                LivingEntity.class, 
                this.getBoundingBox().inflate(radius)
        );

        for (LivingEntity entity : entities) {
            if (entity == owner || entity.getTeam() == owner.getTeam()) continue;

            // Процентный урон от текущего здоровья
            float damage = entity.getHealth() * damagePercent;
            if (damage < 1.0f) damage = 1.0f; // Минимальный урон

            // Игнорирование брони (магический урон)
            entity.invulnerableTime = 0;
            entity.hurt(level.damageSources().magic(), damage); // Синтаксис 1.20.1

            // Зелёный изумруд - лечение союзников/животных
            if ((emeraldFlags & 1) != 0) {
                if (entity.getTeam() == owner.getTeam() || 
                    entity.getType().getCategory() == net.minecraft.world.entity.MobCategory.CREATURE) {
                    entity.heal(damage * 2);
                }
            }

            // Фиолетовый изумруд - замедление (Chaos Control)
            if ((emeraldFlags & 16) != 0) {
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 5));
                entity.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 100, 5));
            }

            // Белый изумруд - ослепление + огонь (Super Nova)
            if ((emeraldFlags & 32) != 0) {
                entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 1));
                entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 1));
                entity.setRemainingFireTicks(100);
            }
        }

        // Взрывная волна (частицы)
        level.explode(owner, this.getX(), this.getY(), this.getZ(), 
                (float) radius, Level.ExplosionInteraction.NONE);

        // Спавн частиц взрыва
        for (int i = 0; i < 50; i++) {
            level.addParticle(net.minecraft.core.particles.ParticleTypes.END_ROD,
                    this.getX(), this.getY(), this.getZ(),
                    (Math.random() - 0.5) * 2, (Math.random() - 0.5) * 2, (Math.random() - 0.5) * 2);
        }
    }

    @Override
    public boolean isPickable() {
        return false;
    }
}