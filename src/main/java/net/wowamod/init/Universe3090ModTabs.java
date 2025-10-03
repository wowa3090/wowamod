
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.wowamod.init;

import net.wowamod.Universe3090Mod;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;

public class Universe3090ModTabs {
	public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Universe3090Mod.MODID);
	public static final RegistryObject<CreativeModeTab> WOWAMODVKLADKA = REGISTRY.register("wowamodvkladka",
			() -> CreativeModeTab.builder().title(Component.translatable("item_group.universe3090.wowamodvkladka")).icon(() -> new ItemStack(Universe3090ModItems.WOWABRONYA_HELMET.get())).displayItems((parameters, tabData) -> {
				tabData.accept(Universe3090ModItems.WOWABRONYA_HELMET.get());
				tabData.accept(Universe3090ModItems.WOWABRONYA_CHESTPLATE.get());
				tabData.accept(Universe3090ModItems.WOWABRONYA_LEGGINGS.get());
				tabData.accept(Universe3090ModItems.WOWABRONYA_BOOTS.get());
				tabData.accept(Universe3090ModItems.NETHERSTARSWORD.get());
				tabData.accept(Universe3090ModItems.SHADOWBLADE.get());
				tabData.accept(Universe3090ModItems.WOWASWORD.get());
				tabData.accept(Universe3090ModItems.STB_HSWORD.get());
				tabData.accept(Universe3090ModItems.GUIDEBOOKITEM.get());
				tabData.accept(Universe3090ModItems.SOULCORE.get());
				tabData.accept(Universe3090ModItems.REDEMERALD.get());
				tabData.accept(Universe3090ModItems.GREENEMERALD.get());
				tabData.accept(Universe3090ModItems.BLUEEMERALD.get());
				tabData.accept(Universe3090ModItems.LIGHTBLUEEMERALD.get());
				tabData.accept(Universe3090ModItems.PURPLEEMERALD.get());
				tabData.accept(Universe3090ModItems.WHITEEMERALD.get());
				tabData.accept(Universe3090ModItems.YELLOWEMERALD.get());
				tabData.accept(Universe3090ModItems.RAINBOWEMERALD.get());
				tabData.accept(Universe3090ModItems.REDSOUL.get());
				tabData.accept(Universe3090ModItems.ORANGESOUL.get());
				tabData.accept(Universe3090ModItems.YELLOWSOUL.get());
				tabData.accept(Universe3090ModItems.GREENSOUL.get());
				tabData.accept(Universe3090ModItems.BLUESOUL.get());
				tabData.accept(Universe3090ModItems.LIGHTBLUESOUL.get());
				tabData.accept(Universe3090ModItems.PURPLESOUL.get());
				tabData.accept(Universe3090ModItems.REDSOULPIECE.get());
				tabData.accept(Universe3090ModItems.ORANGESOULPIECE.get());
				tabData.accept(Universe3090ModItems.YELLOWSOULPIECE.get());
				tabData.accept(Universe3090ModItems.GREENSOULPIECE.get());
				tabData.accept(Universe3090ModItems.BLUESOULPIECE.get());
				tabData.accept(Universe3090ModItems.LIGHTBLUESOULPIECE.get());
				tabData.accept(Universe3090ModItems.PURPLESOULPIECE.get());
				tabData.accept(Universe3090ModItems.SOULSVAZIVATEL.get());
				tabData.accept(Universe3090ModItems.SUPERPALKA.get());
				tabData.accept(Universe3090ModItems.DERJATILI.get());
				tabData.accept(Universe3090ModItems.MIMICTKANB.get());
				tabData.accept(Universe3090ModItems.MIMICKOJA.get());
				tabData.accept(Universe3090ModItems.ADARKPLACESOUND.get());
				tabData.accept(Universe3090ModItems.DISSASEBMLYREQUIRED.get());
				tabData.accept(Universe3090ModItems.NIGHTMARE_SPAWN_EGG.get());
				tabData.accept(Universe3090ModItems.WOWASWORDOSNOVA.get());
				tabData.accept(Universe3090ModItems.EKRANCHIK.get());
				tabData.accept(Universe3090ModItems.MAGICIRONINGOT.get());
				tabData.accept(Universe3090ModItems.EXTRACTLBSOUL.get());
				tabData.accept(Universe3090ModItems.KOLBA.get());
				tabData.accept(Universe3090ModItems.EXTRACTGOLUBAYSOUL_BUCKET.get());
				tabData.accept(Universe3090ModItems.KLAVA.get());
				tabData.accept(Universe3090ModItems.ROTOR.get());
				tabData.accept(Universe3090ModItems.EXTRACTREDSOUL.get());
				tabData.accept(Universe3090ModItems.WHITESOUL_MYPLACE.get());
				tabData.accept(Universe3090ModItems.BLACKSOUL_MYPLACE.get());
				tabData.accept(Universe3090ModItems.AAA_GLASS.get());
				tabData.accept(Universe3090ModItems.IRONMASS_1.get());
				tabData.accept(Universe3090ModItems.IRONPLASTINEWOWA.get());
				tabData.accept(Universe3090ModItems.OSNOVAAAAGLASS.get());
				tabData.accept(Universe3090ModItems.BLACKIRONINGOT.get());
				tabData.accept(Universe3090ModItems.COPPERMASS_1.get());
				tabData.accept(Universe3090ModItems.WIRESPROVODA.get());
				tabData.accept(Universe3090ModItems.INSTRUMENTPROSTO.get());
				tabData.accept(Universe3090ModItems.ALPHAELEMENT.get());
				tabData.accept(Universe3090ModBlocks.DARKGRASS.get().asItem());
				tabData.accept(Universe3090ModBlocks.DARKGRASSBLOCKNIZ.get().asItem());
				tabData.accept(Universe3090ModBlocks.DARK_BIOME_ORE.get().asItem());
				tabData.accept(Universe3090ModBlocks.ALUMINIUM_ORE_WOWA.get().asItem());
				tabData.accept(Universe3090ModItems.ETHERIUMDARK.get());
				tabData.accept(Universe3090ModItems.BATAREYKA.get());
				tabData.accept(Universe3090ModItems.ZAGATOVKABLACKIRON.get());
				tabData.accept(Universe3090ModItems.POSUDINA.get());
				tabData.accept(Universe3090ModItems.KARMANNIELECTROLIZER.get());
				tabData.accept(Universe3090ModItems.MOLOTWOWA.get());
				tabData.accept(Universe3090ModItems.IZOLATIONW.get());
				tabData.accept(Universe3090ModItems.COPPERPLATE.get());
				tabData.accept(Universe3090ModItems.MULTITOOLCOMPONENT.get());
				tabData.accept(Universe3090ModBlocks.POGRANICHNIK.get().asItem());
				tabData.accept(Universe3090ModBlocks.DARK_STRK_BLK.get().asItem());
				tabData.accept(Universe3090ModItems.RED_EMERALD_PIECE.get());
				tabData.accept(Universe3090ModItems.BLUE_EMERALD_PIECE.get());
				tabData.accept(Universe3090ModItems.WHITE_EMERALD_PIECE.get());
				tabData.accept(Universe3090ModItems.YELLOW_EMERALD_PIECE.get());
				tabData.accept(Universe3090ModItems.LIGHT_BLUE_EMERALD_PIECE.get());
				tabData.accept(Universe3090ModItems.PURPLE_EMERALD_PIECE.get());
				tabData.accept(Universe3090ModItems.GREEN_EMERALD_PIECE.get());
				tabData.accept(Universe3090ModItems.ALUMINIUM_INGOT.get());
				tabData.accept(Universe3090ModItems.ALUMINIUM_PLATE.get());
				tabData.accept(Universe3090ModBlocks.BLOCKFORPROFESSIAFIX.get().asItem());
				tabData.accept(Universe3090ModBlocks.CRAFTERBRONI.get().asItem());
				tabData.accept(Universe3090ModBlocks.EMERALD_COMBINER.get().asItem());
				tabData.accept(Universe3090ModBlocks.BIG_BATTERYW.get().asItem());
				tabData.accept(Universe3090ModBlocks.REDSTONECOMMANDBLOCK.get().asItem());
				tabData.accept(Universe3090ModBlocks.MAGICIRONBLOCK.get().asItem());
				tabData.accept(Universe3090ModBlocks.PORTCONSTRUCTOR.get().asItem());
				tabData.accept(Universe3090ModBlocks.INTERFACECONSTRUCTOR.get().asItem());
				tabData.accept(Universe3090ModBlocks.REDSTONEPORT.get().asItem());
				tabData.accept(Universe3090ModBlocks.EXTRACTOR.get().asItem());
				tabData.accept(Universe3090ModBlocks.DARK_IRON_MACHINE_CASING.get().asItem());
				tabData.accept(Universe3090ModBlocks.SOLARPANELGENERATORW.get().asItem());
				tabData.accept(Universe3090ModItems.DARKIRONPLASTINE.get());
				tabData.accept(Universe3090ModItems.ALUMINIUMDIAMONDBASE.get());
				tabData.accept(Universe3090ModItems.ENERGYELEMENT.get());
				tabData.accept(Universe3090ModItems.MULTITOOLCOMPONENTNOTTRUE.get());
				tabData.accept(Universe3090ModItems.SOLARPANELELEMENT.get());
				tabData.accept(Universe3090ModItems.LASERGUNTEST.get());
				tabData.accept(Universe3090ModItems.CLEARETHERIUMLIGHT.get());
				tabData.accept(Universe3090ModItems.MIMIC_SPAWN_EGG.get());
				tabData.accept(Universe3090ModItems.RING.get());
				tabData.accept(Universe3090ModItems.GOLD_PLASTINE.get());
				tabData.accept(Universe3090ModBlocks.TESTPLANT.get().asItem());
				tabData.accept(Universe3090ModBlocks.CABLE_N.get().asItem());
				tabData.accept(Universe3090ModItems.DIAMOND_MULTI_TOOLW.get());
				tabData.accept(Universe3090ModItems.DIAMONDLAPIS.get());
				tabData.accept(Universe3090ModItems.REDDIAMONDMULTITOOLPLUSW.get());
				tabData.accept(Universe3090ModItems.REDDIAMONDLAPIS.get());
			})

					.build());
	public static final RegistryObject<CreativeModeTab> MYPLACEVKLADKA = REGISTRY.register("myplacevkladka",
			() -> CreativeModeTab.builder().title(Component.translatable("item_group.universe3090.myplacevkladka")).icon(() -> new ItemStack(Universe3090ModBlocks.TRUEROSE.get())).displayItems((parameters, tabData) -> {
				tabData.accept(Universe3090ModItems.ROSE.get());
				tabData.accept(Universe3090ModBlocks.CORRUPTED_SOULS_BLOCK.get().asItem());
				tabData.accept(Universe3090ModBlocks.BLACK_WALLS_MYPLACE.get().asItem());
				tabData.accept(Universe3090ModItems.MYPLACEZONE.get());
				tabData.accept(Universe3090ModBlocks.TRUEROSE.get().asItem());
				tabData.accept(Universe3090ModItems.DARK_HEAD_SPAWN_EGG.get());
			})

					.build());
}
