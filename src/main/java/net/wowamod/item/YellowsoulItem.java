
package net.wowamod.item;

import net.wowamod.procedures.YellowsoulRightclickedProcedure;

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

public class YellowsoulItem extends Item {
	public YellowsoulItem() {
		super(new Item.Properties().stacksTo(64).rarity(Rarity.RARE));
	}

	@Override
	public void appendHoverText(ItemStack itemstack, Level world, List<Component> list, TooltipFlag flag) {
		super.appendHoverText(itemstack, world, list, flag);
		list.add(Component.literal("\u0414\u0443\u0448\u0430"));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player entity, InteractionHand hand) {
	    InteractionResultHolder<ItemStack> ar = super.use(world, entity, hand);
	    // You must create and pass the dependencies map
	    java.util.Map<String, Object> dependencies = new java.util.HashMap<>();
	    dependencies.put("world", world);
	    dependencies.put("entity", entity);
	    YellowsoulRightclickedProcedure.execute(dependencies);
	    return ar;
	}
}
