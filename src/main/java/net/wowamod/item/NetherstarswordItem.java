
package net.wowamod.item;

import net.wowamod.procedures.NetherstarswordPriUdariePoSushchnostiProcedure;

import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.network.chat.Component;

import java.util.List;

public class NetherstarswordItem extends SwordItem {
	public NetherstarswordItem() {
		super(new Tier() {
			public int getUses() {
				return 4096;
			}

			public float getSpeed() {
				return 4f;
			}

			public float getAttackDamageBonus() {
				return 8.5f;
			}

			public int getLevel() {
				return 1;
			}

			public int getEnchantmentValue() {
				return 18;
			}

			public Ingredient getRepairIngredient() {
				return Ingredient.of();
			}
		}, 3, -2.25f, new Item.Properties());
	}

	@Override
	public boolean hurtEnemy(ItemStack itemstack, LivingEntity entity, LivingEntity sourceentity) {
		boolean retval = super.hurtEnemy(itemstack, entity, sourceentity);
		NetherstarswordPriUdariePoSushchnostiProcedure.execute(sourceentity, itemstack);
		return retval;
	}

	@Override
	public void appendHoverText(ItemStack itemstack, Level world, List<Component> list, TooltipFlag flag) {
		super.appendHoverText(itemstack, world, list, flag);
		list.add(Component.literal("\u0414\u0430\u0440\u0443\u0435\u0442 \u0432\u0430\u0441 \u0431\u043B\u0430\u0433\u043E\u0441\u043B\u043E\u0432\u043B\u0435\u043D\u0438\u0435 \u0437\u0432\u0435\u0437\u0434\u044B"));
		list.add(Component.literal(
				"\u0440\u0430\u0437 \u0432 30 \u043C\u0438\u043D \u043F\u0440\u0438 \u0443\u0434\u0430\u0440\u0435 \u043D\u0430 \u0432\u0430\u0441 \u043D\u0430\u043A\u043B\u0430\u0434\u044B\u0432\u0430\u0435\u0442\u0441\u044F \u044D\u0444\u0444\u0435\u043A\u0442 \"\u0411\u043B\u0430\u0433\u043E\u0441\u043B\u043E\u0432\u043B\u0435\u043D\u0438\u0435 \u0437\u0432\u0435\u0437\u0434\u044B\""));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean isFoil(ItemStack itemstack) {
		return true;
	}
}
