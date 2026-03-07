package net.wowamod.procedures;

import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class CableUpdateTickEnergyProcedure {
    private static final int MAX_TRANSFER_RATE = 50000;

    public static void execute(LevelAccessor world, double x, double y, double z) {
        if (world.isClientSide()) return;

        BlockPos currentPos = BlockPos.containing(x, y, z);
        BlockEntity currentBlockEntity = world.getBlockEntity(currentPos);
        if (currentBlockEntity == null) return;

        IEnergyStorage currentEnergy = currentBlockEntity.getCapability(ForgeCapabilities.ENERGY, null).orElse(null);
        if (currentEnergy == null) return;

        boolean changed = false;

        List<EnergyTransferTarget> machines = new ArrayList<>();
        List<EnergyTransferTarget> cables = new ArrayList<>();

        // ==========================================
        // ЭТАП 1: АКТИВНЫЙ "ПЫЛЕСОС" И ПОИСК ЦЕЛЕЙ
        // ==========================================
        for (Direction direction : Direction.values()) {
            BlockEntity neighbor = world.getBlockEntity(currentPos.relative(direction));
            if (neighbor == null) continue;

            IEnergyStorage neighborEnergy = neighbor.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).orElse(null);
            if (neighborEnergy == null) continue;

            // Проверяем, является ли сосед таким же кабелем
            boolean isCable = neighbor.getBlockState().getBlock() == currentBlockEntity.getBlockState().getBlock();

            // 1. АКТИВНОЕ ВЫТЯГИВАНИЕ ИЗ ПАНЕЛЕЙ (ГЕНЕРАТОРОВ)
            // Если сосед - не кабель, МОЖЕТ отдавать энергию и НЕ МОЖЕТ принимать (чистый генератор)
            if (!isCable && neighborEnergy.canExtract() && !neighborEnergy.canReceive()) {
                int spaceInCable = currentEnergy.receiveEnergy(MAX_TRANSFER_RATE, true);
                if (spaceInCable > 0) {
                    // Силой забираем энергию у ленивой панели
                    int extracted = neighborEnergy.extractEnergy(spaceInCable, false);
                    if (extracted > 0) {
                        currentEnergy.receiveEnergy(extracted, false);
                        neighbor.setChanged();
                        changed = true;
                    }
                }
            }

            // 2. СОРТИРОВКА ПОЛУЧАТЕЛЕЙ (VIP-Маршрутизация)
            if (neighborEnergy.canReceive()) {
                if (!isCable) {
                    // Механизмы (Smelter и т.д.) отправляются в приоритетный список!
                    machines.add(new EnergyTransferTarget(neighbor, neighborEnergy));
                } else {
                    // Кабели отправляются во вторичный список (защита от пинг-понга: только если у соседа меньше энергии)
                    if (neighborEnergy.getEnergyStored() < currentEnergy.getEnergyStored()) {
                        cables.add(new EnergyTransferTarget(neighbor, neighborEnergy));
                    }
                }
            }
        }

        // ==========================================
        // ЭТАП 2: РАСПРЕДЕЛЕНИЕ (ПУШ)
        // ==========================================
        int availableToTransfer = currentEnergy.extractEnergy(MAX_TRANSFER_RATE, true);
        if (availableToTransfer > 0) {
            
            // ПРИОРИТЕТ 1: Кормим МЕХАНИЗМЫ 
            // Отдаем им всё, что они могут проглотить! Никаких задержек.
            if (!machines.isEmpty()) {
                int remainingMachines = machines.size();
                
                for (EnergyTransferTarget target : machines) {
                    if (availableToTransfer <= 0) break;
                    
                    // Делим оставшуюся энергию на количество голодных механизмов
                    int amountToOffer = availableToTransfer / remainingMachines;
                    if (amountToOffer == 0) amountToOffer = availableToTransfer;
                    
                    int simReceived = target.energyStorage.receiveEnergy(amountToOffer, true);
                    if (simReceived > 0) {
                        int actualExtracted = currentEnergy.extractEnergy(simReceived, false);
                        target.energyStorage.receiveEnergy(actualExtracted, false);
                        availableToTransfer -= actualExtracted;
                        target.blockEntity.setChanged();
                        changed = true;
                    }
                    remainingMachines--; // Уменьшаем счетчик, чтобы оставшиеся получили больше
                }
            }

            // ПРИОРИТЕТ 2: Если механизмы сыты, проталкиваем остатки дальше по КАБЕЛЯМ
            if (availableToTransfer > 0 && !cables.isEmpty()) {
                int perCable = availableToTransfer / cables.size();
                if (perCable == 0) perCable = availableToTransfer;

                for (EnergyTransferTarget target : cables) {
                    if (availableToTransfer <= 0) break;
                    
                    // Балансируем энергию между кабелями, чтобы она ползла вперед
                    int diff = currentEnergy.getEnergyStored() - target.energyStorage.getEnergyStored();
                    int maxToBalance = (int) Math.ceil(diff / 2.0);
                    int amountToPush = Math.min(perCable, maxToBalance);

                    if (amountToPush > 0) {
                        int simReceived = target.energyStorage.receiveEnergy(amountToPush, true);
                        if (simReceived > 0) {
                            int actualExtracted = currentEnergy.extractEnergy(simReceived, false);
                            target.energyStorage.receiveEnergy(actualExtracted, false);
                            availableToTransfer -= actualExtracted;
                            target.blockEntity.setChanged();
                            changed = true;
                        }
                    }
                }
            }
        }

        // Сохраняем состояние кабеля только если были реальные изменения
        if (changed) {
            currentBlockEntity.setChanged();
        }
    }

    // Вспомогательный класс-структура
    private static class EnergyTransferTarget {
        public final BlockEntity blockEntity;
        public final IEnergyStorage energyStorage;

        public EnergyTransferTarget(BlockEntity blockEntity, IEnergyStorage energyStorage) {
            this.blockEntity = blockEntity;
            this.energyStorage = energyStorage;
        }
    }
}