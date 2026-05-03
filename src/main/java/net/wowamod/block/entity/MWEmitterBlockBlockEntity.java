package net.wowamod.block.entity;

import net.wowamod.block.MWEmitterBlockBlock;
import net.wowamod.init.Universe3090ModBlockEntities;
import net.wowamod.network.wave.WaveManager;
import net.wowamod.network.wave.WaveNetwork;
import net.wowamod.inventory.EmitterMenu;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

public class MWEmitterBlockBlockEntity extends BlockEntity implements MenuProvider {
    private UUID ownerUUID = null;
    private String activeWaveName = "";
    private boolean modeOutput = true; 
    private int transferLimit = 10000; 

    public final EnergyStorage energyStorage = new EnergyStorage(200000, 100000, 100000, 0) {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int rc = super.receiveEnergy(maxReceive, simulate);
            if (!simulate && rc > 0) setChanged();
            return rc;
        }
        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            int ex = super.extractEnergy(maxExtract, simulate);
            if (!simulate && ex > 0) setChanged();
            return ex;
        }
    };

    private final LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(() -> energyStorage);

    public MWEmitterBlockBlockEntity(BlockPos pos, BlockState state) {
        super(Universe3090ModBlockEntities.MW_EMITTER_BLOCK.get(), pos, state);
    }

    // --- НОВОЕ: СИНХРОНИЗАЦИЯ С КЛИЕНТОМ ---
    private void syncClient() {
        setChanged();
        if (level != null && !level.isClientSide) {
            // Отправляем пакет клиенту, чтобы GUI знал актуальные данные при открытии
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    // --- ГЕТТЕРЫ И СЕТТЕРЫ (Теперь они обновляют клиент!) ---
    public void setOwnerUUID(UUID uuid) { this.ownerUUID = uuid; syncClient(); }
    public UUID getOwnerUUID() { return ownerUUID; }
    
    public void setActiveWaveName(String name) { this.activeWaveName = name; syncClient(); }
    public String getActiveWaveName() { return activeWaveName; }
    
    public void setTransferLimit(int limit) { this.transferLimit = Math.max(1, limit); syncClient(); }
    public int getTransferLimit() { return transferLimit; }

    public boolean isModeOutput() { return this.modeOutput; }

    public void setModeOutput(boolean modeOutput) { 
        this.modeOutput = modeOutput; 
        syncClient(); 
        if (level != null && !level.isClientSide) {
            BlockEntity below = level.getBlockEntity(worldPosition.below());
            if (below instanceof MWReceiverBlockBlockEntity receiver) {
                receiver.refreshPorts();
            }
        }
    }

    // --- ЛОГИКА ТИКА ---
    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide || ownerUUID == null || activeWaveName.isEmpty()) return;
        BlockEntity blockEntityBelow = level.getBlockEntity(pos.below());
        if (!(blockEntityBelow instanceof MWReceiverBlockBlockEntity receiver)) return;

        WaveManager manager = WaveManager.get((ServerLevel) level);
        WaveNetwork network = manager.getOrCreateNetwork(ownerUUID, activeWaveName);

        if (modeOutput) {
            int spaceInBall = energyStorage.getMaxEnergyStored() - energyStorage.getEnergyStored();
            if (spaceInBall > 0) {
                long canExtract = network.extractEnergy(Math.min(transferLimit, spaceInBall), true);
                if (canExtract > 0) {
                    int accepted = energyStorage.receiveEnergy((int) canExtract, false);
                    network.extractEnergy(accepted, false);
                    manager.setDirty();
                }
            }
            int avail = energyStorage.getEnergyStored();
            if (avail > 0) {
                int acceptedByReceiver = receiver.energyStorage.receiveEnergy(avail, false);
                if (acceptedByReceiver > 0) energyStorage.extractEnergy(acceptedByReceiver, false);
            }
        } else {
            int spaceInBall = energyStorage.getMaxEnergyStored() - energyStorage.getEnergyStored();
            if (spaceInBall > 0) {
                int fromReceiver = receiver.energyStorage.extractEnergy(Math.min(transferLimit, spaceInBall), false);
                if (fromReceiver > 0) energyStorage.receiveEnergy(fromReceiver, false);
            }
            int avail = energyStorage.getEnergyStored();
            if (avail > 0) {
                long acceptedByCloud = network.receiveEnergy(avail, false);
                if (acceptedByCloud > 0) {
                    energyStorage.extractEnergy((int) acceptedByCloud, false);
                    manager.setDirty();
                }
            }
        }
        
        boolean shouldBeActive = ownerUUID != null && !activeWaveName.isEmpty() && (level.getBlockEntity(pos.below()) instanceof MWReceiverBlockBlockEntity);
        if (state.hasProperty(MWEmitterBlockBlock.ACTIVE) && state.getValue(MWEmitterBlockBlock.ACTIVE) != shouldBeActive) {
            level.setBlock(pos, state.setValue(MWEmitterBlockBlock.ACTIVE, shouldBeActive), 3);
        }
    }

    // --- СОХРАНЕНИЕ И ЗАГРУЗКА ---
    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        if (compound.contains("OwnerUUID")) this.ownerUUID = compound.getUUID("OwnerUUID");
        this.activeWaveName = compound.getString("ActiveWave");
        this.modeOutput = compound.getBoolean("ModeOutput");
        this.transferLimit = compound.getInt("TransferLimit");
        if (compound.contains("Energy")) energyStorage.deserializeNBT(compound.get("Energy"));
    }

    @Override
    protected void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        if (this.ownerUUID != null) compound.putUUID("OwnerUUID", this.ownerUUID);
        compound.putString("ActiveWave", this.activeWaveName);
        compound.putBoolean("ModeOutput", this.modeOutput);
        compound.putInt("TransferLimit", this.transferLimit);
        compound.put("Energy", energyStorage.serializeNBT());
    }

    // --- НОВОЕ: ПЕРЕДАЧА NBT КЛИЕНТУ ДЛЯ GUI ---
    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Nullable
    @Override
    public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) return energyCap.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() { super.invalidateCaps(); energyCap.invalidate(); }

    @Override
    public Component getDisplayName() { return Component.literal("Раздатчик"); }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new EmitterMenu(id, inv, this.worldPosition);
    }
}