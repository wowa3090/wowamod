package net.wowamod.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.wowamod.Universe3090Mod; // Убедитесь, что это ваш главный класс мода

// Эта аннотация автоматически подписывает класс на события MOD-шины.
// Теперь не нужно ничего вызывать из главного класса мода.
@Mod.EventBusSubscriber(modid = Universe3090Mod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Universe3090Mod.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    /**
     * Этот метод будет автоматически вызван Forge во время инициализации мода
     * благодаря аннотациям @Mod.EventBusSubscriber и @SubscribeEvent.
     */
    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        // Мы используем event.enqueueWork, чтобы безопасно выполнить регистрацию в основном потоке.
        event.enqueueWork(PacketHandler::register);
    }

    // Сама логика регистрации пакетов остается здесь.
    // Если вы добавите новые пакеты, регистрируйте их тут.
    public static void register() {
        int id = 0;
        INSTANCE.registerMessage(id++,
                DealLaserDamagePacket.class,
                DealLaserDamagePacket::encode,
                DealLaserDamagePacket::decode,
                DealLaserDamagePacket::handle
        );
        // Пример: INSTANCE.registerMessage(id++, AnotherPacket.class, ...);
    }
}

