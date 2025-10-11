// NetworkInit.java
package net.wowamod;

import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.wowamod.ElissClawsSuperAbility; // Импортируем класс, содержащий логику сети

// Подписываемся на MOD EventBus с указанием modid основного мода
// Это позволяет этому классу получать события жизненного цикла от Forge для указанного мода.
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = Universe3090Mod.MODID)
public class NetworkSuperAbility {

    // Статический метод, который будет вызван Forge при событии FMLCommonSetupEvent
    // Это стандартное место для инициализации сетевых каналов и других вещей, требующих Registry.
    private static void init(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // Вызываем метод регистрации сети из ElissClawsSuperAbility
            ElissClawsSuperAbility.registerNetwork();
            System.out.println("ElissClaws Network initialized via NetworkInit.java");
        });
    }
}