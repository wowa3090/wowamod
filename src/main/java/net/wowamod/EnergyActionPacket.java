package net.wowamod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import net.wowamod.core.AbilityConfig;
import net.wowamod.capability.EnergyCapability;
import net.wowamod.entity.EnergyBallEntity;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class EnergyActionPacket {
    private final int actionId;

    public EnergyActionPacket(int actionId) {
        this.actionId = actionId;
    }

    public EnergyActionPacket(FriendlyByteBuf buffer) {
        this.actionId = buffer.readInt();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(this.actionId);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            if (this.actionId == 0) { // СТАРТ
                if (!player.getItemInHand(InteractionHand.OFF_HAND).isEmpty()) return;
                if (!AbilityConfig.hasWowaArmor(player)) return;

                player.getCapability(EnergyCapability.ENERGY_CAPABILITY).ifPresent(cap -> {
                    cap.setCharging(true);
                    cap.setChargeTimer(0);
                    cap.setEmeraldFlags(AbilityConfig.getEmeraldFlags(player));
                });

            } else if (this.actionId == 1) { // ВЫСТРЕЛ
                player.getCapability(EnergyCapability.ENERGY_CAPABILITY).ifPresent(cap -> {
                    if (cap.isCharging()) {
                        fireBall(player, cap);
                        cap.setCharging(false);
                        cap.setChargeTimer(0);
                    }
                });
            }
        });
        context.setPacketHandled(true);
    }

    private void fireBall(ServerPlayer player, EnergyCapability cap) {
        int flags = cap.getEmeraldFlags();

        // --- ЛОГИКА АВТОНАВЕДЕНИЯ С ДОПУСКОМ 1.5 БЛОКА ---
        LivingEntity target = null;
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        double range = 256.0;
        Vec3 end = start.add(look.scale(range));
        
        // 1. Создаем широкую зону поиска вокруг луча взгляда
        // inflate(2.0) дает запас, чтобы найти сущностей рядом с лучом
        AABB searchBox = player.getBoundingBox().expandTowards(look.scale(range)).inflate(2.0);
        
        // 2. Получаем всех живых существ в этой зоне
        List<Entity> candidates = player.level().getEntities(player, searchBox, 
            e -> e instanceof LivingEntity && !e.isSpectator() && e.isPickable());

        double closestDist = range * range; // Дистанция до ближайшей цели
        
        // 3. Перебираем кандидатов
        for (Entity candidate : candidates) {
            // ВАЖНО: Увеличиваем хитбокс цели на 1.5 блока во все стороны
            // Это позволяет целиться "рядом", а не точно в модельку
            AABB expandedBox = candidate.getBoundingBox().inflate(1.5);
            
            // Проверяем, пересекает ли луч взгляда этот УВЕЛИЧЕННЫЙ хитбокс
            Optional<Vec3> hit = expandedBox.clip(start, end);
            
            if (hit.isPresent()) {
                double dist = start.distanceToSqr(hit.get());
                if (dist < closestDist) {
                    closestDist = dist;
                    target = (LivingEntity) candidate;
                }
            }
        }

        // ГОЛУБОЙ: Шар летит быстрее (скорость 3.0 вместо 1.5)
        float speed = (flags & 4) != 0 ? 3.0f : 1.5f;

        // Создание и выстрел основного шара
        createAndShoot(player, target, flags, speed, 1.0f, 0.0f);

        // СИНИЙ: Клоны (3-5 штук), урон 90%
        if ((flags & 8) != 0) {
            int count = 3 + player.getRandom().nextInt(3); // 3, 4 или 5
            for (int i = 0; i < count; i++) {
                // Небольшой разброс для клонов (0.3)
                createAndShoot(player, target, flags, speed * 0.9f, 0.9f, 0.3f);
            }
        }

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                net.minecraft.sounds.SoundEvents.ENDER_EYE_LAUNCH,
                net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    private EnergyBallEntity createAndShoot(ServerPlayer player, LivingEntity target, int flags, float speed, float damageMult, float inaccuracy) {
        EnergyBallEntity ball = new EnergyBallEntity(player.level(), player, target, flags, damageMult);
        
        // Правильная позиция спавна (чуть ниже глаз игрока)
        ball.setPos(player.getX(), player.getY() + player.getEyeHeight() - 0.5, player.getZ());
        
        Vec3 aim = player.getLookAngle();
        // Если есть разброс (например, у клонов), добавляем случайное отклонение к вектору
        if (inaccuracy > 0) {
            aim = aim.add(
                (player.getRandom().nextFloat() - 0.5) * inaccuracy,
                (player.getRandom().nextFloat() - 0.5) * inaccuracy,
                (player.getRandom().nextFloat() - 0.5) * inaccuracy
            );
        }
        
        ball.shoot(aim.x, aim.y, aim.z, speed, 0.0f);
        player.level().addFreshEntity(ball);
        
        return ball;
    }
}