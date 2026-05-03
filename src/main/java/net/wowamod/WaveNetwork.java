package net.wowamod.network.wave;

import net.minecraft.nbt.CompoundTag;

public class WaveNetwork {
    private String name;
    private long currentEnergy;
    private long maxCapacity;
    private WaveManager parent; // Ссылка на менеджер для авто-сохранения

    public WaveNetwork(String name, long maxCapacity, WaveManager parent) {
        this.name = name;
        this.currentEnergy = 0;
        this.maxCapacity = maxCapacity;
        this.parent = parent;
    }

    public String getName() { return name; }
    public long getCurrentEnergy() { return currentEnergy; }
    public long getMaxCapacity() { return maxCapacity; }

    public long receiveEnergy(long maxReceive, boolean simulate) {
        long energyReceived = Math.min(maxCapacity - currentEnergy, Math.max(0, maxReceive));
        if (!simulate && energyReceived > 0) {
            currentEnergy += energyReceived;
            if (parent != null) parent.setDirty(); // Уведомляем менеджер о переменах
        }
        return energyReceived;
    }

    public long extractEnergy(long maxExtract, boolean simulate) {
        long energyExtracted = Math.min(currentEnergy, Math.max(0, maxExtract));
        if (!simulate && energyExtracted > 0) {
            currentEnergy -= energyExtracted;
            if (parent != null) parent.setDirty(); // Уведомляем менеджер о переменах
        }
        return energyExtracted;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Name", name);
        tag.putLong("Energy", currentEnergy);
        tag.putLong("Capacity", maxCapacity);
        return tag;
    }

    public static WaveNetwork load(CompoundTag tag, WaveManager parent) {
        WaveNetwork net = new WaveNetwork(tag.getString("Name"), tag.getLong("Capacity"), parent);
        net.currentEnergy = tag.getLong("Energy");
        return net;
    }
}