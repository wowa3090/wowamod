
/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.wowamod.init;

import net.wowamod.world.inventory.WowabronyacraftringMenu;
import net.wowamod.world.inventory.SolarpanelmenuMenu;
import net.wowamod.world.inventory.PortConstructoraMenu;
import net.wowamod.world.inventory.GuidebookstartMenu;
import net.wowamod.world.inventory.GuidebookMenu;
import net.wowamod.world.inventory.ExtractorintefaceMenu;
import net.wowamod.world.inventory.ExtractorGuideMenu;
import net.wowamod.world.inventory.ECIMenu;
import net.wowamod.world.inventory.ConstructorinterfaceMenu;
import net.wowamod.world.inventory.AbobaMenu;
import net.wowamod.Universe3090Mod;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.common.extensions.IForgeMenuType;

import net.minecraft.world.inventory.MenuType;

public class Universe3090ModMenus {
	public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.MENU_TYPES, Universe3090Mod.MODID);
	public static final RegistryObject<MenuType<WowabronyacraftringMenu>> WOWABRONYACRAFTRING = REGISTRY.register("wowabronyacraftring", () -> IForgeMenuType.create(WowabronyacraftringMenu::new));
	public static final RegistryObject<MenuType<ConstructorinterfaceMenu>> CONSTRUCTORINTERFACE = REGISTRY.register("constructorinterface", () -> IForgeMenuType.create(ConstructorinterfaceMenu::new));
	public static final RegistryObject<MenuType<PortConstructoraMenu>> PORT_CONSTRUCTORA = REGISTRY.register("port_constructora", () -> IForgeMenuType.create(PortConstructoraMenu::new));
	public static final RegistryObject<MenuType<ExtractorintefaceMenu>> EXTRACTORINTEFACE = REGISTRY.register("extractorinteface", () -> IForgeMenuType.create(ExtractorintefaceMenu::new));
	public static final RegistryObject<MenuType<GuidebookMenu>> GUIDEBOOK = REGISTRY.register("guidebook", () -> IForgeMenuType.create(GuidebookMenu::new));
	public static final RegistryObject<MenuType<GuidebookstartMenu>> GUIDEBOOKSTART = REGISTRY.register("guidebookstart", () -> IForgeMenuType.create(GuidebookstartMenu::new));
	public static final RegistryObject<MenuType<ECIMenu>> ECI = REGISTRY.register("eci", () -> IForgeMenuType.create(ECIMenu::new));
	public static final RegistryObject<MenuType<SolarpanelmenuMenu>> SOLARPANELMENU = REGISTRY.register("solarpanelmenu", () -> IForgeMenuType.create(SolarpanelmenuMenu::new));
	public static final RegistryObject<MenuType<ExtractorGuideMenu>> EXTRACTOR_GUIDE = REGISTRY.register("extractor_guide", () -> IForgeMenuType.create(ExtractorGuideMenu::new));
	public static final RegistryObject<MenuType<AbobaMenu>> ABOBA = REGISTRY.register("aboba", () -> IForgeMenuType.create(AbobaMenu::new));
}
