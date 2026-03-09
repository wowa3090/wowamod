package net.wowamod.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;

public class GreensoulRightclickedProcedure {
    // Убрали X, Y, Z из аргументов, так как мы берем координаты прямо из сущностей
    public static void execute(LevelAccessor world, Entity entity, ItemStack itemstack) {
        if (entity == null || !(world instanceof ServerLevel serverLevel)) return;

        // Параметры системы
        final float HEAL_AMOUNT = 12.0F;
        final double RADIUS = 16.0; // Радиус поиска цели (16 блоков)

        LivingEntity target = null;
        boolean healed = false;

        if (entity instanceof Player player) {
            Vec3 startPos = player.getEyePosition(1.0F);
            Vec3 lookVec = player.getViewVector(1.0F);
            Vec3 endPos = startPos.add(lookVec.x * RADIUS, lookVec.y * RADIUS, lookVec.z * RADIUS);

            // Создаем коробку поиска вокруг луча взгляда
            AABB searchArea = player.getBoundingBox().expandTowards(lookVec.scale(RADIUS)).inflate(1.0D);

            // Ванильный и самый оптимизированный способ найти сущность на линии взгляда
            EntityHitResult hitResult = ProjectileUtil.getEntityHitResult(
                player, startPos, endPos, searchArea,
                e -> e instanceof LivingEntity && e != player, RADIUS * RADIUS
            );

            if (hitResult != null && hitResult.getEntity() instanceof LivingEntity foundEntity) {
                target = foundEntity;
            }
        }

        // Если по направлению взгляда цель не найдена, лечим самого игрока
        if (target == null && entity instanceof LivingEntity livingEntity) {
            target = livingEntity;
        }

        // Лечение цели
        if (target != null && target.getHealth() < target.getMaxHealth()) {
            float newHealth = Math.min(target.getHealth() + HEAL_AMOUNT, target.getMaxHealth());
            target.setHealth(newHealth);
            healed = true;

            // Звук (играется от позиции цели)
            world.playSound(null, BlockPos.containing(target.position()),
                SoundEvents.EXPERIENCE_ORB_PICKUP,
                SoundSource.PLAYERS,
                1.0F, 1.2F
            );

            // ==========================================
            // ВИЗУАЛЬНЫЕ ЭФФЕКТЫ (ОПТИМИЗИРОВАННЫЕ)
            // ==========================================
            
            // 1. Сердечки над головой (один пакет вместо цикла)
            serverLevel.sendParticles(ParticleTypes.HEART,
                target.getX(), target.getY() + target.getBbHeight() + 0.3, target.getZ(),
                7, // Количество частиц
                0.3, 0.2, 0.3, // Разброс X, Y, Z
                0.05 // Скорость
            );

            // 2. Вспышка искр вокруг цели (один пакет вместо цикла на 50 итераций)
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                target.getX(), target.getY() + (target.getBbHeight() / 2), target.getZ(),
                30, // Количество
                target.getBbWidth() / 2, target.getBbHeight() / 2, target.getBbWidth() / 2, // Разброс по хитбоксу
                0.1 // Скорость разлета
            );

            // Дополнительные эффекты, если лечим кого-то другого на расстоянии
            if (!target.equals(entity)) {
                
                // 3. Аура счастливого жителя (один пакет вместо самописного круга)
                serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                    15, 0.5, 0.5, 0.5, 0.0
                );

                // 4. Луч от игрока к цели
                Vec3 start = entity.getEyePosition(1.0F).subtract(0, 0.2, 0); // Чуть ниже глаз
                Vec3 end = target.position().add(0, target.getBbHeight() / 2, 0);
                Vec3 direction = end.subtract(start);
                
                int particles = 15; // Уменьшено до 15 для сохранения FPS без потери визуала
                for (int i = 0; i <= particles; i++) {
                    double progress = (double) i / particles;
                    Vec3 pos = start.add(direction.scale(progress));

                    // Отправляем по 1 частице вдоль линии
                    serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                        pos.x, pos.y, pos.z,
                        1, 0, 0, 0, 0
                    );
                }
            }

            // Уменьшаем прочность предмета
            if (entity instanceof Player player && !player.isCreative()) {
                itemstack.hurtAndBreak(1, player,
                    p -> p.broadcastBreakEvent(p.getUsedItemHand())
                );
            }
        }
    }
}