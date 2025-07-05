
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.wowamod.init;

import net.wowamod.WowamodMod;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class WowamodModTabs {
	public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, WowamodMod.MODID);
	public static final RegistryObject<CreativeModeTab> WOWAMODVKLADKA = REGISTRY.register("wowamodvkladka",
			() -> CreativeModeTab.builder().title(Component.translatable("item_group.wowamod.wowamodvkladka")).icon(() -> new ItemStack(WowamodModItems.WOWABRONYA_HELMET.get())).displayItems((parameters, tabData) -> {
				tabData.accept(WowamodModItems.SOULCORE.get());
				tabData.accept(WowamodModItems.REDEMERALD.get());
				tabData.accept(WowamodModItems.GREENEMERALD.get());
				tabData.accept(WowamodModItems.BLUEEMERALD.get());
				tabData.accept(WowamodModItems.LIGHTBLUEEMERALD.get());
				tabData.accept(WowamodModItems.PURPLEEMERALD.get());
				tabData.accept(WowamodModItems.WHITEEMERALD.get());
				tabData.accept(WowamodModItems.YELLOWEMERALD.get());
				tabData.accept(WowamodModItems.RAINBOWEMERALD.get());
				tabData.accept(WowamodModItems.REDSOUL.get());
				tabData.accept(WowamodModItems.ORANGESOUL.get());
				tabData.accept(WowamodModItems.YELLOWSOUL.get());
				tabData.accept(WowamodModItems.GREENSOUL.get());
				tabData.accept(WowamodModItems.BLUESOUL.get());
				tabData.accept(WowamodModItems.LIGHTBLUESOUL.get());
				tabData.accept(WowamodModItems.PURPLESOUL.get());
				tabData.accept(WowamodModItems.REDSOULPIECE.get());
				tabData.accept(WowamodModItems.ORANGESOULPIECE.get());
				tabData.accept(WowamodModItems.YELLOWSOULPIECE.get());
				tabData.accept(WowamodModItems.GREENSOULPIECE.get());
				tabData.accept(WowamodModItems.BLUESOULPIECE.get());
				tabData.accept(WowamodModItems.LIGHTBLUESOULPIECE.get());
				tabData.accept(WowamodModItems.PURPLESOULPIECE.get());
				tabData.accept(WowamodModItems.SOULSVAZIVATEL.get());
				tabData.accept(WowamodModItems.SUPERPALKA.get());
				tabData.accept(WowamodModItems.DERJATILI.get());
				tabData.accept(WowamodModItems.MIMICTKANB.get());
				tabData.accept(WowamodModItems.MIMICKOJA.get());
				tabData.accept(WowamodModItems.FADINGWORLDBETA_2FAZASOUND.get());
				tabData.accept(WowamodModItems.ADARKPLACESOUND.get());
				tabData.accept(WowamodModItems.DISSASEBMLYREQUIRED.get());
				tabData.accept(WowamodModItems.NIGHTMARE_SPAWN_EGG.get());
				tabData.accept(WowamodModItems.WOWASWORDOSNOVA.get());
				tabData.accept(WowamodModItems.EKRANCHIK.get());
				tabData.accept(WowamodModItems.MAGICIRONINGOT.get());
				tabData.accept(WowamodModItems.EXTRACTLBSOUL.get());
				tabData.accept(WowamodModItems.KOLBA.get());
				tabData.accept(WowamodModItems.EXTRACTGOLUBAYSOUL_BUCKET.get());
				tabData.accept(WowamodModItems.KLAVA.get());
				tabData.accept(WowamodModItems.ROTOR.get());
				tabData.accept(WowamodModItems.EXTRACTREDSOUL.get());
				tabData.accept(WowamodModItems.GUIDEBOOKITEM.get());
				tabData.accept(WowamodModItems.WHITESOUL_MYPLACE.get());
				tabData.accept(WowamodModItems.BLACKSOUL_MYPLACE.get());
				tabData.accept(WowamodModItems.AAA_GLASS.get());
				tabData.accept(WowamodModItems.IRONMASS_1.get());
				tabData.accept(WowamodModItems.IRONPLASTINEWOWA.get());
				tabData.accept(WowamodModItems.OSNOVAAAAGLASS.get());
				tabData.accept(WowamodModItems.BLACKIRONINGOT.get());
				tabData.accept(WowamodModItems.COPPERMASS_1.get());
				tabData.accept(WowamodModItems.WIRESPROVODA.get());
				tabData.accept(WowamodModItems.INSTRUMENTPROSTO.get());
				tabData.accept(WowamodModItems.ALPHAELEMENT.get());
				tabData.accept(WowamodModBlocks.DARKGRASS.get().asItem());
				tabData.accept(WowamodModBlocks.DARKGRASSBLOCKNIZ.get().asItem());
				tabData.accept(WowamodModBlocks.DARK_BIOME_ORE.get().asItem());
				tabData.accept(WowamodModBlocks.ALUMINIUM_ORE_WOWA.get().asItem());
				tabData.accept(WowamodModItems.ETHERIUMDARK.get());
				tabData.accept(WowamodModItems.BATAREYKA.get());
				tabData.accept(WowamodModItems.ZAGATOVKABLACKIRON.get());
				tabData.accept(WowamodModItems.POSUDINA.get());
				tabData.accept(WowamodModItems.KARMANNIELECTROLIZER.get());
				tabData.accept(WowamodModItems.MOLOTWOWA.get());
				tabData.accept(WowamodModItems.IZOLATIONW.get());
				tabData.accept(WowamodModItems.COPPERPLATE.get());
				tabData.accept(WowamodModItems.MULTITOOLCOMPONENT.get());
				tabData.accept(WowamodModBlocks.POGRANICHNIK.get().asItem());
				tabData.accept(WowamodModBlocks.DARK_STRK_BLK.get().asItem());
				tabData.accept(WowamodModItems.RED_EMERALD_PIECE.get());
				tabData.accept(WowamodModItems.BLUE_EMERALD_PIECE.get());
				tabData.accept(WowamodModItems.WHITE_EMERALD_PIECE.get());
				tabData.accept(WowamodModItems.YELLOW_EMERALD_PIECE.get());
				tabData.accept(WowamodModItems.LIGHT_BLUE_EMERALD_PIECE.get());
				tabData.accept(WowamodModItems.PURPLE_EMERALD_PIECE.get());
				tabData.accept(WowamodModItems.GREEN_EMERALD_PIECE.get());
				tabData.accept(WowamodModItems.ALUMINIUM_INGOT.get());
				tabData.accept(WowamodModItems.ALUMINIUM_PLATE.get());
				tabData.accept(WowamodModBlocks.BLOCKFORPROFESSIAFIX.get().asItem());
				tabData.accept(WowamodModBlocks.CRAFTERBRONI.get().asItem());
				tabData.accept(WowamodModBlocks.EMERALD_COMBINER.get().asItem());
				tabData.accept(WowamodModBlocks.BIG_BATTERYW.get().asItem());
				tabData.accept(WowamodModBlocks.REDSTONECOMMANDBLOCK.get().asItem());
				tabData.accept(WowamodModBlocks.MAGICIRONBLOCK.get().asItem());
				tabData.accept(WowamodModBlocks.PORTCONSTRUCTOR.get().asItem());
				tabData.accept(WowamodModBlocks.INTERFACECONSTRUCTOR.get().asItem());
				tabData.accept(WowamodModBlocks.REDSTONEPORT.get().asItem());
				tabData.accept(WowamodModBlocks.EXTRACTOR.get().asItem());
				tabData.accept(WowamodModItems.NETHERSTARSWORD.get());
				tabData.accept(WowamodModItems.SHADOWBLADE.get());
				tabData.accept(WowamodModItems.WOWASWORD.get());
				tabData.accept(WowamodModItems.WOWABRONYA_HELMET.get());
				tabData.accept(WowamodModItems.WOWABRONYA_CHESTPLATE.get());
				tabData.accept(WowamodModItems.WOWABRONYA_LEGGINGS.get());
				tabData.accept(WowamodModItems.WOWABRONYA_BOOTS.get());
				tabData.accept(WowamodModBlocks.DARK_IRON_MACHINE_CASING.get().asItem());
				tabData.accept(WowamodModItems.DARKIRONPLASTINE.get());
				tabData.accept(WowamodModItems.ALUMINIUMDIAMONDBASE.get());
				tabData.accept(WowamodModItems.ENERGYELEMENT.get());
			})

					.build());
	public static final RegistryObject<CreativeModeTab> MYPLACEVKLADKA = REGISTRY.register("myplacevkladka",
			() -> CreativeModeTab.builder().title(Component.translatable("item_group.wowamod.myplacevkladka")).icon(() -> new ItemStack(WowamodModBlocks.TRUEROSE.get())).displayItems((parameters, tabData) -> {
				tabData.accept(WowamodModItems.ROSE.get());
				tabData.accept(WowamodModBlocks.CORRUPTED_SOULS_BLOCK.get().asItem());
				tabData.accept(WowamodModBlocks.BLACK_WALLS_MYPLACE.get().asItem());
				tabData.accept(WowamodModItems.MYPLACEZONE.get());
				tabData.accept(WowamodModBlocks.TRUEROSE.get().asItem());
			})

					.build());

	@SubscribeEvent
	public static void buildTabContentsVanilla(BuildCreativeModeTabContentsEvent tabData) {

		if (tabData.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
			tabData.accept(WowamodModItems.MIMIC_SPAWN_EGG.get());
		}
	}
}
