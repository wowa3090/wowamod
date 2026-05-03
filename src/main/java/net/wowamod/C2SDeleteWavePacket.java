package net.wowamod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.wowamod.network.wave.WaveManager;

import java.util.List;
import java.util.function.Supplier;

public class C2SDeleteWavePacket {
    private final String waveName;

    public C2SDeleteWavePacket(String waveName) {
        this.waveName = waveName;
    }

    public C2SDeleteWavePacket(FriendlyByteBuf buf) {
        this.waveName = buf.readUtf(32);
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(waveName, 32);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                // Удаляем сеть на сервере
                WaveManager manager = WaveManager.get(player.serverLevel());
                manager.deleteWave(player.getUUID(), this.waveName);
                
                // Мгновенно отправляем игроку обновленный список сетей
                List<String> updatedNetworks = manager.getAllNetworkNames(player.getUUID());
                MWNetwork.sendToPlayer(new S2CSyncWavesPacket(updatedNetworks), player);
            }
        });
        context.setPacketHandled(true);
    }
}