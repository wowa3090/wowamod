package net.wowamod; // САМАЯ ВАЖНАЯ СТРОЧКА! ДОЛЖНА БЫТЬ ТАКОЙ.

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;
import net.wowamod.block.entity.SmelterblockBlockEntity;

public class SmelterMenu extends AbstractContainerMenu {
    public final SmelterblockBlockEntity blockEntity;
    public final ContainerData data;

    public SmelterMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(4));
    }

    public SmelterMenu(int id, Inventory inv, BlockEntity entity, ContainerData data) {
        // Мы используем SmelterCrafts.SMELTER_MENU (импорт не нужен, т.к. файлы в одной папке)
        super(SmelterCrafts.SMELTER_MENU, id); 
        
        checkContainerSize(inv, 5);
        this.blockEntity = (SmelterblockBlockEntity) entity;
        this.data = data;

        addMenuSlots();
        addPlayerInventory(inv);
        addDataSlots(data);
    }

    private void addMenuSlots() {
        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            
            // Формат: new SlotItemHandler(handler, номер_слота, координата_X, координата_Y)
            // Я сдвинул предметы правее (с 26 на 44) и чуть выше, чтобы они попали в твои рамки.

            // 1. Входные предметы (Левый столбец)
            this.addSlot(new SlotItemHandler(handler, 0, 39, 13)); // Верхний слот (Слиток железа)
            this.addSlot(new SlotItemHandler(handler, 1, 39, 33)); // Средний слот (Слиток золота)
            this.addSlot(new SlotItemHandler(handler, 2, 39, 54)); // Нижний слот (Слиток меди)
            
            // 2. Катализатор (Красная пыль)
            // Сдвинул правее, подбирай под свою картинку, если будет немного криво (попробуй 76, 80 или 89)
            this.addSlot(new SlotItemHandler(handler, 3, 89, 52)); 
            
            // 3. Результат (Одиночный слот справа)
            this.addSlot(new SlotItemHandler(handler, 4, 133, 32) {
                @Override public boolean mayPlace(ItemStack stack) { return false; }
            });
        });
    }

    private void addPlayerInventory(Inventory playerInv) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInv, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInv, i, 8 + i * 18, 142));
        }
    }

    public int getScaledProgress() {
        int progress = this.data.get(0);
        int maxProgress = this.data.get(1);
        int arrowSize = 24; 
        return maxProgress != 0 && progress != 0 ? progress * arrowSize / maxProgress : 0;
    }

    public int getEnergy() {
        return (this.data.get(3) << 16) | this.data.get(2);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()), player, blockEntity.getBlockState().getBlock());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < 5) {
                if (!this.moveItemStackTo(itemstack1, 5, this.slots.size(), true)) return ItemStack.EMPTY;
            } else if (!this.moveItemStackTo(itemstack1, 0, 4, false)) {
                return ItemStack.EMPTY;
            }
            if (itemstack1.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return itemstack;
    }
}