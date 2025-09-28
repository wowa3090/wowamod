package net.wowamod.client.gui;

import net.wowamod.world.inventory.ExtractorintefaceMenu;
import net.wowamod.procedures.EciprocProcedure;
import net.wowamod.procedures.CraftprogressProcedure;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.GuiGraphics;

import java.util.HashMap;

import com.mojang.blaze3d.systems.RenderSystem;

public class ExtractorintefaceScreen extends AbstractContainerScreen<ExtractorintefaceMenu> {
	private final static HashMap<String, Object> guistate = ExtractorintefaceMenu.guistate;
	private final Level world;
	private final int x, y, z;
	private final Player entity;

	public ExtractorintefaceScreen(ExtractorintefaceMenu container, Inventory inventory, Component text) {
		super(container, inventory, text);
		this.world = container.world;
		this.x = container.x;
		this.y = container.y;
		this.z = container.z;
		this.entity = container.entity;
		this.imageWidth = 176;
		this.imageHeight = 166;
	}

	private static final ResourceLocation texture = new ResourceLocation("universe3090:textures/screens/extractorinteface.png");

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
		guiGraphics.blit(texture, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);

		guiGraphics.blit(new ResourceLocation("universe3090:textures/screens/kolba.png"), this.leftPos + 100, this.topPos + 62, 0, 0, 8, 8, 8, 8);

		guiGraphics.blit(new ResourceLocation("universe3090:textures/screens/kolba2.png"), this.leftPos + 137, this.topPos + 37, 0, 0, 8, 8, 8, 8);

		guiGraphics.blit(new ResourceLocation("universe3090:textures/screens/soul_patience.png"), this.leftPos + 47, this.topPos + 24, 0, 0, 8, 8, 8, 8);

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
		guiGraphics.drawString(this.font, Component.translatable("gui.universe3090.extractorinteface.label_centrifuge"), 62, 10, -12829636, false);
		guiGraphics.drawString(this.font,

				CraftprogressProcedure.execute(world, x, y, z), 81, 38, -12829636, false);
		guiGraphics.drawString(this.font,

				EciprocProcedure.execute(world, x, y, z), -81, 4, -3289651, false);
	}

	@Override
	public void onClose() {
		super.onClose();
	}

	@Override
	public void init() {
		super.init();
	}
}
