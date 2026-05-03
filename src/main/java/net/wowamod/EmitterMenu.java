package net.wowamod.inventory;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.wowamod.block.entity.MWEmitterBlockBlockEntity;
import net.wowamod.registration.ModRegisterEnergySp;

public class EmitterMenu extends AbstractContainerMenu {
    public final MWEmitterBlockBlockEntity blockEntity;
    public final Player player;
    public final Level level;
    public final BlockPos pos;

    // Единый конструктор для сервера и клиента
    public EmitterMenu(int containerId, Inventory inv, BlockPos pos) {
        // Ссылаемся на наш независимый MenuType
        super(ModRegisterEnergySp.MW_EMITTER_MENU, containerId);
        this.player = inv.player;
        this.level = inv.player.level();
        this.pos = pos;

        // Пытаемся получить BlockEntity (на клиенте может быть null, мы учтем это в GUI)
        BlockEntity be = this.level.getBlockEntity(pos);
        if (be instanceof MWEmitterBlockBlockEntity emitter) {
            this.blockEntity = emitter;
        } else {
            this.blockEntity = null;
        }
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.blockEntity == null) return false;
        // Проверка дистанции (нельзя управлять блоком издалека)
        return player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0D;
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        return ItemStack.EMPTY; // Инвентаря нет
    }
}