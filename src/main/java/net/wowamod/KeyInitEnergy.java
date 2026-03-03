package net.wowamod.client;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public class KeyInitEnergy {
    public static KeyMapping ENERGY_KINESIS_KEY;

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        ENERGY_KINESIS_KEY = new KeyMapping(
                "key.wowamod.energykinesis",
                GLFW.GLFW_KEY_V,
                "key.categories.wowamod"
        );
        event.register(ENERGY_KINESIS_KEY);
    }
}