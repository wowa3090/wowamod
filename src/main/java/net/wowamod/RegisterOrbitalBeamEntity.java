package net.wowamod.init;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.wowamod.entity.ModEntityBeamOrbital;

@Mod.EventBusSubscriber(modid = "universe3090", bus = Mod.EventBusSubscriber.Bus.MOD)
public class RegisterOrbitalBeamEntity {

    static {
        // Получаем шину событий мода
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Вызываем регистрацию для вашего DeferredRegister из ModEntityBeamOrbital
        ModEntityBeamOrbital.register(modEventBus);
    }
}
