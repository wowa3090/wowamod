package net.wowamod.item;

import net.wowamod.procedures.InstrumentprostoPriShchielchkiePKMPoBlokuProcedure;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.InteractionResult;
import net.minecraft.network.chat.Component;

import java.util.List;

public class InstrumentprostoItem extends Item {
    public InstrumentprostoItem() {
        super(new Item.Properties().durability(3090).fireResistant().rarity(Rarity.COMMON));
    }

    @Override
    public boolean hasCraftingRemainingItem() {
        return true;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack itemstack) {
        ItemStack retval = new ItemStack(this);
        retval.setDamageValue(itemstack.getDamageValue() + 1);
        if (retval.getDamageValue() >= retval.getMaxDamage()) {
            return ItemStack.EMPTY;
        }
        return retval;
    }

    @Override
    public boolean isRepairable(ItemStack itemstack) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack itemstack, Level world, List<Component> list, TooltipFlag flag) {
        super.appendHoverText(itemstack, world, list, flag);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        // Убрали super.useOn(context); чтобы не вызывать стандартную анимацию

        // Выполняем вашу процедуру из MCreator
        InstrumentprostoPriShchielchkiePKMPoBlokuProcedure.execute(
            context.getLevel(), 
            context.getClickedPos().getX(), 
            context.getClickedPos().getY(), 
            context.getClickedPos().getZ(), 
            context.getPlayer()
        );

        // Возвращаем PASS, чтобы игра не дёргала руку, но действие засчиталось
        return InteractionResult.PASS;
    }
}