package net.wowamod.handlers;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.wowamod.capability.EnergyCapability;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.minecraft.core.Direction;

@Mod.EventBusSubscriber(modid = "universe3090")
public class CapabilityAttacher {

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            if (!event.getObject().getCapability(EnergyCapability.ENERGY_CAPABILITY).isPresent()) {
                event.addCapability(new ResourceLocation("universe3090", "energy_cap"), new ICapabilityProvider() {
                    private final EnergyCapability backend = new EnergyCapability();
                    private final LazyOptional<EnergyCapability> optional = LazyOptional.of(() -> backend);

                    @Override
                    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
                        return EnergyCapability.ENERGY_CAPABILITY.orEmpty(cap, optional);
                    }
                });
            }
        }
    }
}