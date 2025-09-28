package net.wowamod.procedures;

import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class CableUpdateTickEnergyProcedure {
    // Define the maximum amount of energy that can be transferred per tick
    private static final int MAX_TRANSFER_RATE = 1000; // Adjust as needed
    // Define energy loss percentage (0.0 = no loss, 0.1 = 10% loss, etc.)
    private static final double ENERGY_LOSS_FACTOR = 0.05; // 5% loss per hop

    public static void execute(LevelAccessor world, double x, double y, double z) {
        if (world.isClientSide()) {
            return; // Don't run on client side
        }

        BlockPos currentPos = BlockPos.containing(x, y, z);
        BlockEntity currentBlockEntity = world.getBlockEntity(currentPos);

        if (currentBlockEntity == null) {
            return;
        }

        // Get the energy capability of the current cable block
        IEnergyStorage currentEnergyStorage = currentBlockEntity.getCapability(ForgeCapabilities.ENERGY, null).orElse(null);
        if (currentEnergyStorage == null || !currentEnergyStorage.canExtract()) {
            return; // Can't extract energy from this block
        }

        // Find all valid adjacent blocks that can receive energy
        List<EnergyTransferTarget> targets = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = currentPos.relative(direction);
            BlockEntity neighborEntity = world.getBlockEntity(neighborPos);
            
            if (neighborEntity != null) {
                IEnergyStorage neighborEnergyStorage = neighborEntity.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).orElse(null);
                
                if (neighborEnergyStorage != null && neighborEnergyStorage.canReceive() && 
                    neighborEnergyStorage.getEnergyStored() < neighborEnergyStorage.getMaxEnergyStored()) {
                    targets.add(new EnergyTransferTarget(neighborPos, neighborEntity, neighborEnergyStorage, direction.getOpposite()));
                }
            }
        }

        // If there are no valid targets, nothing to do
        if (targets.isEmpty()) {
            return;
        }

        // Calculate how much energy we can extract from the current block
        int availableEnergy = currentEnergyStorage.getEnergyStored();
        if (availableEnergy <= 0) {
            return; // No energy to transfer
        }

        // Distribute energy equally among all valid targets
        int energyPerTarget = availableEnergy / targets.size();
        if (energyPerTarget <= 0) {
            return; // Not enough energy to distribute
        }

        int totalTransferred = 0;
        
        // Transfer energy to each target
        for (EnergyTransferTarget target : targets) {
            if (currentEnergyStorage.getEnergyStored() <= 0) {
                break; // No more energy to transfer
            }

            // Calculate transfer amount considering the loss factor
            int extractAmount = Math.min(energyPerTarget, MAX_TRANSFER_RATE);
            int energyToTransfer = (int) (extractAmount * (1.0 - ENERGY_LOSS_FACTOR));
            
            // Simulate receiving energy to see how much can actually be accepted
            int simulatedReceived = target.energyStorage.receiveEnergy(energyToTransfer, true);
            
            // Extract energy from the current block
            int extracted = currentEnergyStorage.extractEnergy(extractAmount, false);
            
            // Actually transfer the energy to the target
            if (extracted > 0 && simulatedReceived > 0) {
                int actualReceived = target.energyStorage.receiveEnergy(Math.min(extracted, simulatedReceived), false);
                totalTransferred += actualReceived;
                
                // Update the neighbor block entity if needed
                if (world instanceof Level level) {
                    level.sendBlockUpdated(target.pos, world.getBlockState(target.pos), world.getBlockState(target.pos), 3);
                }
            }
        }

        // Update the current block entity if energy was transferred
        if (totalTransferred > 0 && world instanceof Level level) {
            level.sendBlockUpdated(currentPos, world.getBlockState(currentPos), world.getBlockState(currentPos), 3);
        }
    }

    // Helper class to store information about energy transfer targets
    private static class EnergyTransferTarget {
        public final BlockPos pos;
        public final BlockEntity blockEntity;
        public final IEnergyStorage energyStorage;
        public final Direction side;

        public EnergyTransferTarget(BlockPos pos, BlockEntity blockEntity, IEnergyStorage energyStorage, Direction side) {
            this.pos = pos;
            this.blockEntity = blockEntity;
            this.energyStorage = energyStorage;
            this.side = side;
        }
    }
}