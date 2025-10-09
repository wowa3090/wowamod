package net.wowamod.handlers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.wowamod.init.Universe3090ModItems;
// import net.wowamod.init.Universe3090ModSounds; // <-- Раскомментируйте, когда создадите свои звуки
import net.wowamod.network.DealLaserDamagePacket;
import net.wowamod.network.PacketHandler;
import org.joml.Matrix4f;

import java.util.Optional;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class LaserRayHandler {

    private static boolean laserActive = false;
    private static boolean wasLaserActive = false;
    private static float laserIntensity = 0.0f;
    
    private static Vec3 prevLaserStart = Vec3.ZERO;
    private static Vec3 prevLaserEnd = Vec3.ZERO;
    private static Vec3 laserStart = Vec3.ZERO;
    private static Vec3 laserEnd = Vec3.ZERO;

    private static int damageCooldown = 0;
    private static final double LASER_RANGE = 64.0;
    private static final float LASER_WIDTH = 0.1F;
    public static final float MAX_DAMAGE_AMOUNT = 10.0F;

    @SubscribeEvent
    public static void onMouseInput(InputEvent.MouseButton.Pre event) {
        if (event.getButton() != 1) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.screen != null) return;

        boolean mainHandHasItem = player.getMainHandItem().is(Universe3090ModItems.MT_WTESTANIM.get());
        boolean offHandHasItem = player.getOffhandItem().is(Universe3090ModItems.MT_WTESTANIM.get());

        if (mainHandHasItem || offHandHasItem) {
            InteractionHand hand = mainHandHasItem ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
            if (event.getAction() == 1) { // Press
                if (!laserActive) { // Предотвращаем повторный вызов
                    laserActive = true;
                    player.startUsingItem(hand);
                }
            } else if (event.getAction() == 0) { // Release
                if (laserActive) {
                    laserActive = false;
                    player.stopUsingItem();
                }
            }
            event.setCanceled(true);
        } else {
            if (laserActive) {
                laserActive = false;
                player.stopUsingItem();
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;

            // --- ИСПРАВЛЕННАЯ ЛОГИКА ---
            // Добавляем защитную проверку: если лазер должен быть активен, но игрок сменил предмет, выключаем.
            if (laserActive) {
                 boolean hasItem = mc.player.getMainHandItem().is(Universe3090ModItems.MT_WTESTANIM.get()) ||
                                   mc.player.getOffhandItem().is(Universe3090ModItems.MT_WTESTANIM.get());
                if (!hasItem) {
                    laserActive = false;
                    // stopUsingItem() будет вызван автоматически, так как предмет сменился
                }
            }
            
            laserIntensity = Mth.lerp(0.25f, laserIntensity, laserActive ? 1.0f : 0.0f);
            prevLaserStart = laserStart;
            prevLaserEnd = laserEnd;

            // Убрана проблемная проверка isStillUsing, которая вызывала баг
            if (laserActive || laserIntensity > 0.01f) {
                updateLaserState(mc.player);
            }
            
            handleLaserSounds(mc.player);
            
            wasLaserActive = laserActive;
            
            if (damageCooldown > 0) {
                damageCooldown--;
            }
        }
    }

    private static void handleLaserSounds(Player player) {
        if (laserActive && !wasLaserActive) {
            player.level().playLocalSound(player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.9f, 1.6f, false);
            Minecraft.getInstance().getSoundManager().play(new LaserLoopSoundInstance(player));
        }
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES && laserIntensity > 0.01f) {
            float partialTicks = event.getPartialTick();
            Vec3 smoothStart = prevLaserStart.lerp(laserStart, partialTicks);
            Vec3 smoothEnd = prevLaserEnd.lerp(laserEnd, partialTicks);
            renderLaserBeam(event.getPoseStack(), smoothStart, smoothEnd);
        }
    }

    private static Vec3 getLaserStartPosition(Player player, float partialTicks) {
        InteractionHand hand = player.getUsedItemHand();
        if (hand == null) hand = player.getMainHandItem().is(Universe3090ModItems.MT_WTESTANIM.get()) ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;

        Vec3 cameraPos = player.getEyePosition(partialTicks);
        Vec3 lookVec = player.getViewVector(partialTicks);
        Vec3 upVec = player.getUpVector(partialTicks);
        Vec3 rightVec = lookVec.cross(upVec).normalize();

        if (hand == InteractionHand.MAIN_HAND) {
            return cameraPos.add(lookVec.scale(0.05)).add(rightVec.scale(0.35)).add(upVec.scale(-0.26));
        } else {
            return cameraPos.add(lookVec.scale(0.05)).add(rightVec.scale(-0.35)).add(upVec.scale(-0.28));
        }
    }

    private static void updateLaserState(Player player) {
        Level level = player.level();
        if (level == null) return;

        laserStart = getLaserStartPosition(player, 1.0f);
        Vec3 lookVec = player.getLookAngle();
        Vec3 endPoint = laserStart.add(lookVec.scale(LASER_RANGE));

        BlockHitResult blockHit = level.clip(new ClipContext(laserStart, endPoint, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (blockHit.getType() != HitResult.Type.MISS) {
            endPoint = blockHit.getLocation();
        }

        double finalDistance = laserStart.distanceToSqr(endPoint);
        AABB searchBox = new AABB(laserStart, endPoint);
        Entity finalTarget = null;
        
        for (Entity entity : level.getEntities(player, searchBox, e -> e.isAlive() && !e.isSpectator() && e.isPickable())) {
            AABB entityBox = entity.getBoundingBox().inflate(0.3);
            Optional<Vec3> hitPosOpt = entityBox.clip(laserStart, endPoint);

            if (hitPosOpt.isPresent()) {
                double distToEntity = laserStart.distanceToSqr(hitPosOpt.get());
                if (distToEntity < finalDistance) {
                    finalDistance = distToEntity;
                    endPoint = hitPosOpt.get();
                    finalTarget = entity;
                }
            }
        }
        
        laserEnd = endPoint;

        if (finalTarget != null && laserIntensity > 0.8f && damageCooldown <= 0) {
            float currentDamage = MAX_DAMAGE_AMOUNT * laserIntensity;
            PacketHandler.INSTANCE.sendToServer(new DealLaserDamagePacket(finalTarget.getId(), currentDamage));
            damageCooldown = 4;
            player.level().playLocalSound(finalTarget.getX(), finalTarget.getY(), finalTarget.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.5f, 1.0f, false);
        }
    }

    private static void renderLaserBeam(PoseStack poseStack, Vec3 start, Vec3 end) {
        if (start.equals(end)) return;
        Minecraft mc = Minecraft.getInstance();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        Vec3 camPos = mc.gameRenderer.getMainCamera().getPosition();
        float currentWidth = LASER_WIDTH * laserIntensity;

        poseStack.pushPose();
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);
        
        drawCylinder(poseStack, bufferSource.getBuffer(RenderType.lightning()), start, end, currentWidth * 2.5f, 255, 20, 20, (int)(90 * laserIntensity));
        drawCylinder(poseStack, bufferSource.getBuffer(RenderType.lightning()), start, end, currentWidth, 255, 200, 200, (int)(200 * laserIntensity));

        bufferSource.endBatch(RenderType.lightning());
        poseStack.popPose();
    }
    
    private static void drawCylinder(PoseStack poseStack, VertexConsumer consumer, Vec3 start, Vec3 end, float width, int r, int g, int b, int a) {
        Vec3 direction = end.subtract(start).normalize();
        Vec3 perp1 = direction.cross(new Vec3(0, 1, 0));
        if (perp1.lengthSqr() < 1E-6) perp1 = direction.cross(new Vec3(1, 0, 0));
        perp1 = perp1.normalize();
        Vec3 perp2 = direction.cross(perp1).normalize();
        Matrix4f matrix = poseStack.last().pose();
        int light = 0xF000F0;
        int sides = 8;

        for (int i = 0; i < sides; i++) {
            float angle1 = (float) i / sides * Mth.TWO_PI;
            float angle2 = (float) (i + 1) / sides * Mth.TWO_PI;
            Vec3 p1_start = start.add(perp1.scale(Mth.cos(angle1) * width)).add(perp2.scale(Mth.sin(angle1) * width));
            Vec3 p2_start = start.add(perp1.scale(Mth.cos(angle2) * width)).add(perp2.scale(Mth.sin(angle2) * width));
            Vec3 p1_end = end.add(perp1.scale(Mth.cos(angle1) * width)).add(perp2.scale(Mth.sin(angle1) * width));
            Vec3 p2_end = end.add(perp1.scale(Mth.cos(angle2) * width)).add(perp2.scale(Mth.sin(angle2) * width));
            addQuad(consumer, matrix, p1_start, p2_start, p2_end, p1_end, r, g, b, a, light);
        }
    }
    
    private static void addQuad(VertexConsumer consumer, Matrix4f matrix, Vec3 p1, Vec3 p2, Vec3 p3, Vec3 p4, int r, int g, int b, int a, int light) {
        consumer.vertex(matrix, (float)p1.x, (float)p1.y, (float)p1.z).color(r, g, b, a).uv2(light).endVertex();
        consumer.vertex(matrix, (float)p2.x, (float)p2.y, (float)p2.z).color(r, g, b, a).uv2(light).endVertex();
        consumer.vertex(matrix, (float)p3.x, (float)p3.y, (float)p3.z).color(r, g, b, a).uv2(light).endVertex();
        consumer.vertex(matrix, (float)p4.x, (float)p4.y, (float)p4.z).color(r, g, b, a).uv2(light).endVertex();
    }
    
    private static class LaserLoopSoundInstance extends AbstractTickableSoundInstance {
        private final Player player;

        public LaserLoopSoundInstance(Player player) {
            super(SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
            this.player = player;
            this.looping = true;
            this.delay = 0;
            this.volume = 0.68F;
        }

        @Override
        public void tick() {
            // ИСПРАВЛЕНО: Условие остановки звука теперь более простое и надежное
            if (!laserActive || !player.isAlive()) {
                this.stop();
                return;
            }
            this.x = player.getX();
            this.y = player.getY();
            this.z = player.getZ();
            this.volume = 0.8f * laserIntensity;
            this.pitch = 1.0f + (laserIntensity - 1.0f) * 0.2f;
        }
    }
}