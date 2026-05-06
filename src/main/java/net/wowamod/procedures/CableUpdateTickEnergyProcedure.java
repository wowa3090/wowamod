package net.wowamod.procedures;

import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

import net.wowamod.block.entity.CableNBlockEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class CableUpdateTickEnergyProcedure {
    private static final int MAX_TRANSFER_RATE = 10000; // Мгновенная передача
    private static final int MAX_NETWORK_SIZE = 512; // Защита от зависаний при огромных сетях

    public static void execute(LevelAccessor world, double x, double y, double z) {
        if (world.isClientSide()) return;

        BlockPos currentPos = BlockPos.containing(x, y, z);
        BlockEntity currentBlockEntity = world.getBlockEntity(currentPos);
        
        if (!(currentBlockEntity instanceof CableNBlockEntity)) return;

        IEnergyStorage currentEnergy = currentBlockEntity.getCapability(ForgeCapabilities.ENERGY, null).orElse(null);
        if (currentEnergy == null) return;

        boolean changed = false;

        // ==========================================
        // ЭТАП 1: ВЫТЯГИВАНИЕ ИЗ ЧИСТЫХ ГЕНЕРАТОРОВ
        // ==========================================
        if (currentEnergy.getEnergyStored() < currentEnergy.getMaxEnergyStored()) {
            for (Direction direction : Direction.values()) {
                BlockEntity neighbor = world.getBlockEntity(currentPos.relative(direction));
                if (neighbor == null || neighbor instanceof CableNBlockEntity) continue;

                IEnergyStorage neighborEnergy = neighbor.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).orElse(null);
                
                if (neighborEnergy != null && neighborEnergy.canExtract() && !neighborEnergy.canReceive()) {
                    int space = currentEnergy.receiveEnergy(MAX_TRANSFER_RATE, true);
                    if (space > 0) {
                        int extracted = neighborEnergy.extractEnergy(space, false);
                        if (extracted > 0) {
                            currentEnergy.receiveEnergy(extracted, false);
                            neighbor.setChanged();
                            changed = true;
                        }
                    }
                }
            }
        }

        // ==========================================
        // ЭТАП 2: ПОИСК ПОЛУЧАТЕЛЕЙ ПО СЕТИ (BFS)
        // ==========================================
        int availableToTransfer = currentEnergy.extractEnergy(MAX_TRANSFER_RATE, true);

        if (availableToTransfer > 0) {
            List<IEnergyStorage> receivers = new ArrayList<>();
            Set<BlockPos> visited = new HashSet<>();
            Queue<BlockPos> queue = new LinkedList<>();

            queue.add(currentPos);
            visited.add(currentPos);

            int processedBlocks = 0;

            while (!queue.isEmpty() && processedBlocks < MAX_NETWORK_SIZE) {
                BlockPos pos = queue.poll();
                processedBlocks++;

                for (Direction dir : Direction.values()) {
                    BlockPos neighborPos = pos.relative(dir);
                    
                    if (visited.contains(neighborPos)) continue;
                    visited.add(neighborPos); // Помечаем, чтобы не проверять дважды
                    
                    BlockEntity be = world.getBlockEntity(neighborPos);
                    if (be == null) continue;

                    if (be instanceof CableNBlockEntity) {
                        queue.add(neighborPos); // Расширяем сеть кабелей
                    } else {
                        // Если это механизм и он может принимать энергию
                        IEnergyStorage cap = be.getCapability(ForgeCapabilities.ENERGY, dir.getOpposite()).orElse(null);
                        if (cap != null && cap.canReceive()) {
                            receivers.add(cap);
                        }
                    }
                }
            }

            // ==========================================
            // ЭТАП 3: РАВНОМЕРНОЕ РАСПРЕДЕЛЕНИЕ (Круговая порука)
            // ==========================================
            if (!receivers.isEmpty()) {
                // Перемешиваем список, чтобы при нехватке энергии дальние механизмы не голодали
                Collections.shuffle(receivers); 

                boolean energyDistributed = true;
                int totalExtractedFromCable = 0;

                // Крутим цикл раздачи до тех пор, пока есть энергия и хоть кто-то её принимает
                while (availableToTransfer > 0 && energyDistributed && !receivers.isEmpty()) {
                    energyDistributed = false;
                    
                    // Делим остаток поровну на всех голодных. Если энергии мало, даем хотя бы по 1 FE.
                    int amountPerMachine = Math.max(1, availableToTransfer / receivers.size());
                    
                    Iterator<IEnergyStorage> iterator = receivers.iterator();
                    while (iterator.hasNext()) {
                        IEnergyStorage receiver = iterator.next();
                        
                        int simReceived = receiver.receiveEnergy(amountPerMachine, true);
                        if (simReceived > 0) {
                            // Отправляем реально
                            int actualReceived = receiver.receiveEnergy(simReceived, false);
                            availableToTransfer -= actualReceived;
                            totalExtractedFromCable += actualReceived;
                            energyDistributed = true; // Мы кому-то отдали энергию, значит продолжаем цикл
                            
                            if (availableToTransfer <= 0) break;
                        } else {
                            // Механизм наелся или не принимает -> выкидываем из текущей очереди
                            iterator.remove();
                        }
                    }
                }

                // Списываем реально потраченную энергию из начального кабеля
                if (totalExtractedFromCable > 0) {
                    currentEnergy.extractEnergy(totalExtractedFromCable, false);
                    changed = true;
                }
            }
        }

        if (changed) {
            currentBlockEntity.setChanged();
        }
    }
}