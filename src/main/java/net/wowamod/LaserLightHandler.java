package net.wowamod.handlers;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes; // Добавлен импорт для ParticleTypes
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.wowamod.item.LaserguntestItem;
import net.wowamod.DynamicLightUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class LaserLightHandler {
    private static final Map<UUID, LightEntry> activeLights = new HashMap<>();
    private static final int LIGHT_DURATION = 2;

    private static class LightEntry {
        BlockPos basePos;
        BlockPos endPos;
        int ticksRemaining;

        LightEntry(BlockPos basePos, BlockPos endPos) {
            this.basePos = basePos;
            this.endPos = endPos;
            this.ticksRemaining = LIGHT_DURATION;
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onLaserFired(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SOLID_BLOCKS) return;

        Level world = Minecraft.getInstance().level;
        if (world == null) return;

        // Просто очищаем устаревшие записи
        activeLights.entrySet().removeIf(entry -> entry.getValue().ticksRemaining-- <= 0);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        
        Player player = event.player;
        ItemStack stack = player.getMainHandItem();
        
        if (stack.getItem() instanceof LaserguntestItem && player.isUsingItem()) {
            Vec3 start = player.getEyePosition(1.0F).add(player.getViewVector(1.0F).scale(LaserguntestItem.getBeamOffset()));
            Vec3 end = start.add(player.getViewVector(1.0F).scale(LaserguntestItem.getRange()));
            
            BlockPos basePos = BlockPos.containing(start);
            BlockPos endPos = BlockPos.containing(end);
            
            LightEntry entry = activeLights.get(player.getUUID());
            if (entry == null) {
                entry = new LightEntry(basePos, endPos);
                activeLights.put(player.getUUID(), entry);
            } else {
                entry.basePos = basePos;
                entry.endPos = endPos;
                entry.ticksRemaining = LIGHT_DURATION;
            }
            
            // Визуальные эффекты вместо реального света
            spawnLightParticles(player.level(), basePos);
            spawnLightParticles(player.level(), endPos);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void spawnLightParticles(Level world, BlockPos pos) {
        if (world.isClientSide()) {
            for (int i = 0; i < 3; i++) {
                double x = pos.getX() + 0.5 + world.random.nextGaussian() * 0.3;
                double y = pos.getY() + 0.5 + world.random.nextGaussian() * 0.3;
                double z = pos.getZ() + 0.5 + world.random.nextGaussian() * 0.3;
                
                world.addParticle(ParticleTypes.GLOW,
                                x, y, z,
                                0, 0, 0);
            }
        }
    }
}