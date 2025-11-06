package net.wowamod.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
// import net.wowamod.LaserRayMTW; // Удалено
import net.wowamod.entity.ModEntityBeamOrbital; // ИСПРАВЛЕНО: Указываем на правильный класс

// ИСПРАВЛЕНО: Возвращаем правильную шину Bus.MOD и MODID
@Mod.EventBusSubscriber(modid = "universe3090", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetupOrbital {
  @SubscribeEvent
  public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
    // ИСПРАВЛЕНО: Указываем на правильный класс
    event.registerEntityRenderer(ModEntityBeamOrbital.ORBITAL_BEAM.get(), OrbitalBeamRenderer::new);
  }
}

