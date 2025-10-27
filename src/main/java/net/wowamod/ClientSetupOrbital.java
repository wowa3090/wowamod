package net.wowamod.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.wowamod.entity.ModEntityBeamOrbital; // <-- Добавить этот импорт

// ВАЖНО: Переключаем на Forge-Bus, чтобы избежать NPE
@Mod.EventBusSubscriber(modid = "universe3090", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE) // ИСПРАВЛЕНО
public class ClientSetupOrbital {
  @SubscribeEvent
  // Используем RegisterRenderers, чтобы привязать рендерер к EntityType
  public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
    // ВАЖНО: ORBITAL_BEAM берем из ModEntityBeamOrbital, если нет класса ModEntities
    event.registerEntityRenderer(ModEntityBeamOrbital.ORBITAL_BEAM.get(), OrbitalBeamRenderer::new); // ИСПРАВЛЕНО (предполагая, что ModEntities - это ModEntityBeamOrbital)
  }
}