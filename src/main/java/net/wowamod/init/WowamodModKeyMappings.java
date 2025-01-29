
/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.wowamod.init;

import org.lwjgl.glfw.GLFW;

import net.wowamod.network.VkeyabilityMessage;
import net.wowamod.network.TeleportMessage;
import net.wowamod.network.SoulsViewbuttonMessage;
import net.wowamod.WowamodMod;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = {Dist.CLIENT})
public class WowamodModKeyMappings {
	public static final KeyMapping TELEPORT = new KeyMapping("key.wowamod.teleport", GLFW.GLFW_KEY_R, "key.categories.wowamod") {
		private boolean isDownOld = false;

		@Override
		public void setDown(boolean isDown) {
			super.setDown(isDown);
			if (isDownOld != isDown && isDown) {
				WowamodMod.PACKET_HANDLER.sendToServer(new TeleportMessage(0, 0));
				TeleportMessage.pressAction(Minecraft.getInstance().player, 0, 0);
			}
			isDownOld = isDown;
		}
	};
	public static final KeyMapping SOULS_VIEWBUTTON = new KeyMapping("key.wowamod.souls_viewbutton", GLFW.GLFW_KEY_N, "key.categories.wowamod") {
		private boolean isDownOld = false;

		@Override
		public void setDown(boolean isDown) {
			super.setDown(isDown);
			if (isDownOld != isDown && isDown) {
				WowamodMod.PACKET_HANDLER.sendToServer(new SoulsViewbuttonMessage(0, 0));
				SoulsViewbuttonMessage.pressAction(Minecraft.getInstance().player, 0, 0);
			}
			isDownOld = isDown;
		}
	};
	public static final KeyMapping VKEYABILITY = new KeyMapping("key.wowamod.vkeyability", GLFW.GLFW_KEY_V, "key.categories.wowamod") {
		private boolean isDownOld = false;

		@Override
		public void setDown(boolean isDown) {
			super.setDown(isDown);
			if (isDownOld != isDown && isDown) {
				WowamodMod.PACKET_HANDLER.sendToServer(new VkeyabilityMessage(0, 0));
				VkeyabilityMessage.pressAction(Minecraft.getInstance().player, 0, 0);
			}
			isDownOld = isDown;
		}
	};

	@SubscribeEvent
	public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
		event.register(TELEPORT);
		event.register(SOULS_VIEWBUTTON);
		event.register(VKEYABILITY);
	}

	@Mod.EventBusSubscriber({Dist.CLIENT})
	public static class KeyEventListener {
		@SubscribeEvent
		public static void onClientTick(TickEvent.ClientTickEvent event) {
			if (Minecraft.getInstance().screen == null) {
				TELEPORT.consumeClick();
				SOULS_VIEWBUTTON.consumeClick();
				VKEYABILITY.consumeClick();
			}
		}
	}
}
