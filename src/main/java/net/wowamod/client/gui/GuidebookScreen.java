package net.wowamod.client.gui;

import net.wowamod.world.inventory.GuidebookMenu;
import net.wowamod.network.GuidebookButtonMessage;
import net.wowamod.Universe3090Mod;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.GuiGraphics;

import java.util.HashMap;

import com.mojang.blaze3d.systems.RenderSystem;

public class GuidebookScreen extends AbstractContainerScreen<GuidebookMenu> {
	private final static HashMap<String, Object> guistate = GuidebookMenu.guistate;
	private final Level world;
	private final int x, y, z;
	private final Player entity;
	ImageButton imagebutton_soul;
	ImageButton imagebutton_blueemerald;
	ImageButton imagebutton_netherite_sword;
	ImageButton imagebutton_kolba;
	ImageButton imagebutton_magicironingot3;
	ImageButton imagebutton_guidebook_image;

	public GuidebookScreen(GuidebookMenu container, Inventory inventory, Component text) {
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
		guiGraphics.drawString(this.font, Component.translatable("gui.universe3090.guidebook.label_dushi"), -93, -21, -1, false);
		guiGraphics.drawString(this.font, Component.translatable("gui.universe3090.guidebook.label_izumrudy"), -93, 3, -1, false);
		guiGraphics.drawString(this.font, Component.translatable("gui.universe3090.guidebook.label_nachalo"), -93, 26, -1, false);
		guiGraphics.drawString(this.font, Component.translatable("gui.universe3090.guidebook.label_ekstraktor"), -94, 48, -1, false);
		guiGraphics.drawString(this.font, Component.translatable("gui.universe3090.guidebook.label_krafty"), -94, 70, -1, false);
		guiGraphics.drawString(this.font, Component.translatable("gui.universe3090.guidebook.label_multiblok"), -69, 101, -1, false);
	}

	@Override
	public void onClose() {
		super.onClose();
	}

	@Override
	public void init() {
		super.init();
		imagebutton_soul = new ImageButton(this.leftPos + -111, this.topPos + -24, 16, 16, 0, 0, 16, new ResourceLocation("universe3090:textures/screens/atlas/imagebutton_soul.png"), 16, 32, e -> {
		});
		guistate.put("button:imagebutton_soul", imagebutton_soul);
		this.addRenderableWidget(imagebutton_soul);
		imagebutton_blueemerald = new ImageButton(this.leftPos + -111, this.topPos + -1, 16, 16, 0, 0, 16, new ResourceLocation("universe3090:textures/screens/atlas/imagebutton_blueemerald.png"), 16, 32, e -> {
		});
		guistate.put("button:imagebutton_blueemerald", imagebutton_blueemerald);
		this.addRenderableWidget(imagebutton_blueemerald);
		imagebutton_netherite_sword = new ImageButton(this.leftPos + -111, this.topPos + 22, 16, 16, 0, 0, 16, new ResourceLocation("universe3090:textures/screens/atlas/imagebutton_netherite_sword.png"), 16, 32, e -> {
			if (true) {
				Universe3090Mod.PACKET_HANDLER.sendToServer(new GuidebookButtonMessage(2, x, y, z));
				GuidebookButtonMessage.handleButtonAction(entity, 2, x, y, z);
			}
		});
		guistate.put("button:imagebutton_netherite_sword", imagebutton_netherite_sword);
		this.addRenderableWidget(imagebutton_netherite_sword);
		imagebutton_kolba = new ImageButton(this.leftPos + -112, this.topPos + 44, 16, 16, 0, 0, 16, new ResourceLocation("universe3090:textures/screens/atlas/imagebutton_kolba.png"), 16, 32, e -> {
			if (true) {
				Universe3090Mod.PACKET_HANDLER.sendToServer(new GuidebookButtonMessage(3, x, y, z));
				GuidebookButtonMessage.handleButtonAction(entity, 3, x, y, z);
			}
		});
		guistate.put("button:imagebutton_kolba", imagebutton_kolba);
		this.addRenderableWidget(imagebutton_kolba);
		imagebutton_magicironingot3 = new ImageButton(this.leftPos + -111, this.topPos + 65, 16, 16, 0, 0, 16, new ResourceLocation("universe3090:textures/screens/atlas/imagebutton_magicironingot3.png"), 16, 32, e -> {
		});
		guistate.put("button:imagebutton_magicironingot3", imagebutton_magicironingot3);
		this.addRenderableWidget(imagebutton_magicironingot3);
		imagebutton_guidebook_image = new ImageButton(this.leftPos + -110, this.topPos + 91, 32, 32, 0, 0, 32, new ResourceLocation("universe3090:textures/screens/atlas/imagebutton_guidebook_image.png"), 32, 64, e -> {
		});
		guistate.put("button:imagebutton_guidebook_image", imagebutton_guidebook_image);
		this.addRenderableWidget(imagebutton_guidebook_image);
	}
}
