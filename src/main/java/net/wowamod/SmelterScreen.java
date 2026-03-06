package net.wowamod;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class SmelterScreen extends AbstractContainerScreen<SmelterMenu> {
    // ВАЖНО: Убедись, что картинка smelter_gui.png существует по этому пути в ресурсах мода!
    private static final ResourceLocation TEXTURE = new ResourceLocation("universe3090", "textures/gui/smelter_gui.png");

    public SmelterScreen(SmelterMenu menu, Inventory inv, Component title) {
        // Принудительно задаем заголовок "Concept"
        super(menu, inv, Component.literal("Concept"));
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = 8;
        this.titleLabelY = 5;
        this.inventoryLabelY = 74;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics); // Темный фон за GUI
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // 1. Рисуем основную текстуру фона
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, 176, 166);

        // 2. Рендер заполненной стрелки прогресса
        int progress = this.menu.getScaledProgress();
        if (progress > 0) {
            // Предполагается, что заполненная стрелка на текстуре находится по координатам X=176, Y=0
            graphics.blit(TEXTURE, x + 76, y + 20, 176, 0, progress, 12);
        }

        // 3. Рендер 10 декоративных иконок сверху (горизонтальный ряд)
        /*
        for (int i = 0; i < 10; i++) {
            // Рисуем маленькую иконку 8x8 (координаты на текстуре: 176, 16)
            int iconX = x + 40 + (i * 10);
            int iconY = y + 5;
            graphics.blit(TEXTURE, iconX, iconY, 176, 16, 8, 8);
        }
        
        // 4. Рендер графической полоски энергии
       /* int energyStr = (int) ((this.menu.getEnergy() / 400000.0f) * 50); // 50 пикселей максимальная высота
        if (energyStr > 0) {
            // Предполагается, что полоска энергии на текстуре по координатам X=176, Y=24...
            graphics.blit(TEXTURE, x + 8, y + 70 - energyStr, 176, 74 - energyStr, 12, energyStr);
        }
        */
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // Отрисовка названия "Concept" в верхнем левом углу (белым цветом для темной темы)
        // graphics.drawString(this.font, "Concept", this.titleLabelX, this.titleLabelY, 0xFFFFFF, false);
        
        // ПОКАЗЫВАЕМ ЭНЕРГИЮ ТЕКСТОМ (Красным цветом, правее)
        int energy = this.menu.getEnergy();
        graphics.drawString(this.font, "FE: " + energy, 120, 12, 0x000000, false);
        
        // ПОКАЗЫВАЕМ ПРОЦЕНТЫ КРАФТА (Зеленым цветом, над стрелкой)
        int maxProgress = this.menu.data.get(1);
        int currentProgress = this.menu.data.get(0);
        if (maxProgress > 0) {
            int percent = (currentProgress * 100) / maxProgress;
            graphics.drawString(this.font, percent + "%", 76, 30, 0x00AA00, false);
        }

        // Отрисовка надписи "Inventory" (Инвентарь игрока)
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
    }
}