package net.wowamod.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EnergyCapability implements INBTSerializable<CompoundTag> {
    public static final Capability<EnergyCapability> ENERGY_CAPABILITY = 
            CapabilityManager.get(new CapabilityToken<>() {});

    private boolean isCharging = false;
    private int chargeTimer = 0;
    private int emeraldFlags = 0;

    public boolean isCharging() { return isCharging; }
    public void setCharging(boolean charging) { isCharging = charging; }
    public int getChargeTimer() { return chargeTimer; }
    public void setChargeTimer(int timer) { chargeTimer = timer; }
    public int getEmeraldFlags() { return emeraldFlags; }
    public void setEmeraldFlags(int flags) { emeraldFlags = flags; }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("isCharging", isCharging);
        tag.putInt("chargeTimer", chargeTimer);
        tag.putInt("emeraldFlags", emeraldFlags);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        isCharging = tag.getBoolean("isCharging");
        chargeTimer = tag.getInt("chargeTimer");
        emeraldFlags = tag.getInt("emeraldFlags");
    }

    public static class Provider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
        private final EnergyCapability instance = new EnergyCapability();
        private final LazyOptional<EnergyCapability> optional = LazyOptional.of(() -> instance);

        @NotNull
        @Override
        public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            return ENERGY_CAPABILITY.orEmpty(cap, optional);
        }

        @Override
        public CompoundTag serializeNBT() {
            return instance.serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            instance.deserializeNBT(nbt);
        }
    }
}