package net.wowamod.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;

// Импорт больше не нужен, так как мы передаём EntityType напрямую
// import net.wowamod.entity.ModEntityBeamOrbital;

public class OrbitalBeamEntity extends Entity {
  private int life;
  private float radius; // Убрано 'final'

  // Тайминги для анимации и логики
  public static final int WARMUP = 20; // 1 сек
  public static final int PEAK = 5;    // 0.25 сек (пик урона)
  public static final int FADE = 15;   // 0.75 сек (исчезание)
  private static final float DAMAGE = 14.0f;

  // Основной конструктор, принимающий EntityType
  public OrbitalBeamEntity(EntityType<?> type, Level level) {
    super(type, level);
    this.radius = 6.0f; // Значение по умолчанию
    this.life = 0;
  }

  // Конструктор, который вызывается из MtwmodeorbitalItem, принимающий конкретный EntityType
  public OrbitalBeamEntity(EntityType<OrbitalBeamEntity> type, Level level, double x, double y, double z, float radius) {
    // Вызываем основной конструктор, передавая полученный EntityType
    this(type, level);
    this.setPos(x, y, z);
    this.radius = radius; // Корректное присвоение радиуса
  }

  @Override
  public void tick() {
    super.tick();
    life++;

    // Логика удара (происходит 1 раз)
    if (life == WARMUP) {
      // Эффект удара (молния)
      LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level());
      if (bolt != null) {
        // Молния бьет в землю (y - 1)
        bolt.moveTo(getX(), getY() - 1.0, getZ());
        level().addFreshEntity(bolt);
      }

      // Логика урона
      AABB box = new AABB(getX() - radius, getY() - 2, getZ() - radius,
                          getX() + radius, getY() + 3, getZ() + radius);
      for (LivingEntity e : level().getEntitiesOfClass(LivingEntity.class, box)) {
        if (e != null && e.isAlive()) {
          // 2D-дистанция (по XZ)
          double dist = Math.sqrt(e.distanceToSqr(getX(), e.getY(), getZ()));
          double factor = Mth.clamp(1.0 - (dist / radius), 0.0, 1.0);
          float dmg = (float)(DAMAGE * (0.6 + 0.4 * factor));
          e.hurt(level().damageSources().magic(), dmg);
        }
      }

      // Звуки удара (перенесены из предмета)
      level().playSound(null, blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 2.0f, 1.0f);
      level().playSound(null, blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 1.5f, 0.9f);
    }

    // Удаление сущности
    if (life > WARMUP + PEAK + FADE) {
      discard();
    }
  }

  // Метод для рендерера, чтобы знать текущую жизнь
  public int getLife() {
    return this.life;
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