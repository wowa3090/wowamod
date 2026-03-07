package net.wowamod.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.items.ItemStackHandler;
import net.wowamod.block.SmelterblockBlock;
import net.wowamod.init.Universe3090ModBlockEntities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.wowamod.SmelterMenu;
import net.wowamod.SmelterCrafts;

public class SmelterblockBlockEntity extends BlockEntity implements MenuProvider {

    public final ItemStackHandler itemHandler = new ItemStackHandler(5) {
        @Override
        protected void onContentsChanged(int slot) { setChanged(); }
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slot != 4; // В слот выхода (4) класть предметы нельзя
        }
    };

    // ИСПРАВЛЕНО: maxExtract теперь 10000, чтобы позволить внутреннему коду тратить энергию
    public final EnergyStorage energyStorage = new EnergyStorage(400000, 1000, 10000, 0) {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int received = super.receiveEnergy(maxReceive, simulate);
            if (received > 0 && !simulate) {
                setChanged();
            }
            return received;
        }

        // ИСПРАВЛЕНО: Добавлено сохранение NBT при трате энергии
        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            int extracted = super.extractEnergy(maxExtract, simulate);
            if (extracted > 0 && !simulate) {
                setChanged();
            }
            return extracted;
        }
    };

    private final LazyOptional<ItemStackHandler> lazyItemHandler = LazyOptional.of(() -> itemHandler);
    private final LazyOptional<EnergyStorage> lazyEnergyHandler = LazyOptional.of(() -> energyStorage);

    public int progress = 0;
    public int maxProgress = 0;

    protected final ContainerData data;

    public SmelterblockBlockEntity(BlockPos pos, BlockState state) {
        super(Universe3090ModBlockEntities.SMELTERBLOCK.get(), pos, state);
        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> SmelterblockBlockEntity.this.progress;
                    case 1 -> SmelterblockBlockEntity.this.maxProgress;
                    case 2 -> SmelterblockBlockEntity.this.energyStorage.getEnergyStored() & 0xFFFF;
                    case 3 -> (SmelterblockBlockEntity.this.energyStorage.getEnergyStored() >> 16) & 0xFFFF;
                    default -> 0;
                };
            }
            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> SmelterblockBlockEntity.this.progress = value;
                    case 1 -> SmelterblockBlockEntity.this.maxProgress = value;
                }
            }
            @Override
            public int getCount() { return 4; }
        };
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;

        SmelterCrafts.SmelterRecipe recipe = SmelterCrafts.getRecipe(
                itemHandler.getStackInSlot(0), itemHandler.getStackInSlot(1),
                itemHandler.getStackInSlot(2), itemHandler.getStackInSlot(3));

        if (recipe != null && hasEnoughSpace(recipe.output)) {
            maxProgress = recipe.processTime;
            
            // ИСПРАВЛЕНО: Вычисляем, сколько энергии нужно потратить ровно за 1 тик.
            // Math.ceil округляет в большую сторону (чтобы крафт не оказался бесплатным, если стоимость мала)
            int energyPerTick = (int) Math.ceil((double) recipe.energyCost / recipe.processTime);

            // Проверяем, есть ли энергия хотя бы на этот тик
            if (energyStorage.getEnergyStored() >= energyPerTick) {
                energyStorage.extractEnergy(energyPerTick, false); // Тратим энергию по кусочкам
                progress++;
                
                if (progress >= maxProgress) {
                    craftItem(recipe);
                    progress = 0;
                }
                setChanged(level, pos, state);
            }
            // Если рецепт валиден, но энергии нет, мы просто ничего не делаем. 
            // Прогресс замирает на месте ("Пауза" механизма).
        } else {
            // Сбрасываем прогресс ТОЛЬКО если убрали нужные предметы или переполнен выходной слот
            if (progress != 0 || maxProgress != 0) {
                progress = 0;
                maxProgress = 0;
                setChanged(level, pos, state);
            }
        }
    }

    private boolean hasEnoughSpace(ItemStack output) {
        ItemStack currentOutput = itemHandler.getStackInSlot(4);
        return currentOutput.isEmpty() || (currentOutput.is(output.getItem()) && currentOutput.getCount() + output.getCount() <= currentOutput.getMaxStackSize());
    }

    private void craftItem(SmelterCrafts.SmelterRecipe recipe) {
        for (int i = 0; i < 4; i++) {
            itemHandler.extractItem(i, 1, false);
        }

        ItemStack currentOutput = itemHandler.getStackInSlot(4);
        if (currentOutput.isEmpty()) {
            itemHandler.setStackInSlot(4, recipe.output.copy());
        } else {
            currentOutput.grow(recipe.output.getCount());
            itemHandler.setStackInSlot(4, currentOutput);
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }
        if (cap == ForgeCapabilities.ENERGY) {
            if (side == null) return lazyEnergyHandler.cast();
            if (this.getBlockState().hasProperty(SmelterblockBlock.FACING)) {
                Direction back = this.getBlockState().getValue(SmelterblockBlock.FACING).getOpposite();
                if (side == back) {
                    return lazyEnergyHandler.cast();
                }
            }
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
        lazyEnergyHandler.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.put("energy", energyStorage.serializeNBT());
        nbt.putInt("progress", progress);
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("inventory"));
        energyStorage.deserializeNBT(nbt.get("energy"));
        progress = nbt.getInt("progress");
    }

    @Override
    public Component getDisplayName() { return Component.literal("Smelter"); }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInv, Player player) {
        return new SmelterMenu(id, playerInv, this, this.data);
    }
}