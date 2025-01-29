
package net.wowamod.item;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.network.chat.Component;

import java.util.List;

public class SoulcoreItem extends Item {
	public SoulcoreItem() {
		super(new Item.Properties().stacksTo(64).rarity(Rarity.EPIC));
	}

	@Override
	public void appendHoverText(ItemStack itemstack, Level world, List<Component> list, TooltipFlag flag) {
		super.appendHoverText(itemstack, world, list, flag);
		list.add(Component.literal("\u042F\u0434\u0440\u043E \u0434\u0443\u0448"));
		list.add(Component.literal(
				"\u043D\u0435\u043E\u0431\u0445\u043E\u0434\u0438\u043C\u043E \u0434\u043B\u044F \u0441\u043E\u0437\u0434\u0430\u043D\u0438\u044F \u043D\u0435\u043A\u043E\u0442\u043E\u0440\u044B\u0445 \u043F\u0440\u0435\u0434\u043C\u0435\u0442\u043E\u0432"));
	}
}
