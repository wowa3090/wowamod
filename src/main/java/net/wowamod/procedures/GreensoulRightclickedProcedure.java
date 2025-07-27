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
        final double RADIUS = 5.0;
        final double MAX_ANGLE = 0.2;
        
        LivingEntity target = null;
        boolean healed = false;
        
        if (entity instanceof Player) {
            Player player = (Player) entity;
            Vec3 lookVec = player.getLookAngle();
            Vec3 startPos = player.getEyePosition(1.0F);
            Vec3 endPos = startPos.add(lookVec.scale(RADIUS));
            
            AABB searchArea = new AABB(
                Math.min(startPos.x, endPos.x) - RADIUS,
                Math.min(startPos.y, endPos.y) - RADIUS,
                Math.min(startPos.z, endPos.z) - RADIUS,
                Math.max(startPos.x, endPos.x) + RADIUS,
                Math.max(startPos.y, endPos.y) + RADIUS,
                Math.max(startPos.z, endPos.z) + RADIUS
            );
            
            List<Entity> entities = world.getEntitiesOfClass(Entity.class, searchArea);
            entities.removeIf(e -> !(e instanceof LivingEntity) || e == player);
            entities.sort(Comparator.comparingDouble(e -> e.distanceToSqr(player)));
            
            for (Entity e : entities) {
                Vec3 toEntity = e.position().subtract(startPos).normalize();
                double angle = Math.acos(lookVec.dot(toEntity));
                
                if (angle <= MAX_ANGLE) {
                    target = (LivingEntity) e;
                    break;
                }
            }
        }
        
        // Если цель не найдена, лечим самого игрока
        if (target == null && entity instanceof LivingEntity) {
            target = (LivingEntity) entity;
        }
        
        // Лечение цели только если здоровье не максимальное
        if (target != null && target.getHealth() < target.getMaxHealth()) {
            // Лечение
            float health = target.getHealth();
            float newHealth = Math.min(health + (float) HEAL_AMOUNT, target.getMaxHealth());
            target.setHealth(newHealth);
            healed = true;
            
            // Звуковые эффекты
            world.playSound(null, BlockPos.containing(x, y, z), 
                SoundEvents.EXPERIENCE_ORB_PICKUP, 
                SoundSource.PLAYERS, 
                1.0F, 
                1.2F
            );
            
            // Частицы над целью
            for (int i = 0; i < 12; i++) {
                world.addParticle(ParticleTypes.HEART,
                    target.getX() + (world.getRandom().nextDouble() - 0.5) * 0.5,
                    target.getY() + 1.8 + (world.getRandom().nextDouble() - 0.5) * 0.3,
                    target.getZ() + (world.getRandom().nextDouble() - 0.5) * 0.5,
                    0, 0.15, 0);
            }
            
            // Зеленые частицы между игроком и целью (луч)
            if (healed && !target.equals(entity)) {
                Vec3 start = entity.getEyePosition(1.0F);
                Vec3 end = target.position().add(0, target.getBbHeight() / 2, 0);
                Vec3 direction = end.subtract(start);
                
                // Создание частиц вдоль линии
                int particles = 30;
                for (int i = 0; i < particles; i++) {
                    double progress = (double) i / particles;
                    Vec3 pos = start.add(direction.scale(progress));
                    
                    world.addParticle(ParticleTypes.ELECTRIC_SPARK,
                        pos.x, pos.y, pos.z,
                        0, 0.05, 0);
                }
                
                // Аура вокруг цели
                for (int i = 0; i < 15; i++) {
                    double angle = i * Math.PI * 2 / 15;
                    double radius = 0.7;
                    
                    world.addParticle(ParticleTypes.HAPPY_VILLAGER,
                        target.getX() + Math.cos(angle) * radius,
                        target.getY() + 1.0,
                        target.getZ() + Math.sin(angle) * radius,
                        0, 0.1, 0);
                }
            }
            
            // Повреждение предмета
            if (entity instanceof Player) {
                Player player = (Player) entity;
                if (!player.isCreative()) {
                    itemstack.hurtAndBreak(1, player, 
                        p -> p.broadcastBreakEvent(p.getUsedItemHand())
                    );
                }
            }
        }
    }
}