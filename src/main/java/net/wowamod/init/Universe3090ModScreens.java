
/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.wowamod.init;

import net.wowamod.client.gui.WowabronyacraftringScreen;
import net.wowamod.client.gui.PortConstructoraScreen;
import net.wowamod.client.gui.GuidebookstartScreen;
import net.wowamod.client.gui.GuidebookScreen;
import net.wowamod.client.gui.ExtractorintefaceScreen;
import net.wowamod.client.gui.ECIScreen;
import net.wowamod.client.gui.ConstructorinterfaceScreen;

import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.client.gui.screens.MenuScreens;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class Universe3090ModScreens {
	@SubscribeEvent
	public static void clientLoad(FMLClientSetupEvent event) {
		event.enqueueWork(() -> {
			MenuScreens.register(Universe3090ModMenus.WOWABRONYACRAFTRING.get(), WowabronyacraftringScreen::new);
			MenuScreens.register(Universe3090ModMenus.CONSTRUCTORINTERFACE.get(), ConstructorinterfaceScreen::new);
			MenuScreens.register(Universe3090ModMenus.PORT_CONSTRUCTORA.get(), PortConstructoraScreen::new);
			MenuScreens.register(Universe3090ModMenus.EXTRACTORINTEFACE.get(), ExtractorintefaceScreen::new);
			MenuScreens.register(Universe3090ModMenus.GUIDEBOOK.get(), GuidebookScreen::new);
			MenuScreens.register(Universe3090ModMenus.GUIDEBOOKSTART.get(), GuidebookstartScreen::new);
			MenuScreens.register(Universe3090ModMenus.ECI.get(), ECIScreen::new);
		});
	}
}
