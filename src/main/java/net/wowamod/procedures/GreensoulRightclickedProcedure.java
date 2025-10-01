package net.wowamod.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.Comparator;

public class GreensoulRightclickedProcedure {
    public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, ItemStack itemstack) {
        if (entity == null) return;

        // Параметры системы
        final double HEAL_AMOUNT = 4.0;
        final double RADIUS = 5.0; // Радиус поиска цели
        final double MAX_ANGLE = 0.2; // Максимальный угол от направления взгляда (в радианах)

        LivingEntity target = null;
        boolean healed = false; // Флаг успешного лечения

        if (entity instanceof Player) {
            Player player = (Player) entity;
            Vec3 lookVec = player.getLookAngle(); // Направление взгляда
            Vec3 startPos = player.getEyePosition(1.0F); // Позиция глаз игрока
            Vec3 endPos = startPos.add(lookVec.scale(RADIUS)); // Конечная точка луча взгляда

            // Определяем область поиска
            AABB searchArea = new AABB(
                Math.min(startPos.x, endPos.x) - RADIUS,
                Math.min(startPos.y, endPos.y) - RADIUS,
                Math.min(startPos.z, endPos.z) - RADIUS,
                Math.max(startPos.x, endPos.x) + RADIUS,
                Math.max(startPos.y, endPos.y) + RADIUS,
                Math.max(startPos.z, endPos.z) + RADIUS
            );

            // Получаем список сущностей в области
            List<Entity> entities = world.getEntitiesOfClass(Entity.class, searchArea);
            // Фильтруем: оставляем только LivingEntity, исключаем самого игрока
            entities.removeIf(e -> !(e instanceof LivingEntity) || e == player);
            // Сортируем по расстоянию до игрока
            entities.sort(Comparator.comparingDouble(e -> e.distanceToSqr(player)));

            // Проверяем каждую сущность в отсортированном списке
            for (Entity e : entities) {
                // Вектор от игрока до сущности
                Vec3 toEntity = e.position().subtract(startPos).normalize();
                // Вычисляем угол между направлением взгляда и вектором к сущности
                double angle = Math.acos(lookVec.dot(toEntity));

                // Если угол меньше порога, это наша цель
                if (angle <= MAX_ANGLE) {
                    target = (LivingEntity) e;
                    break; // Нашли ближайшую подходящую цель
                }
            }
        }

        // Если по направлению взгляда цель не найдена, лечим самого игрока
        if (target == null && entity instanceof LivingEntity) {
            target = (LivingEntity) entity;
        }

        // Лечение цели, если её здоровье не максимальное
        if (target != null && target.getHealth() < target.getMaxHealth()) {
            // Рассчитываем новое здоровье
            float health = target.getHealth();
            float newHealth = Math.min(health + (float) HEAL_AMOUNT, target.getMaxHealth());
            target.setHealth(newHealth);
            healed = true; // Устанавливаем флаг успешного лечения

            // Воспроизводим звук лечения
            world.playSound(null, BlockPos.containing(x, y, z),
                SoundEvents.EXPERIENCE_ORB_PICKUP, // Можно заменить на другой звук лечения
                SoundSource.PLAYERS,
                1.0F, // Громкость
                1.2F  // Высота тона
            );

            // Генерируем частицы 'HEART' над целью
            for (int i = 0; i < 12; i++) {
                world.addParticle(ParticleTypes.HEART,
                    target.getX() + (world.getRandom().nextDouble() - 0.5) * 0.5, // Случайное смещение X
                    target.getY() + 1.8 + (world.getRandom().nextDouble() - 0.5) * 0.3, // Случайное смещение Y над головой
                    target.getZ() + (world.getRandom().nextDouble() - 0.5) * 0.5, // Случайное смещение Z
                    0, 0.15, 0); // Скорость (маленькие прыжки)
            }

            // Дополнительные эффекты, если лечение НЕ для самого игрока
            if (healed && !target.equals(entity)) {
                Vec3 start = entity.getEyePosition(1.0F); // Начало луча (глаза игрока)
                Vec3 end = target.position().add(0, target.getBbHeight() / 2, 0); // Конец луча (центр цели)
                Vec3 direction = end.subtract(start); // Вектор направления луча

                // Создание частиц луча 'ELECTRIC_SPARK' от игрока к цели
                int particles = 30;
                for (int i = 0; i < particles; i++) {
                    double progress = (double) i / particles; // Прогресс вдоль луча (0.0 - 1.0)
                    Vec3 pos = start.add(direction.scale(progress)); // Позиция частицы на луче

                    world.addParticle(ParticleTypes.ELECTRIC_SPARK,
                        pos.x, pos.y, pos.z, // Координаты
                        0, 0.05, 0); // Маленькая вертикальная скорость
                }

                // Аура частиц 'HAPPY_VILLAGER' вокруг цели
                for (int i = 0; i < 15; i++) {
                    double angle = i * Math.PI * 2 / 15; // Угол для круга
                    double radius = 0.7; // Радиус ауры

                    world.addParticle(ParticleTypes.HAPPY_VILLAGER,
                        target.getX() + Math.cos(angle) * radius, // X по кругу
                        target.getY() + 1.0, // Y на уровне глаз цели
                        target.getZ() + Math.sin(angle) * radius, // Z по кругу
                        0, 0.1, 0); // Скорость
                }
            }

            // Применяем визуальный эффект сверкания к цели (независимо от того, сам игрок или нет)
            if (healed) {
                // Интенсивная вспышка частиц 'ELECTRIC_SPARK' из позиции цели
                for (int i = 0; i < 50; i++) { // Большое количество частиц для вспышки
                    double offsetX = (world.getRandom().nextDouble() - 0.5) * target.getBbWidth(); // Смещение в пределах хитбокса X
                    double offsetY = world.getRandom().nextDouble() * target.getBbHeight(); // Смещение в пределах хитбокса Y
                    double offsetZ = (world.getRandom().nextDouble() - 0.5) * target.getBbWidth(); // Смещение в пределах хитбокса Z

                    world.addParticle(ParticleTypes.ELECTRIC_SPARK,
                        target.getX() + offsetX, // X центра + смещение
                        target.getY() + offsetY, // Y центра + смещение
                        target.getZ() + offsetZ, // Z центра + смещение
                        (world.getRandom().nextDouble() - 0.5) * 0.2, // Маленькая случайная скорость X
                        (world.getRandom().nextDouble() - 0.5) * 0.2, // Маленькая случайная скорость Y
                        (world.getRandom().nextDouble() - 0.5) * 0.2  // Маленькая случайная скорость Z
                    );
                }
            }


            // Уменьшаем прочность предмета, если игрок не в творческом режиме
            if (entity instanceof Player) {
                Player player = (Player) entity;
                if (!player.isCreative()) {
                    itemstack.hurtAndBreak(1, player,
                        p -> p.broadcastBreakEvent(p.getUsedItemHand()) // Уведомление об износе
                    );
                }
            }
        }
        // Если лечение не произошло (здоровье цели уже максимальное), можно добавить другую реакцию
        // например, другой звук или сообщение игроку.
    }
}