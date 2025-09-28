package net.wowamod.client.gui;

import net.wowamod.world.inventory.ExtractorGuideMenu;
import net.wowamod.network.ExtractorGuideButtonMessage;
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

public class ExtractorGuideScreen extends AbstractContainerScreen<ExtractorGuideMenu> {
	private final static HashMap<String, Object> guistate = ExtractorGuideMenu.guistate;
	private final Level world;
	private final int x, y, z;
	private final Player entity;
	Button button_nazad;

	public ExtractorGuideScreen(ExtractorGuideMenu container, Inventory inventory, Component text) {
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

		guiGraphics.blit(new ResourceLocation("universe3090:textures/screens/extractorinteface.png"), this.leftPos + 0, this.topPos + -1, 0, 0, 176, 166, 176, 166);

		guiGraphics.blit(new ResourceLocation("universe3090:textures/screens/soul_patience.png"), this.leftPos + 44, this.topPos + 34, 0, 0, 16, 16, 16, 16);

		guiGraphics.blit(new ResourceLocation("universe3090:textures/screens/kolba.png"), this.leftPos + 80, this.topPos + 56, 0, 0, 16, 16, 16, 16);

		guiGraphics.blit(new ResourceLocation("universe3090:textures/screens/kolba2.png"), this.leftPos + 116, this.topPos + 32, 0, 0, 16, 16, 16, 16);

		guiGraphics.blit(new ResourceLocation("universe3090:textures/screens/soul.png"), this.leftPos + 25, this.topPos + 33, 0, 0, 16, 16, 16, 16);

		guiGraphics.blit(new ResourceLocation("universe3090:textures/screens/redsoulkolba11.png"), this.leftPos + 134, this.topPos + 32, 0, 0, 16, 16, 16, 16);

		guiGraphics.blit(new ResourceLocation("universe3090:textures/screens/extractorimage.png"), this.leftPos + 25, this.topPos + -2, 0, 0, 128, 32, 128, 32);

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
		guiGraphics.drawString(this.font, Component.translatable("gui.universe3090.extractor_guide.label_empty"), 86, 37, -12829636, false);
	}

	@Override
	public void onClose() {
		super.onClose();
	}

	@Override
	public void init() {
		super.init();
		button_nazad = Button.builder(Component.translatable("gui.universe3090.extractor_guide.button_nazad"), e -> {
			if (true) {
				Universe3090Mod.PACKET_HANDLER.sendToServer(new ExtractorGuideButtonMessage(0, x, y, z));
				ExtractorGuideButtonMessage.handleButtonAction(entity, 0, x, y, z);
			}
		}).bounds(this.leftPos + 64, this.topPos + -17, 51, 20).build();
		guistate.put("button:button_nazad", button_nazad);
		this.addRenderableWidget(button_nazad);
	}
}
