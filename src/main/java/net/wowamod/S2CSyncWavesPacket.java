package net.wowamod.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.wowamod.client.gui.EmitterScreen;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class S2CSyncWavesPacket {
    private final List<String> networks;

    public S2CSyncWavesPacket(List<String> networks) {
        this.networks = networks;
    }

    public S2CSyncWavesPacket(FriendlyByteBuf buf) {
        int size = buf.readInt();
        this.networks = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            this.networks.add(buf.readUtf(32));
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(networks.size());
        for (String net : networks) {
            buf.writeUtf(net, 32);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            // ВАЖНО: Сохраняем сети в кэш экрана, даже если он еще не открыт!
            EmitterScreen.cachedNetworks = new ArrayList<>(this.networks);
            
            if (Minecraft.getInstance().screen instanceof EmitterScreen screen) {
                screen.updateNetworks(this.networks);
            }
        });
        context.setPacketHandled(true);
    }
}