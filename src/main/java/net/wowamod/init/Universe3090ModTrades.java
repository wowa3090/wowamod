
/*
*	MCreator note: This file will be REGENERATED on each build.
*/
package net.wowamod.init;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.village.WandererTradesEvent;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.common.BasicItemListing;

import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.npc.VillagerProfession;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Universe3090ModTrades {
	@SubscribeEvent
	public static void registerWanderingTrades(WandererTradesEvent event) {
		event.getGenericTrades().add(new BasicItemListing(new ItemStack(Items.EMERALD, 32),

				new ItemStack(Universe3090ModItems.WHITEEMERALD.get()), 10, 10, 0.05f));
	}

	@SubscribeEvent
	public static void registerTrades(VillagerTradesEvent event) {
		if (event.getType() == VillagerProfession.WEAPONSMITH) {
			event.getTrades().get(3).add(new BasicItemListing(new ItemStack(Items.EMERALD, 16),

					new ItemStack(Universe3090ModItems.REDEMERALD.get()), 8, 5, 0.06f));
			event.getTrades().get(2).add(new BasicItemListing(new ItemStack(Items.EMERALD, 12),

					new ItemStack(Universe3090ModItems.GREENEMERALD.get()), 10, 5, 0.06f));
		}
		if (event.getType() == VillagerProfession.CARTOGRAPHER) {
			event.getTrades().get(3).add(new BasicItemListing(new ItemStack(Items.EMERALD, 18),

					new ItemStack(Universe3090ModItems.LIGHTBLUEEMERALD.get()), 6, 5, 0.06f));
			event.getTrades().get(3).add(new BasicItemListing(new ItemStack(Items.EMERALD, 20),

					new ItemStack(Universe3090ModItems.BLUEEMERALD.get()), 6, 5, 0.06f));
		}
		if (event.getType() == VillagerProfession.LIBRARIAN) {
			event.getTrades().get(1).add(new BasicItemListing(new ItemStack(Items.EMERALD, 10),

					new ItemStack(Universe3090ModItems.PURPLEEMERALD.get()), 10, 5, 0.05f));
		}
		if (event.getType() == VillagerProfession.FARMER) {
			event.getTrades().get(3).add(new BasicItemListing(new ItemStack(Items.EMERALD, 24),

					new ItemStack(Universe3090ModItems.YELLOWEMERALD.get()), 8, 5, 0.06f));
		}
		if (event.getType() == Universe3090ModVillagerProfessions.EMERALDSHOPPER.get()) {
			event.getTrades().get(1).add(new BasicItemListing(new ItemStack(Items.EMERALD, 12),

					new ItemStack(Universe3090ModItems.GREENEMERALD.get()), 10, 5, 0.05f));
			event.getTrades().get(1).add(new BasicItemListing(new ItemStack(Items.EMERALD, 16),

					new ItemStack(Universe3090ModItems.YELLOWEMERALD.get()), 10, 5, 0.05f));
			event.getTrades().get(2).add(new BasicItemListing(new ItemStack(Items.EMERALD, 18),

					new ItemStack(Universe3090ModItems.PURPLEEMERALD.get()), 10, 5, 0.05f));
			event.getTrades().get(2).add(new BasicItemListing(new ItemStack(Items.EMERALD, 12),

					new ItemStack(Universe3090ModItems.BLUEEMERALD.get()), 10, 5, 0.05f));
			event.getTrades().get(3).add(new BasicItemListing(new ItemStack(Items.EMERALD, 14),

					new ItemStack(Universe3090ModItems.LIGHTBLUEEMERALD.get()), 10, 5, 0.05f));
			event.getTrades().get(3).add(new BasicItemListing(new ItemStack(Items.EMERALD, 16),

					new ItemStack(Universe3090ModItems.WHITEEMERALD.get()), 10, 5, 0.05f));
			event.getTrades().get(4).add(new BasicItemListing(new ItemStack(Items.EMERALD, 20),

					new ItemStack(Universe3090ModItems.RAINBOWEMERALD.get()), 10, 5, 0.05f));
			event.getTrades().get(1).add(new BasicItemListing(new ItemStack(Universe3090ModItems.RAINBOWEMERALD.get()),

					new ItemStack(Items.EMERALD, 8), 15, 5, 0.1f));
		}
	}
}
