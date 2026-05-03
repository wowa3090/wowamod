package net.wowamod.network.wave;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

public class WaveManager extends SavedData {
    private final Map<UUID, Map<String, WaveNetwork>> playerNetworks = new HashMap<>();

    public static WaveManager get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(
                WaveManager::load, WaveManager::new, "wowamod_wave_networks"
        );
    }

    public WaveNetwork getOrCreateNetwork(UUID player, String waveName) {
        playerNetworks.putIfAbsent(player, new HashMap<>());
        Map<String, WaveNetwork> networks = playerNetworks.get(player);
        if (!networks.containsKey(waveName)) {
            // Лимит 100 млн FE. Можно увеличить до Long.MAX_VALUE для "бесконечных" сетей
            networks.put(waveName, new WaveNetwork(waveName, 100000000L, this)); 
            setDirty();
        }
        return networks.get(waveName);
    }

    // НОВОЕ: Удаление волны
    public void deleteWave(UUID player, String waveName) {
        if (playerNetworks.containsKey(player)) {
            playerNetworks.get(player).remove(waveName);
            setDirty();
        }
    }

    public List<String> getAllNetworkNames(UUID player) {
        if (playerNetworks.containsKey(player)) {
            return new ArrayList<>(playerNetworks.get(player).keySet());
        }
        return new ArrayList<>();
    }

    @Override
    public CompoundTag save(CompoundTag compound) {
        ListTag playersList = new ListTag();
        for (Map.Entry<UUID, Map<String, WaveNetwork>> entry : playerNetworks.entrySet()) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID("UUID", entry.getKey());
            ListTag networksList = new ListTag();
            for (WaveNetwork net : entry.getValue().values()) {
                networksList.add(net.save());
            }
            playerTag.put("Networks", networksList);
            playersList.add(playerTag);
        }
        compound.put("PlayerNetworks", playersList);
        return compound;
    }

    public static WaveManager load(CompoundTag compound) {
        WaveManager manager = new WaveManager();
        ListTag playersList = compound.getList("PlayerNetworks", Tag.TAG_COMPOUND);
        for (int i = 0; i < playersList.size(); i++) {
            CompoundTag playerTag = playersList.getCompound(i);
            UUID uuid = playerTag.getUUID("UUID");
            Map<String, WaveNetwork> nets = new HashMap<>();
            ListTag networksList = playerTag.getList("Networks", Tag.TAG_COMPOUND);
            for (int j = 0; j < networksList.size(); j++) {
                // Передаем менеджер в конструктор сети для обратной связи
                WaveNetwork net = WaveNetwork.load(networksList.getCompound(j), manager);
                nets.put(net.getName(), net);
            }
            manager.playerNetworks.put(uuid, nets);
        }
        return manager;
    }
}