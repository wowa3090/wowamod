package net.wowamod.client.gui;

import net.wowamod.world.inventory.GuidebookstartMenu;
import net.wowamod.network.GuidebookstartButtonMessage;
import net.wowamod.Universe3090Mod;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.GuiGraphics;

import java.util.HashMap;

import com.mojang.blaze3d.systems.RenderSystem;

public class GuidebookstartScreen extends AbstractContainerScreen<GuidebookstartMenu> {
	private final static HashMap<String, Object> guistate = GuidebookstartMenu.guistate;
	private final Level world;
	private final int x, y, z;
	private final Player entity;
	Button button_nazad;

	public GuidebookstartScreen(GuidebookstartMenu container, Inventory inventory, Component text) {
		super(container, inventory, text);
		this.world = container.world;
		this.x = container.x;
		this.y = container.y;
		this.z = container.z;
		this.entity = container.entity;
		this.imageWidth = 176;
		this.imageHeight = 166;
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(guiGraphics);
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
		this.renderTooltip(guiGraphics, mouseX, mouseY);
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int gx, int gy) {
		RenderSystem.setShaderColor(1, 1, 1, 1);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		guiGraphics.blit(new ResourceLocation("universe3090:textures/screens/soul.png"), this.leftPos + 62, this.topPos + 19, 0, 0, 16, 16, 16, 16);

		guiGraphics.blit(new ResourceLocation("universe3090:textures/screens/blueemerald.png"), this.leftPos + 62, this.topPos + 37, 0, 0, 16, 16, 16, 16);

		RenderSystem.disableBlend();
	}

	@Override
	public boolean keyPressed(int key, int b, int c) {
		if (key == 256) {
			this.minecraft.player.closeContainer();
			return true;
		}
		return super.keyPressed(key, b, c);
	}

	@Override
	public void containerTick() {
		super.containerTick();
	}

	@Override
	protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		guiGraphics.drawString(this.font, Component.translatable("gui.universe3090.guidebookstart.label_dlia_nachala_vam_ponadobitsia_nait"), -59, 3, -1, false);
		guiGraphics.drawString(this.font, Component.translatable("gui.universe3090.guidebookstart.label_nachalo"), 71, -12, -1, false);
		guiGraphics.drawString(this.font, Component.translatable("gui.universe3090.guidebookstart.label_dush"), 80, 22, -1, false);
		guiGraphics.drawString(this.font, Component.translatable("gui.universe3090.guidebookstart.label_izumrudov"), 80, 40, -1, false);
		guiGraphics.drawString(this.font, Component.translatable("gui.universe3090.guidebookstart.label_kozhi_mimika"), 59, 58, -1, false);
		guiGraphics.drawString(this.font, Component.translatable("gui.universe3090.guidebookstart.label_zhielatielno_zapastis_miediu_i_zhiel"), -6, 70, -1, false);
	}

	@Override
	public void onClose() {
		super.onClose();
	}

	@Override
	public void init() {
		super.init();
		button_nazad = Button.builder(Component.translatable("gui.universe3090.guidebookstart.button_nazad"), e -> {
			if (true) {
				Universe3090Mod.PACKET_HANDLER.sendToServer(new GuidebookstartButtonMessage(0, x, y, z));
				GuidebookstartButtonMessage.handleButtonAction(entity, 0, x, y, z);
			}
		}).bounds(this.leftPos + 60, this.topPos + 88, 51, 20).build();
		guistate.put("button:button_nazad", button_nazad);
		this.addRenderableWidget(button_nazad);
	}
}
