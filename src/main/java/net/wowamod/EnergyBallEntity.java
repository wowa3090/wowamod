package net.wowamod.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.wowamod.registration.ModRegisterEnergySp;

import java.util.List;

public class EnergyBallEntity extends ThrowableProjectile {
    private static final EntityDataAccessor<Integer> SYNCED_FLAGS = SynchedEntityData.defineId(EnergyBallEntity.class, EntityDataSerializers.INT);

    private LivingEntity target;
    private float damageMultiplier = 1.1f;

    public EnergyBallEntity(EntityType<? extends EnergyBallEntity> type, Level level) {
        super(type, level);
    }

    public EnergyBallEntity(Level level, LivingEntity shooter, LivingEntity target, int flags, float damageMultiplier) {
        super(ModRegisterEnergySp.ENERGY_BALL_TYPE, shooter, level);
        this.target = target;
        this.damageMultiplier = damageMultiplier;
        this.entityData.set(SYNCED_FLAGS, flags);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(SYNCED_FLAGS, 0);
    }

    public int getEmeraldFlags() {
        return this.entityData.get(SYNCED_FLAGS);
    }

    @Override
    public void tick() {
        super.tick();
        int flags = getEmeraldFlags();

        // АВТОНАВЕДЕНИЕ И ПРОХОЖДЕНИЕ СКВОЗЬ СТЕНЫ
        if (target != null && target.isAlive()) {
            this.noPhysics = true; // Летим сквозь стены
            
            Vec3 myPos = this.position();
            Vec3 targetPos = target.position().add(0, target.getEyeHeight() * 0.5, 0);
            Vec3 dir = targetPos.subtract(myPos).normalize();
            
            double speed = this.getDeltaMovement().length();
            // Плавный поворот к цели
            Vec3 newVel = this.getDeltaMovement().lerp(dir.scale(speed), 0.2); 
            this.setDeltaMovement(newVel);
        } else {
            this.noPhysics = false; // Обычная физика
        }
        
        // ЖЕЛТЫЙ: Освещение (частицы света)
        if ((flags & 2) != 0 && this.level().isClientSide) {
             for(int i=0; i<3; i++) {
                 this.level().addParticle(net.minecraft.core.particles.ParticleTypes.GLOW, 
                     getX() + (Math.random()-0.5), getY() + (Math.random()-0.5), getZ() + (Math.random()-0.5), 0, 0, 0);
             }
        }

        if (this.tickCount > 200) this.discard();
    }

    @Override
    protected void onHit(HitResult result) {
        if (!this.level().isClientSide) {
            // ЗЕЛЕНЫЙ: Если попали в блок - эффект костной муки
            if (result.getType() == HitResult.Type.BLOCK && (getEmeraldFlags() & 1) != 0) {
                BlockHitResult blockHit = (BlockHitResult) result;
                BoneMealItem.growCrop(new ItemStack(Items.BONE_MEAL), this.level(), blockHit.getBlockPos());
            }
            
            explode();
            this.discard();
        }
    }

    private void explode() {
        Level level = this.level();
        LivingEntity owner = (LivingEntity) this.getOwner();
        if (owner == null) return;

        int flags = getEmeraldFlags();
        double radius = 3.0; // Базовый радиус
        float baseDamagePct = 0.25f; // 20% от текущего ХП

        // КРАСНЫЙ: Радиус 165% (3.0 * 2.65 = ~8 блоков), Урон больше
        if ((flags & 64) != 0) {
            radius *= 2.65;
            baseDamagePct = 0.38f; // 35% от ХП
        }
        // ЖЕЛТЫЙ: Радиус 150%
        else if ((flags & 2) != 0) {
            radius *= 1.5;
        }

        // БЕЛЫЙ: Hyper Flash (Огромный радиус, чистый урон)
        if ((flags & 32) != 0) {
            radius = 10.0;
            // Удар молнией в центр взрыва
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
            if (bolt != null) {
                bolt.moveTo(this.position());
                bolt.setVisualOnly(true);
                level.addFreshEntity(bolt);
            }
        }

        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(radius));

        for (LivingEntity entity : entities) {
            if (entity == owner || entity.getTeam() == owner.getTeam()) continue;

            // Расчет урона: % от ТЕКУЩЕГО здоровья
            float damage = entity.getHealth() * baseDamagePct * damageMultiplier;
            if (damage < 2.0f) damage = 10.0f; // Минимум 1 сердце

            // ЗЕЛЕНЫЙ: Хилл вместо урона
            if ((flags & 1) != 0) {
                // Выращиваем животных
                if (entity instanceof AgeableMob ageable && ageable.isBaby()) {
                    ageable.ageUp(AgeableMob.getSpeedUpSecondsWhenFeeding(0), true); // Мгновенный рост
                    level.levelEvent(2005, entity.blockPosition(), 0); // Зеленые частицы
                }
                // Лечим
                entity.heal(damage * 1.5f);
                continue; // Не наносим урон
            }

            // Нанесение урона (Игнор брони)
            entity.invulnerableTime = 0; 
            entity.hurt(level.damageSources().magic(), damage);

            // ФИОЛЕТОВЫЙ: Chaos Control (Левитация + Стоп)
            if ((flags & 16) != 0) {
                entity.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 40, 2));
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 10)); // Полная остановка
            }
            
            // БЕЛЫЙ: Дополнительный чистый урон + Поджигание
            if ((flags & 32) != 0) {
                entity.hurt(level.damageSources().generic(), 15.0f); // +5 сердец чистого урона
                entity.setRemainingFireTicks(100);
            }
        }

        level.playSound(null, this.getX(), this.getY(), this.getZ(), 
                Universe3090ModSounds.ENERGY_EXPLOSION.get(), 
                net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 1.0f);
        
        // Визуальный взрыв (не ломает блоки)
        level.explode(null, this.getX(), this.getY(), this.getZ(), (float) (radius * 0.5), Level.ExplosionInteraction.NONE);
        
        // Доп. эффекты для белого (вспышка)
        if ((flags & 32) != 0 && level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.EXPLOSION_EMITTER, 
                getX(), getY(), getZ(), 5, 2, 2, 2, 0);
        }
    }
}