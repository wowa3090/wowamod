package net.wowamod.entity;

import net.wowamod.init.Universe3090ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity; // <--- ДОБАВЛЕН ВАЖНЫЙ ИМПОРТ
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
    // Множитель по умолчанию > 0, чтобы урон точно проходил
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
        
        // Если владелец пропал, владельцем считаем сам шар, чтобы урон наносился в любом случае
        // Теперь это работает, т.к. класс Entity импортирован
        Entity actualOwner = owner != null ? owner : this;

        int flags = getEmeraldFlags();
        double radius = 3.0; 
        float baseDamagePct = 0.25f; 

        if ((flags & 64) != 0) { // КРАСНЫЙ
            radius *= 2.65;
            baseDamagePct = 0.38f; 
        } else if ((flags & 2) != 0) { // ЖЕЛТЫЙ
            radius *= 1.5;
        }

        if ((flags & 32) != 0) { // БЕЛЫЙ
            radius = 10.0;
            if (!level.isClientSide) {
                LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
                if (bolt != null) {
                    bolt.moveTo(this.position());
                    bolt.setVisualOnly(true);
                    level.addFreshEntity(bolt);
                }
            }
        }

        // 1. Звук (Кастомный)
        level.playSound(null, this.getX(), this.getY(), this.getZ(), 
                Universe3090ModSounds.ENERGY_EXPLOSION.get(), 
                net.minecraft.sounds.SoundSource.PLAYERS, 3.0f, 0.9F + level.random.nextFloat() * 0.2F);

        if (level instanceof ServerLevel serverLevel) {
            // 2. Визуализация взрыва (без звука)
            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.EXPLOSION, 
                getX(), getY(), getZ(), (int)(radius * 3), radius/4, radius/4, radius/4, 0.05);
            
            if ((flags & 32) != 0) {
                serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.EXPLOSION_EMITTER, 
                    getX(), getY(), getZ(), 5, 1, 1, 1, 0);
            }

            // 3. Поиск сущностей через создание новой коробки (более надежно)
            net.minecraft.world.phys.AABB area = new net.minecraft.world.phys.AABB(
                this.getX() - radius, this.getY() - radius, this.getZ() - radius,
                this.getX() + radius, this.getY() + radius, this.getZ() + radius
            );
            
            List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area);
            
            // Отладочное сообщение в консоль (потом можно удалить)
            // System.out.println("EnergyBall: Взрыв задел " + entities.size() + " сущностей в радиусе " + radius);

            for (LivingEntity entity : entities) {
                if (entity == owner) continue; // Игрок не бьет сам себя

                // Расчет отталкивания
                Vec3 vec = entity.position().subtract(this.position());
                double dist = vec.length();
                if (dist < radius) {
                    double force = (1.0 - (dist / radius)) * 1.2;
                    entity.setDeltaMovement(entity.getDeltaMovement().add(vec.normalize().scale(force)));
                    entity.hurtMarked = true; // Важно для обновления позиции у клиента
                }

                // Расчет урона
                float damage = entity.getHealth() * baseDamagePct * damageMultiplier;
                if (damage < 2.0f) damage = 10.0f; // Твой минимальный урон (5 сердец)

                // Эффекты изумрудов
                if ((flags & 1) != 0) { // ЗЕЛЕНЫЙ
                    if (entity instanceof AgeableMob ageable && ageable.isBaby()) {
                        ageable.setAge(0); 
                        serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER, 
                            entity.getX(), entity.getY() + 1, entity.getZ(), 10, 0.3, 0.3, 0.3, 0.1);
                    }
                    entity.heal(damage * 1.5f);
                } else {
                    // ГАРАНТИРОВАННОЕ НАНЕСЕНИЕ УРОНА
                    entity.invulnerableTime = 0; 
                    // Используем indirectMagic, чтобы засчитать убийство игроку
                    entity.hurt(level.damageSources().indirectMagic(this, actualOwner), damage);

                    if ((flags & 16) != 0) { // ФИОЛЕТОВЫЙ
                        entity.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 40, 2));
                        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 10));
                    }
                    
                    if ((flags & 32) != 0) { // БЕЛЫЙ
                        entity.hurt(level.damageSources().generic(), 15.0f);
                        entity.setRemainingFireTicks(100);
                    }
                }
            }
        }
    }
}