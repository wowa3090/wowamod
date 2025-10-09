package net.wowamod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class DealLaserDamagePacket {

    private final int targetId;
    private final float damageAmount; // Теперь передаем и урон

    public DealLaserDamagePacket(int targetId, float damageAmount) {
        this.targetId = targetId;
        this.damageAmount = damageAmount;
    }

    public static void encode(DealLaserDamagePacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.targetId);
        buf.writeFloat(msg.damageAmount);
    }

    public static DealLaserDamagePacket decode(FriendlyByteBuf buf) {
        return new DealLaserDamagePacket(buf.readInt(), buf.readFloat());
    }

    public static void handle(DealLaserDamagePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Этот код выполнится на сервере
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            ServerLevel level = player.serverLevel();
            Entity target = level.getEntity(msg.targetId);

            if (target != null && target.isAlive()) {
                // Проверка на дистанцию остается
                if (player.distanceToSqr(target) < 100 * 100) {
                    // Используем урон из пакета
                    target.hurt(player.damageSources().playerAttack(player), msg.damageAmount);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}

