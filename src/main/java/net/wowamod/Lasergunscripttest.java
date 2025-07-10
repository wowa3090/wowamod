package net.wowamod;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class Lasergunscripttest {
    public Lasergunscripttest() {
    }

    @SubscribeEvent
    public static void init(FMLCommonSetupEvent event) {
        // Инициализация
    }
}