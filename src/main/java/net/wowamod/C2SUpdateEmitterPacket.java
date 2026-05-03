package net.wowamod.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import net.wowamod.block.entity.MWEmitterBlockBlockEntity;
import net.wowamod.network.wave.WaveManager;

import java.util.function.Supplier;

public class C2SUpdateEmitterPacket {
    private final BlockPos pos;
    private final String waveName;
    private final boolean modeOutput;
    private final int transferLimit;

    public C2SUpdateEmitterPacket(BlockPos pos, String waveName, boolean modeOutput, int transferLimit) {
        this.pos = pos;
        this.waveName = waveName;
        this.modeOutput = modeOutput;
        this.transferLimit = transferLimit;
    }

    public C2SUpdateEmitterPacket(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.waveName = buf.readUtf(32);
        this.modeOutput = buf.readBoolean();
        this.transferLimit = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeUtf(waveName, 32);
        buf.writeBoolean(modeOutput);
        buf.writeInt(transferLimit);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            System.out.println("[WOWA-LOG] Пакет от клиента получен! Сеть: " + waveName + ", Режим Отдача: " + modeOutput);

            if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > 64) {
                System.out.println("[WOWA-LOG] ОШИБКА: Игрок слишком далеко от блока.");
                return;
            }

            BlockEntity be = player.level().getBlockEntity(pos);
            if (be instanceof MWEmitterBlockBlockEntity emitter) {
                // Если блок багнутый (поставлен до обновы)
                if (emitter.getOwnerUUID() == null) {
                    System.out.println("[WOWA-LOG] ФИКС: У блока не было владельца. Назначаем владельцем " + player.getName().getString());
                    emitter.setOwnerUUID(player.getUUID());
                }

                if (emitter.getOwnerUUID().equals(player.getUUID())) {
                    emitter.setActiveWaveName(this.waveName);
                    emitter.setModeOutput(this.modeOutput);
                    emitter.setTransferLimit(Math.max(1, Math.min(this.transferLimit, 1000000)));

                    // ПРИНУДИТЕЛЬНО Создаем сеть в Менеджере
                    WaveManager manager = WaveManager.get(player.serverLevel());
                    manager.getOrCreateNetwork(player.getUUID(), this.waveName);
                    
                    System.out.println("[WOWA-LOG] УСПЕХ! Сеть '" + this.waveName + "' успешно сохранена и создана.");
                } else {
                    System.out.println("[WOWA-LOG] ОШИБКА: UUID игрока не совпадает с UUID блока!");
                }
            } else {
                System.out.println("[WOWA-LOG] ОШИБКА: По координатам " + pos + " нет MWEmitterBlockBlockEntity.");
            }
        });
        context.setPacketHandled(true);
    }
}