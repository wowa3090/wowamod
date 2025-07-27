package net.wowamod.item;

import net.wowamod.procedures.STBHAbilities;
import net.wowamod.procedures.STBHLaserOnSwingProcedure; // Добавьте этот импорт, если MCreator потребует

import net.minecraft.world.level.Level;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.chat.Component;

import java.util.List;

public class STBHswordItem extends SwordItem {
    public STBHswordItem() {
        super(new Tier() {
            public int getUses() {
                return 6180;
            }

            public float getSpeed() {
                return 7f;
            }

            public float getAttackDamageBonus() {
                return 32f;
            }

            public int getLevel() {
                return 1;
            }

            public int getEnchantmentValue() {
                return 42;
            }

            public Ingredient getRepairIngredient() {
                return Ingredient.of();
            }
        }, 3, -2f, new Item.Properties().fireResistant());
    }

    @Override
    public boolean hurtEnemy(ItemStack itemstack, LivingEntity targetEntity, LivingEntity sourceEntity) {
        // При ударе по врагу (ЛКМ) вызываем "Небесный удар"
        STBHAbilities.executeHeavenlyStrike(targetEntity.level(), targetEntity, sourceEntity);
        return super.hurtEnemy(itemstack, targetEntity, sourceEntity);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        
        // На ПКМ теперь всегда вызывается "Усиление"
        STBHAbilities.executeEmpowerment(level, player, itemstack);
        
        return InteractionResultHolder.success(itemstack);
    }

    @Override
    public void appendHoverText(ItemStack itemstack, Level world, List<Component> list, TooltipFlag flag) {
        super.appendHoverText(itemstack, world, list, flag);
        // Обновляем подсказки
        list.add(Component.translatable("tooltip.wowamod.stbhsword.ability1")); // ПКМ: Усиление
        list.add(Component.translatable("tooltip.wowamod.stbhsword.ability2")); // ЛКМ в воздухе: Лазерный луч
        list.add(Component.translatable("tooltip.wowamod.stbhsword.ability3")); // ЛКМ по врагу: Небесный удар
    }
}