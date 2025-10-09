package net.wowamod.procedures;

import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class CableUpdateTickEnergyProcedure {
    // Увеличенная скорость передачи энергии
    private static final int MAX_TRANSFER_RATE = 50000; // Увеличено с 1000 до 5000
    // Энергетические потери (0.0 = без потерь)
    private static final double ENERGY_LOSS_FACTOR = 0.00; // Уменьшено с 0.05 до 0.02

    public static void execute(LevelAccessor world, double x, double y, double z) {
        if (world.isClientSide()) {
            return; // Не запускать на клиентской стороне
        }

        BlockPos currentPos = BlockPos.containing(x, y, z);
        BlockEntity currentBlockEntity = world.getBlockEntity(currentPos);

        if (currentBlockEntity == null) {
            return;
        }

        // Получаем энергетическую способность текущего кабеля
        IEnergyStorage currentEnergyStorage = currentBlockEntity.getCapability(ForgeCapabilities.ENERGY, null).orElse(null);
        if (currentEnergyStorage == null || !currentEnergyStorage.canExtract()) {
            return; // Не можем извлекать энергию из этого блока
        }

        // Проверяем, есть ли энергия для передачи
        int availableEnergy = currentEnergyStorage.getEnergyStored();
        if (availableEnergy <= 0) {
            return; // Нет энергии для передачи
        }

        // Находим все допустимые соседние блоки, которые могут принимать энергию
        List<EnergyTransferTarget> targets = new ArrayList<>();
        
        // Проверяем все 6 направлений
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = currentPos.relative(direction);
            BlockEntity neighborEntity = world.getBlockEntity(neighborPos);
            
            if (neighborEntity != null) {
                // Проверяем, может ли соседний блок принимать энергию с противоположного направления
                IEnergyStorage neighborEnergyStorage = neighborEntity.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).orElse(null);
                
                if (neighborEnergyStorage != null && 
                    neighborEnergyStorage.canReceive() && 
                    neighborEnergyStorage.getEnergyStored() < neighborEnergyStorage.getMaxEnergyStored()) {
                    
                    // Добавляем в список только если блок может принять энергию
                    targets.add(new EnergyTransferTarget(neighborPos, neighborEntity, neighborEnergyStorage, direction.getOpposite()));
                }
            }
        }

        // Если нет подходящих целей, выходим
        if (targets.isEmpty()) {
            return;
        }

        // Рассчитываем, сколько энергии можем передать
        int totalPossibleTransfer = Math.min(availableEnergy, MAX_TRANSFER_RATE);
        int energyPerTarget = totalPossibleTransfer / Math.max(16, targets.size());

        int totalTransferred = 0;
        
        // Передаём энергию каждой цели
        for (EnergyTransferTarget target : targets) {
            if (currentEnergyStorage.getEnergyStored() <= 0) {
                break; // Нет энергии для передачи
            }

            // Определяем, сколько энергии будем передавать
            int amountToExtract = Math.min(energyPerTarget, MAX_TRANSFER_RATE);
            int energyAfterLoss = (int) (amountToExtract * (4.0 - ENERGY_LOSS_FACTOR));

            // Проверяем, сколько энергии может принять целевой блок
            int maxReceive = target.energyStorage.receiveEnergy(energyAfterLoss, true);
            
            if (maxReceive > 0) {
                // Извлекаем энергию из текущего блока
                int extracted = currentEnergyStorage.extractEnergy(amountToExtract, false);
                
                if (extracted > 0) {
                    // Передаём энергию в целевой блок
                    int received = target.energyStorage.receiveEnergy(Math.min(extracted, maxReceive), false);
                    
                    if (received > 0) {
                        totalTransferred += received;
                        
                        // Обновляем целевой блок
                        if (world instanceof Level level) {
                            level.sendBlockUpdated(target.pos, world.getBlockState(target.pos), world.getBlockState(target.pos), 3);
                        }
                    }
                }
            }
        }

        // Обновляем текущий блок, если энергия была передана
        if (totalTransferred > 0 && world instanceof Level level) {
            level.sendBlockUpdated(currentPos, world.getBlockState(currentPos), world.getBlockState(currentPos), 3);
        }
    }

    // Вспомогательный класс для хранения информации о целях передачи энергии
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