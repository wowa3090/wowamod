package net.wowamod.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.wowamod.inventory.EmitterMenu;
import net.wowamod.network.C2SUpdateEmitterPacket;
import net.wowamod.network.C2SDeleteWavePacket;
import net.wowamod.network.MWNetwork;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EmitterScreen extends AbstractContainerScreen<EmitterMenu> {

    public static List<String> cachedNetworks = new ArrayList<>();
    private final List<String> playerNetworks = new ArrayList<>();

    private int currentTab = 0; 
    private EditBox waveNameBox;
    private EditBox limitBox;
    
    private String localWaveName = "";
    private String localLimit = "10000";
    private boolean localModeOutput = true;
    
    private boolean needsReinit = false;
    private int tickCount = 0;
    private final String[] cmdLines = {
        "sys.route_power(fw_net);", "auth: UUID_VERIFIED", "wave_link.establish()...",
        "buffer_capacity: MAX", "pinging_remote_nodes...", "01001110 01100101",
        "WARN: high_voltage_detected", "bypass_relay(true);"
    };

    public EmitterScreen(EmitterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 175; // Сделал чуть выше для красоты
        
        if (menu.blockEntity != null) {
            this.localWaveName = menu.blockEntity.getActiveWaveName();
            this.localLimit = String.valueOf(menu.blockEntity.getTransferLimit());
            this.localModeOutput = menu.blockEntity.isModeOutput();
        }
        this.playerNetworks.addAll(cachedNetworks);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        this.tickCount++; 

        if (this.needsReinit) {
            this.needsReinit = false;
            if (this.currentTab == 1) this.init(); 
        }

        if (this.currentTab == 0 && this.waveNameBox != null && this.limitBox != null) {
            this.localWaveName = this.waveNameBox.getValue();
            this.localLimit = this.limitBox.getValue();
        }
    }

    @Override
    protected void init() {
        super.init();
        this.clearWidgets();
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // Используем наши кастомные кибер-кнопки
        CyberButton tab1Btn = new CyberButton(x, y - 22, 85, 20, Component.literal("Настройки"), b -> switchTab(0));
        tab1Btn.active = (currentTab != 0);
        this.addRenderableWidget(tab1Btn);

        CyberButton tab2Btn = new CyberButton(x + 89, y - 22, 85, 20, Component.literal("Мои сети"), b -> switchTab(1));
        tab2Btn.active = (currentTab != 1);
        this.addRenderableWidget(tab2Btn);

        if (currentTab == 0) initSettingsTab(x, y);
        else initManagerTab(x, y);
    }

    private void switchTab(int tabIndex) {
        this.currentTab = tabIndex;
        this.init(); 
    }

    private void initSettingsTab(int x, int y) {
        this.waveNameBox = new EditBox(this.font, x + 40, y + 70, 96, 16, Component.literal("Имя сети"));
        this.waveNameBox.setMaxLength(16);
        this.waveNameBox.setValue(this.localWaveName);
        this.waveNameBox.setBordered(false); // Отключаем стандартную рамку
        this.waveNameBox.setTextColor(0xFF00FF00); // Неоновый зеленый текст
        this.addRenderableWidget(this.waveNameBox);

        this.limitBox = new EditBox(this.font, x + 40, y + 95, 96, 16, Component.literal("Лимит FE"));
        this.limitBox.setMaxLength(7);
        this.limitBox.setValue(this.localLimit);
        this.limitBox.setBordered(false);
        this.limitBox.setTextColor(0xFF00FF00);
        this.addRenderableWidget(this.limitBox);

        CyberButton modeBtn = new CyberButton(x + 40, y + 118, 96, 20, Component.literal(localModeOutput ? "Режим: ОТДАЧА" : "Режим: ПРИЕМ"), button -> {
            this.localModeOutput = !this.localModeOutput;
            button.setMessage(Component.literal(localModeOutput ? "Режим: ОТДАЧА" : "Режим: ПРИЕМ"));
        });
        this.addRenderableWidget(modeBtn);

        CyberButton saveBtn = new CyberButton(x + 10, y + 145, 75, 20, Component.literal("Применить"), button -> saveAndSync());
        this.addRenderableWidget(saveBtn);

        CyberButton disconnectBtn = new CyberButton(x + 91, y + 145, 75, 20, Component.literal("Отключить"), button -> {
            this.localWaveName = "";
            if (this.waveNameBox != null) this.waveNameBox.setValue("");
            saveAndSync();
        });
        this.addRenderableWidget(disconnectBtn);
    }

    private void initManagerTab(int x, int y) {
        int listY = y + 20;
        for (String networkName : playerNetworks) {
            CyberButton netBtn = new CyberButton(x + 10, listY, 110, 20, Component.literal("Сеть: " + networkName), button -> {
                this.localWaveName = networkName;
                this.switchTab(0);
            });
            this.addRenderableWidget(netBtn);

            CyberButton delBtn = new CyberButton(x + 125, listY, 40, 20, Component.literal("Удал."), button -> {
                MWNetwork.sendToServer(new C2SDeleteWavePacket(networkName.trim()));
                if (this.localWaveName.trim().equals(networkName.trim())) {
                    this.localWaveName = "";
                }
            });
            this.addRenderableWidget(delBtn);

            listY += 24; 
        }
    }

    private void saveAndSync() {
        String wave = this.localWaveName.trim();
        int limit = 10000;
        try { limit = Integer.parseInt(this.localLimit.trim()); } catch (NumberFormatException ignored) {}

        MWNetwork.sendToServer(new C2SUpdateEmitterPacket(this.menu.pos, wave, this.localModeOutput, limit));
        
        if (this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.player.closeContainer();
        } else {
            this.onClose();
        }
    }

    public void updateNetworks(List<String> networks) {
        this.playerNetworks.clear();
        this.playerNetworks.addAll(networks);
        this.needsReinit = true; 
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        
        // --- 1. КИБЕР ФОН ---
        graphics.fill(x, y, x + this.imageWidth, y + this.imageHeight, 0xEE080808); // Темно-серый почти черный фон
        drawBorder(graphics, x, y, this.imageWidth, this.imageHeight, 0xFF00CC00); // Зеленая рамка
        graphics.fill(x, y + 16, x + this.imageWidth, y + 17, 0xFF00CC00); // Полоска-разделитель заголовка

        // --- 2. КАСТОМНЫЕ РАМКИ ДЛЯ ТЕКСТОВЫХ ПОЛЕЙ ---
        if (currentTab == 0) {
            // Рамки вокруг EditBox
            drawBorder(graphics, x + 38, y + 68, 100, 20, 0xFF00AA00);
            drawBorder(graphics, x + 38, y + 93, 100, 20, 0xFF00AA00);

            // Текстовые подсказки
            graphics.drawString(this.font, "NAME", x + 10, y + 74, 0xFF008800, false);
            graphics.drawString(this.font, "MAX", x + 12, y + 99, 0xFF008800, false);
            
            // --- 3. ТЕРМИНАЛ CMD ---
            int termX = x + 10;
            int termY = y + 20;
            int termW = 156;
            int termH = 42;

            graphics.fill(termX, termY, termX + termW, termY + termH, 0xFF000000);
            drawBorder(graphics, termX, termY, termW, termH, 0xFF00FF00);

            graphics.enableScissor(termX, termY, termX + termW, termY + termH);
            Random rand = new Random(this.menu.pos.asLong()); 
            for (int i = 0; i < 4; i++) {
                int scrollSpeed = 1 + rand.nextInt(2);
                int yOffset = termY + termH - ((this.tickCount * scrollSpeed + rand.nextInt(100)) % (termH + 20));
                int xOffset = termX + 2 + rand.nextInt(50);
                String line = cmdLines[rand.nextInt(cmdLines.length)];
                graphics.drawString(this.font, line, xOffset, yOffset, 0xFF004400, false);
            }

            String displayWave = this.localWaveName.trim();
            boolean displayMode = this.localModeOutput;
            
            if (displayWave.isEmpty()) {
                graphics.drawString(this.font, "> STATUS: OFFLINE", termX + 5, termY + 5, 0xFFFF3333, false);
                graphics.drawString(this.font, "> NO CONNECTION", termX + 5, termY + 15, 0xFFAAAAAA, false);
            } else {
                graphics.drawString(this.font, "> STATUS: CONNECTED", termX + 5, termY + 5, 0xFF55FF55, false);
                graphics.drawString(this.font, "> LINK: " + displayWave, termX + 5, termY + 18, 0xFF00FF00, false);
                graphics.drawString(this.font, "> MODE: " + (displayMode ? "OUTPUT" : "INPUT"), termX + 5, termY + 30, 0xFF00FF00, false);
            }
            graphics.disableScissor();
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        if (currentTab == 0) {
            graphics.drawString(this.font, "Модуль связи", 55, 5, 0xFF00FF00, false);
        } else {
            graphics.drawString(this.font, "База данных", 55, 5, 0xFF00FF00, false);
        }
    }

    // --- УТИЛИТА ДЛЯ ОТРИСОВКИ РАМОК ---
    public static void drawBorder(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + 1, color); // Верх
        g.fill(x, y + h - 1, x + w, y + h, color); // Низ
        g.fill(x, y + 1, x + 1, y + h - 1, color); // Лево
        g.fill(x + w - 1, y + 1, x + w, y + h - 1, color); // Право
    }

    // ==========================================
    // ВНУТРЕННИЙ КЛАСС: ПРОЦЕДУРНАЯ КИБЕР-КНОПКА
    // ==========================================
    private class CyberButton extends Button {
        public CyberButton(int x, int y, int width, int height, Component message, OnPress onPress) {
            super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            boolean hovered = this.isHoveredOrFocused();
            // Цвета фона
            int bgColor = hovered ? 0xAA004400 : 0xDD000000;
            // Цвета рамок (если неактивна - серая, если наведена - яркая)
            int borderColor = this.active ? (hovered ? 0xFF00FF00 : 0xFF008800) : 0xFF333333;
            // Цвет текста
            int textColor = this.active ? (hovered ? 0xFFFFFFFF : 0xFF00CC00) : 0xFF555555;

            // Рисуем фон кнопки
            graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, bgColor);
            // Рисуем границу кнопки через нашу утилиту
            drawBorder(graphics, this.getX(), this.getY(), this.width, this.height, borderColor);
            
            // Центрируем текст внутри кнопки
            int textWidth = font.width(this.getMessage());
            graphics.drawString(font, this.getMessage(), this.getX() + (this.width - textWidth) / 2, this.getY() + (this.height - 8) / 2, textColor, false);
        }
    }
}