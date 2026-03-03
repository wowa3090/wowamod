package net.wowamod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.wowamod.OrbitalBeamEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

@Mod.EventBusSubscriber(modid = OrbitalBeamRenderer.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class OrbitalBeamRenderer extends EntityRenderer<OrbitalBeamEntity> {
    public static final String MODID = "universe3090";

    private static final ResourceLocation BEAM_TEXTURE = new ResourceLocation("textures/entity/beacon_beam.png");
    private static final ResourceLocation GLOW_TEXTURE = new ResourceLocation("textures/misc/white.png");

    // параметры тряски камеры
    private static float cameraShakeIntensity = 0f;
    private static float cameraShakeDuration = 0f;
    private static long cameraShakeStartTick = 0L;

    public OrbitalBeamRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.shadowRadius = 0.0f;
    }

    @Override
    public void render(OrbitalBeamEntity entity, float yaw, float partialTicks,
        PoseStack poseStack, MultiBufferSource buffers, int packedLight) {

        float life = entity.getLife() + partialTicks;
        float warmup = OrbitalBeamEntity.WARMUP;
        float peak   = OrbitalBeamEntity.PEAK;
        float fade   = OrbitalBeamEntity.FADE;

        // прозрачность по фазам
        float alpha;
        if (life < warmup) {
            alpha = Mth.lerp(life / warmup, 0.0f, 0.85f);
        } else if (life < (warmup + peak)) {
            alpha = 0.95f;
        } else {
            float fadeLife = life - (warmup + peak);
            alpha = Mth.lerp(Mth.clamp(fadeLife / fade, 0f, 1f), 0.95f, 0.0f);
        }
        if (alpha <= 0.01f) return;

        // увеличенный радиус
        float baseRadius = entity.getBeamRadius() * 0.65f;

        // анимация ширины
        float time = (entity.level().getGameTime() + partialTicks) * 0.05f;
        float softPulse = 0.15f * Mth.sin(time * Mth.TWO_PI);
        float t = (life - warmup) / 3f;
		t = Mth.clamp(t, 0f, 1f);
		float hardPulse = 0.35f * (float) Mth.smoothstep(t);
        float animatedRadius = baseRadius * (1.0f + softPulse + hardPulse);

        // начало и конец луча
        Vec3 end = entity.position();
		double groundY = entity.level().getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, (int)end.x, (int)end.z);
		Vec3 start = new Vec3(end.x, groundY, end.z); // низ
		Vec3 top   = new Vec3(end.x, end.y + 300, end.z); // верх

        // камера
        Minecraft mc = Minecraft.getInstance();
        Vec3 camPos = mc.gameRenderer.getMainCamera().getPosition();

        poseStack.pushPose();
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);

        VertexConsumer lightning = buffers.getBuffer(RenderType.lightning());

        // внешний ореол
        float widthOuter = animatedRadius * 2.8f;
        int outerA = (int) (110 * alpha);
        drawCylinder(poseStack, lightning, start, end, widthOuter, 255, 40, 40, outerA);

        // внутренний стержень
        float widthInner = animatedRadius * 1.2f;
        int innerA = (int) (220 * alpha);
        drawCylinder(poseStack, lightning, start, end, widthInner, 210, 230, 255, innerA);

		// свечение
        float swirlU = (entity.tickCount + partialTicks) * 0.01f;
        float swirlV = (entity.tickCount + partialTicks) * 0.02f;
        VertexConsumer glow = buffers.getBuffer(RenderType.energySwirl(GLOW_TEXTURE, swirlU, swirlV));
        float widthGlow = animatedRadius * 3.2f;
        int glowA = (int) (140 * alpha);
        drawCylinder(poseStack, glow, start, end, widthGlow, 150, 200, 255, glowA);

        poseStack.popPose();

        // триггер тряски камеры
        if (entity.level().isClientSide && life >= warmup && life <= warmup + 0.5f) {
            triggerCameraShake(0.8f, 10f);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(OrbitalBeamEntity entity) {
        return BEAM_TEXTURE;
    }

    /* ======== цилиндр ======== */
    private static void drawCylinder(PoseStack poseStack, VertexConsumer consumer,
                                     Vec3 start, Vec3 end, float width,
                                     int r, int g, int b, int a) {
        Vec3 direction = end.subtract(start);
        if (direction.lengthSqr() < 1e-6) return;
        direction = direction.normalize();
        Vec3 perp1 = direction.cross(new Vec3(0, 1, 0));
        if (perp1.lengthSqr() < 1E-6) perp1 = direction.cross(new Vec3(1, 0, 0));
        perp1 = perp1.normalize();
        Vec3 perp2 = direction.cross(perp1).normalize();

        Matrix4f matrix = poseStack.last().pose();
        int light = 0xF000F0;
        int sides = 12;

        for (int i = 0; i < sides; i++) {
            float angle1 = (float) i / sides * Mth.TWO_PI;
            float angle2 = (float) (i + 1) / sides * Mth.TWO_PI;

            Vec3 ring1 = perp1.scale(Mth.cos(angle1) * width).add(perp2.scale(Mth.sin(angle1) * width));
            Vec3 ring2 = perp1.scale(Mth.cos(angle2) * width).add(perp2.scale(Mth.sin(angle2) * width));

            Vec3 p1_start = start.add(ring1);
            Vec3 p2_start = start.add(ring2);
            Vec3 p1_end   = end.add(ring1);
            Vec3 p2_end   = end.add(ring2);

            addQuad(consumer, matrix, p1_start, p2_start, p2_end, p1_end, r, g, b, a, light);
        }
    }

    private static void addQuad(VertexConsumer consumer, Matrix4f matrix,
	                            Vec3 p1, Vec3 p2, Vec3 p3, Vec3 p4,
	                            int r, int g, int b, int a, int light) {
	    int overlay = net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY;
	
	    consumer.vertex(matrix, (float)p1.x, (float)p1.y, (float)p1.z)
	            .color(r, g, b, a).uv(0f, 0f).overlayCoords(overlay).uv2(light).normal(0f, 1f, 0f).endVertex();
	    consumer.vertex(matrix, (float)p2.x, (float)p2.y, (float)p2.z)
	            .color(r, g, b, a).uv(1f, 0f).overlayCoords(overlay).uv2(light).normal(0f, 1f, 0f).endVertex();
	    consumer.vertex(matrix, (float)p3.x, (float)p3.y, (float)p3.z)
	            .color(r, g, b, a).uv(1f, 1f).overlayCoords(overlay).uv2(light).normal(0f, 1f, 0f).endVertex();
	    consumer.vertex(matrix, (float)p4.x, (float)p4.y, (float)p4.z)
	            .color(r, g, b, a).uv(0f, 1f).overlayCoords(overlay).uv2(light).normal(0f, 1f, 0f).endVertex();
	}

    /* ======== тряска камеры ======== */
    private static void triggerCameraShake(float intensity, float durationTicks) {
        cameraShakeIntensity = Math.max(cameraShakeIntensity, intensity);
        cameraShakeDuration = Math.max(cameraShakeDuration, durationTicks);
        cameraShakeStartTick = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getGameTime() : 0L;
    }


	@SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        if (cameraShakeIntensity <= 0f || cameraShakeDuration <= 0f) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        long now = mc.level.getGameTime();
        float elapsed = (float) (now - cameraShakeStartTick);
        if (elapsed > cameraShakeDuration) {
            // затухание
            cameraShakeIntensity = 0f;
            cameraShakeDuration = 0f;
            return;
        }

// нелинейное затухание
        float t = Mth.clamp(elapsed / cameraShakeDuration, 0f, 1f);
        float falloff = (1f - t) * (0.6f + 0.4f * (1f - t));
        float intensity = cameraShakeIntensity * falloff;

        // шумы для поворотов камеры
        float seed = now * 0.15f + (float) Math.random();
        float yawShake   = (Mth.sin(seed * 2.1f) + Mth.cos(seed * 1.7f)) * 0.6f * intensity;
        float pitchShake = (Mth.cos(seed * 2.8f) + Mth.sin(seed * 2.3f)) * 0.5f * intensity;
        float rollShake  = (Mth.sin(seed * 3.4f)) * 0.4f * intensity;

        event.setYaw(event.getYaw() + yawShake);
        event.setPitch(event.getPitch() + pitchShake);
        event.setRoll(event.getRoll() + rollShake);
    }
}