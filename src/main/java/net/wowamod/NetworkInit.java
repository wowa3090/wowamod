package net.wowamod;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

/**
 * Этот класс отвечает за инициализацию сетевых пакетов мода.
 * Он слушает события жизненного цикла Forge и вызывает метод регистрации сети.
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = Universe3090Mod.MODID)
public class NetworkInit {

    /**
     * Этот метод вызывается во время события FMLCommonSetupEvent.
     * Мы используем его для регистрации наших сетевых пакетов.
     * Аннотация @SubscribeEvent обязательна, а метод должен быть public static.
     * @param event Событие FMLCommonSetupEvent, предоставляемое Forge.
     */
    @SubscribeEvent
    public static void onCommonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // Вызываем статический метод регистрации из основного класса способности
            ElissClawsSuperAbility.registerNetwork();
            // Выводим сообщение в консоль для подтверждения, что регистрация прошла успешно
            System.out.println("Universe3090 Mod: Network channels successfully registered.");
        });
    }
}
