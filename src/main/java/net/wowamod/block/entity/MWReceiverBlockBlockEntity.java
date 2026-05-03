package net.wowamod.block.entity;

import net.wowamod.init.Universe3090ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import javax.annotation.Nullable;

public class MWReceiverBlockBlockEntity extends BlockEntity {

    public final EnergyStorage energyStorage = new EnergyStorage(100000, 50000, 50000, 0) {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int received = super.receiveEnergy(maxReceive, simulate);
            if (!simulate && received > 0) setChanged();
            return received;
        }
        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            int extracted = super.extractEnergy(maxExtract, simulate);
            if (!simulate && extracted > 0) setChanged();
            return extracted;
        }
    };

    private final IEnergyStorage externalProxy = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            if (!canReceive()) return 0;
            return energyStorage.receiveEnergy(maxReceive, simulate);
        }
        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            if (!canExtract()) return 0;
            return energyStorage.extractEnergy(maxExtract, simulate);
        }
        @Override public int getEnergyStored() { return energyStorage.getEnergyStored(); }
        @Override public int getMaxEnergyStored() { return energyStorage.getMaxEnergyStored(); }

        @Override
        public boolean canExtract() {
            MWEmitterBlockBlockEntity emitter = getEmitter();
            return emitter != null && emitter.isModeOutput();
        }

        @Override
        public boolean canReceive() {
            MWEmitterBlockBlockEntity emitter = getEmitter();
            return emitter != null && !emitter.isModeOutput();
        }
    };

    private LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(() -> externalProxy);
    private boolean wasLinked = false;

    public MWReceiverBlockBlockEntity(BlockPos pos, BlockState state) {
        super(Universe3090ModBlockEntities.MW_RECEIVER_BLOCK.get(), pos, state);
    }

    @Nullable
    public MWEmitterBlockBlockEntity getEmitter() {
        if (level == null) return null;
        BlockEntity be = level.getBlockEntity(worldPosition.above());
        return be instanceof MWEmitterBlockBlockEntity ? (MWEmitterBlockBlockEntity) be : null;
    }

    public void refreshPorts() {
        this.energyCap.invalidate();
        this.energyCap = LazyOptional.of(() -> externalProxy);
        if (level != null && !level.isClientSide) {
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        }
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;
        boolean isLinkedNow = getEmitter() != null;
        if (this.wasLinked != isLinkedNow) {
            this.wasLinked = isLinkedNow;
            refreshPorts();
            setChanged();
        }

        if (externalProxy.canExtract() && this.energyStorage.getEnergyStored() > 0) {
            for (Direction dir : Direction.values()) {
                if (dir == Direction.UP) continue; 
                BlockEntity neighbor = level.getBlockEntity(pos.relative(dir));
                if (neighbor != null) {
                    neighbor.getCapability(ForgeCapabilities.ENERGY, dir.getOpposite()).ifPresent(neighborEnergy -> {
                        if (neighborEnergy.canReceive()) {
                            int toExtract = Math.min(this.energyStorage.getEnergyStored(), 10000);
                            int accepted = neighborEnergy.receiveEnergy(toExtract, false);
                            if (accepted > 0) this.energyStorage.extractEnergy(accepted, false);
                        }
                    });
                }
            }
        }
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        if (compound.contains("Energy")) energyStorage.deserializeNBT(compound.get("Energy"));
    }

    @Override
    protected void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        compound.put("Energy", energyStorage.serializeNBT());
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY && side != Direction.UP) {
            return getEmitter() != null ? energyCap.cast() : LazyOptional.empty();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() { super.invalidateCaps(); energyCap.invalidate(); }
}