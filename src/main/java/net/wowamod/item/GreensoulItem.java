package net.wowamod.item;

import net.wowamod.procedures.GreensoulRightclickedProcedure;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.chat.Component;

import java.util.List;

public class GreensoulItem extends Item {
    public GreensoulItem() {
        super(new Item.Properties().durability(1311).rarity(Rarity.RARE));
    }

    @Override
    public void appendHoverText(ItemStack itemstack, Level world, List<Component> list, TooltipFlag flag) {
        super.appendHoverText(itemstack, world, list, flag);
        list.add(Component.literal("\u0414\u0443\u0448\u0430")); // "Душа"
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player entity, InteractionHand hand) {
        ItemStack itemstack = entity.getItemInHand(hand);
        
        // Вызов логики на серверной стороне
        if (!world.isClientSide()) {
            GreensoulRightclickedProcedure.execute(
                world, 
                entity, 
                itemstack
            );
        }
        
        // Добавляем перезарядку (20 тиков = 1 секунда), чтобы не спамили и не лагали эффектами
        entity.getCooldowns().addCooldown(this, 20);
        
        // Анимация использования
        entity.swing(hand, true);
        
        // Правильное возвращение результата
        return InteractionResultHolder.sidedSuccess(itemstack, world.isClientSide());
    }
    
    @Override
    public int getUseDuration(ItemStack itemstack) {
        return 20;
    }
}