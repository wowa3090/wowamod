package net.wowamod.procedures;

import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class CableUpdateTickEnergyProcedure {
    private static final int MAX_TRANSFER_RATE = 50000;
    // Потери энергии: 0.0 = без потерь, 0.05 = потеря 5%
    private static final double ENERGY_LOSS_FACTOR = 0.00;

    public static void execute(LevelAccessor world, double x, double y, double z) {
        if (world.isClientSide()) return;

        BlockPos currentPos = BlockPos.containing(x, y, z);
        BlockEntity currentBlockEntity = world.getBlockEntity(currentPos);

        if (currentBlockEntity == null) return;

        IEnergyStorage currentEnergy = currentBlockEntity.getCapability(ForgeCapabilities.ENERGY, null).orElse(null);
        if (currentEnergy == null || !currentEnergy.canExtract()) return;

        // Сколько кабель реально может отдать прямо сейчас
        int maxAvailableToExtract = currentEnergy.extractEnergy(MAX_TRANSFER_RATE, true);
        if (maxAvailableToExtract <= 0) return;

        // Расчет процента заполненности текущего кабеля (как уровень воды)
        double currentMax = currentEnergy.getMaxEnergyStored();
        double currentFillRatio = currentMax > 0 ? (double) currentEnergy.getEnergyStored() / currentMax : 1.0;

        List<EnergyTransferTarget> targets = new ArrayList<>();

        // Собираем всех соседей
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = currentPos.relative(direction);
            BlockEntity neighborEntity = world.getBlockEntity(neighborPos);

            if (neighborEntity != null) {
                LazyOptional<IEnergyStorage> neighborCap = neighborEntity.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite());

                neighborCap.ifPresent(neighborEnergy -> {
                    if (neighborEnergy.canReceive()) {
                        double neighborMax = neighborEnergy.getMaxEnergyStored();
                        // Защита от деления на ноль у сломанных модов
                        double neighborFillRatio = neighborMax > 0 ? (double) neighborEnergy.getEnergyStored() / neighborMax : 1.0;

                        // Главное правило сообщающихся сосудов: энергия течет от более полного к менее полному!
                        if (neighborFillRatio < currentFillRatio) {
                            targets.add(new EnergyTransferTarget(neighborEntity, neighborEnergy, neighborFillRatio));
                        }
                    }
                });
            }
        }

        if (targets.isEmpty()) return;

        // Сортируем цели по возрастанию "сытости". Сначала питаем самые пустые механизмы!
        targets.sort((t1, t2) -> Double.compare(t1.fillRatio, t2.fillRatio));

        // Безопасный лимит потерь (не дает поставить 100% и сломать деление)
        double safeLossFactor = Math.min(0.99, Math.max(0.0, ENERGY_LOSS_FACTOR));
        
        int availableToTransfer = maxAvailableToExtract;
        int remainingTargets = targets.size();
        boolean transferredAny = false;

        // Динамическое распределение энергии
        for (EnergyTransferTarget target : targets) {
            if (availableToTransfer <= 0) break;

            // Выделяем долю для текущей цели
            int amountToSimulate = availableToTransfer / remainingTargets;
            if (amountToSimulate == 0) amountToSimulate = availableToTransfer;

            // 1. Сколько можем извлечь из кабеля для этой цели?
            int simExtracted = currentEnergy.extractEnergy(amountToSimulate, true);
            
            // 2. Учитываем потери в пути
            int afterLoss = (int) (simExtracted * (1.0 - safeLossFactor));
            
            // 3. Сколько механизм может принять из этого объема?
            int simReceived = target.energyStorage.receiveEnergy(afterLoss, true);

            if (simReceived > 0) {
                // 4. Математика: сколько нужно РЕАЛЬНО списать с кабеля, чтобы механизм получил simReceived.
                // Используем Math.ceil, чтобы не дюпать энергию при округлении вниз!
                int actualToExtractFromCable = (int) Math.ceil(simReceived / (1.0 - safeLossFactor));
                
                // Защита от перерасхода
                actualToExtractFromCable = Math.min(actualToExtractFromCable, availableToTransfer);

                // 5. ФИЗИЧЕСКИЙ ПЕРЕВОД
                int actualExtracted = currentEnergy.extractEnergy(actualToExtractFromCable, false);
                int actualReceived = target.energyStorage.receiveEnergy((int)(actualExtracted * (1.0 - safeLossFactor)), false);

                if (actualReceived > 0) {
                    availableToTransfer -= actualExtracted; // Уменьшаем доступную энергию для следующих целей
                    transferredAny = true;
                    target.blockEntity.setChanged(); // Тихо сохраняем механизм
                }
            }
            remainingTargets--; // Уменьшаем счетчик оставшихся целей
        }

        if (transferredAny) {
            currentBlockEntity.setChanged(); // Тихо сохраняем кабель
        }
    }

    private static class EnergyTransferTarget {
        public final BlockEntity blockEntity;
        public final IEnergyStorage energyStorage;
        public final double fillRatio;

        public EnergyTransferTarget(BlockEntity blockEntity, IEnergyStorage energyStorage, double fillRatio) {
            this.blockEntity = blockEntity;
            this.energyStorage = energyStorage;
            this.fillRatio = fillRatio;
        }
    }
}