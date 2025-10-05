package net.wowamod.procedures;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.wowamod.Universe3090Mod; // Импортируем основной класс мода
import net.wowamod.procedures.SoulSystemWProcedure.ISoulCapability;
import net.wowamod.procedures.SoulSystemWProcedure.SoulType;
import org.lwjgl.glfw.GLFW;
import com.mojang.blaze3d.platform.InputConstants; // Импорт для InputConstants.Key

// Класс, отвечающий за открытие и отрисовку GUI
// Добавляем аннотацию для подписки на события Forge на клиенте
@Mod.EventBusSubscriber(modid = Universe3090Mod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class SoulSystemWGUIProcedure {

    // Создаём экземпляр KeyMapping как статическую переменную
    // Используем правильную сигнатуру конструктора для 1.20.1
    public static final KeyMapping OPEN_SOUL_GUI_KEY = new KeyMapping(
        "key.universe3090.open_soul_gui", // Текстовый ключ для локализации
        KeyConflictContext.IN_GAME,        // Контекст: в игре
        KeyModifier.NONE,                  // Модификатор (Ctrl, Shift и т.д.)
        InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_G), // Ключ: конвертируем GLFW код в InputConstants.Key
        "key.categories.wowamod"              // Категория (например, "key.categories.misc", "key.categories.movement", "key.categories.gameplay", "key.categories.ui")
    );

    // Внутренний класс, наследующий Screen, для отображения информации о душе
    public static class SoulScreen extends Screen {
        private final Player player;
        private final ISoulCapability soulCapability;

        // Конструктор экрана
        public SoulScreen(Player player, ISoulCapability capability) {
            super(Component.literal("Soul Status"));
            this.player = player;
            this.soulCapability = capability;
        }

        // Метод, вызываемый для отрисовки содержимого экрана
        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            // Рендерим стандартный фон
            super.render(guiGraphics, mouseX, mouseY, partialTick);

            Font font = Minecraft.getInstance().font;
            int textY = 20; // Начальная позиция по Y для текста
            int textX = 10; // Начальная позиция по X для текста

            // Заголовок
            guiGraphics.drawCenteredString(font, this.title, this.width / 2, textY, 0xFFFFFF);
            textY += 20;

            // Отображаем имя игрока
            guiGraphics.drawString(font, "Player: " + player.getName().getString(), textX, textY, 0xFFFFFF);
            textY += 12;

            // Отображаем текущую душу, если она определена
            if (soulCapability.isSoulDetermined()) {
                guiGraphics.drawString(font, "Current Soul: " + soulCapability.getCurrentSoul().name(), textX, textY, 0x00FF00); // Зелёный цвет для определённой души
            } else {
                guiGraphics.drawString(font, "Current Soul: Undetermined", textX, textY, 0xFFFF00); // Жёлтый цвет для неопределённой
            }
            textY += 12;

            // Отображаем значения переменных души
            guiGraphics.drawString(font, "Determination: " + soulCapability.getSoulValue(SoulType.DETERMINATION), textX, textY, 0xFFFFFF);
            textY += 12;
            guiGraphics.drawString(font, "Bravery: " + soulCapability.getSoulValue(SoulType.BRAVERY), textX, textY, 0xFFFFFF);
            textY += 12;
            guiGraphics.drawString(font, "Justice: " + soulCapability.getSoulValue(SoulType.JUSTICE), textX, textY, 0xFFFFFF);
            textY += 12;
            guiGraphics.drawString(font, "Kindness: " + soulCapability.getSoulValue(SoulType.KINDNESS), textX, textY, 0xFFFFFF);
            textY += 12;
            guiGraphics.drawString(font, "Patience: " + soulCapability.getSoulValue(SoulType.PATIENCE), textX, textY, 0xFFFFFF);
            textY += 12;
            guiGraphics.drawString(font, "Integrity: " + soulCapability.getSoulValue(SoulType.INTEGRITY), textX, textY, 0xFFFFFF);
            textY += 12;
            guiGraphics.drawString(font, "Perseverance: " + soulCapability.getSoulValue(SoulType.PERSEVERANCE), textX, textY, 0xFFFFFF);
            textY += 20;

            // Подсказка
            guiGraphics.drawCenteredString(font, "Press ESC to close", this.width / 2, this.height - 20, 0xAAAAAA);
        }

        // Метод, вызываемый при нажатии клавиш
        @Override
        public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
            // Закрываем экран при нажатии ESC или 'E'
            if (p_keyPressed_1_ == 256 || p_keyPressed_1_ == 69) { // 256 = ESC, 69 = E
                this.onClose();
                return true;
            }
            return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
        }

        // Метод, вызываемый при закрытии экрана
        @Override
        public void onClose() {
            super.onClose();
        }
    }

    // Метод для регистрации KeyMapping
    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        // Регистрируем KeyMapping, чтобы она появилась в Controls
        event.register(OPEN_SOUL_GUI_KEY);
    }

    // Подкласс для обработки событий Forge Bus (нажатия клавиш)
    @Mod.EventBusSubscriber(modid = Universe3090Mod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ForgeEvents {
        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
             // Проверяем, была ли нажата наша клавиша
             // consumeClick() сбрасывает состояние и возвращает true, если клавиша была нажата
             if (SoulSystemWGUIProcedure.OPEN_SOUL_GUI_KEY.consumeClick()) { // Обратите внимание на полный путь к переменной
                 openSoulGUI();
             }
        }
    }

    // Метод, вызываемый при нажатии кнопки
    private static void openSoulGUI() {
        // Проверяем, что мы на клиенте и есть игрок
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.level != null) {
            // Вызываем вашу процедуру открытия GUI, передав игрока
            execute(mc.player);
        }
    }

    // Основной метод для открытия GUI
    public static void execute(Player player) {
        if (player != null && player.level().isClientSide()) { // Убедимся, что игрок существует и код выполняется на клиенте
            // Получаем Capability игрока
            SoulSystemWProcedure.getCapability(player).ifPresent(cap -> {
                // Создаём и открываем экран
                Minecraft.getInstance().setScreen(new SoulScreen(player, cap));
            });
        } else {
            System.out.println("Error: Player is null or not on client side.");
        }
    }
}