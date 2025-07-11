package net.wowamod.procedures;

import net.wowamod.network.Universe3090ModVariables;
import net.wowamod.init.Universe3090ModItems;

import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.Minecraft;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.systems.RenderSystem;

import org.joml.Matrix4f;

public class TeleportPriNazhatiiKlavishiProcedure {
    /**
     * The original server-side logic for teleportation. This remains unchanged.
     */
    public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
        if (entity == null)
            return;

        // Check if the player is wearing the full armor set
        boolean hasFullSet = entity instanceof LivingEntity _living
                && _living.getItemBySlot(EquipmentSlot.HEAD).getItem() == Universe3090ModItems.WOWABRONYA_HELMET.get()
                && _living.getItemBySlot(EquipmentSlot.CHEST).getItem() == Universe3090ModItems.WOWABRONYA_CHESTPLATE.get()
                && _living.getItemBySlot(EquipmentSlot.LEGS).getItem() == Universe3090ModItems.WOWABRONYA_LEGGINGS.get()
                && _living.getItemBySlot(EquipmentSlot.FEET).getItem() == Universe3090ModItems.WOWABRONYA_BOOTS.get();

        if (hasFullSet) {
            // ИЗМЕНЕНО: Дальность телепортации увеличена до 32 блоков (2 чанка)
            BlockPos targetPos = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(32)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos();
            
            // Teleportation logic
            {
                Entity _ent = entity;
                _ent.teleportTo(targetPos.getX() + 0.5, targetPos.getY() + 1, targetPos.getZ() + 0.5);
                if (_ent instanceof ServerPlayer _serverPlayer)
                    _serverPlayer.connection.teleport(targetPos.getX() + 0.5, targetPos.getY() + 1, targetPos.getZ() + 0.5, _ent.getYRot(), _ent.getXRot());
            }

            // Sound effect
            if (world instanceof Level _level) {
                if (!_level.isClientSide()) {
                    _level.playSound(null, BlockPos.containing(x, y, z), ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.enderman.teleport")), SoundSource.NEUTRAL, 2, 2);
                } else {
                    _level.playLocalSound(x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.enderman.teleport")), SoundSource.NEUTRAL, 2, 2, false);
                }
            }

            // Cooldown logic
            if ((entity.getCapability(Universe3090ModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new Universe3090ModVariables.PlayerVariables())).wowaswordupgrade >= 50) {
                if (entity instanceof Player _player)
                    _player.getCooldowns().addCooldown(Universe3090ModItems.WOWABRONYA_HELMET.get(), 2);
            } else {
                if (entity instanceof Player _player)
                    _player.getCooldowns().addCooldown(Universe3090ModItems.WOWABRONYA_HELMET.get(), 7);
            }
        }
    }

    /**
     * Inner class to handle client-side rendering for the block highlight.
     * It is registered to the FORGE event bus only on the client.
     */
    @Mod.EventBusSubscriber(modid = "universe3090", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class BlockHighlighter {
        private static BlockPos highlightedBlock = null;

        /**
         * On every player tick, check if they are wearing the armor.
         * If so, find the block they are looking at and store its position.
         */
        @SubscribeEvent
        public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
            if (event.phase == TickEvent.Phase.END && event.player == Minecraft.getInstance().player) {
                Player player = event.player;

                // Check if the player is wearing the full armor set
                boolean hasFullSet = player.getItemBySlot(EquipmentSlot.HEAD).getItem() == Universe3090ModItems.WOWABRONYA_HELMET.get()
                        && player.getItemBySlot(EquipmentSlot.CHEST).getItem() == Universe3090ModItems.WOWABRONYA_CHESTPLATE.get()
                        && player.getItemBySlot(EquipmentSlot.LEGS).getItem() == Universe3090ModItems.WOWABRONYA_LEGGINGS.get()
                        && player.getItemBySlot(EquipmentSlot.FEET).getItem() == Universe3090ModItems.WOWABRONYA_BOOTS.get();

                if (hasFullSet) {
                    // ИЗМЕНЕНО: Дальность подсветки увеличена до 32 блоков (2 чанка)
                    highlightedBlock = getBlockLookingAt(player, 32);
                } else {
                    highlightedBlock = null; // Clear highlight if armor is removed
                }
            }
        }

        /**
         * After the world renders, draw our highlight if a block has been targeted.
         */
        @SubscribeEvent
        public static void onRenderLevelStage(RenderLevelStageEvent event) {
            if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SOLID_BLOCKS && highlightedBlock != null) {
                renderHighlightedBlock(event.getPoseStack(), highlightedBlock);
            }
        }

        /**
         * Renders a bright, semi-transparent box around the target block.
         * @param poseStack The current pose stack for transformations.
         * @param blockPos The position of the block to highlight.
         */
        private static void renderHighlightedBlock(PoseStack poseStack, BlockPos blockPos) {
            // Get camera position to correctly translate the rendering
            var cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();

            // Define the bounding box for the highlight, slightly larger than the block
            AABB highlightBox = new AABB(blockPos).inflate(0.005);

            // Set up RenderSystem for transparent rendering
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            RenderSystem.depthMask(false); // Disable depth writing for transparency
            RenderSystem.disableCull(); // Render all faces of the box

            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder bufferBuilder = tesselator.getBuilder();

            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            poseStack.pushPose();
            // Translate the box to its world position relative to the camera
            poseStack.translate(highlightBox.minX - cameraPos.x(), highlightBox.minY - cameraPos.y(), highlightBox.minZ - cameraPos.z());
            Matrix4f matrix = poseStack.last().pose();

            // ИЗМЕНЕНО: Цвет изменен на более заметный красный с большей непрозрачностью
            float r = 1.0f, g = 0.2f, b = 0.2f, a = 0.4f;

            // Draw all 6 faces of the box
            drawSolidBox(bufferBuilder, matrix, (float) (highlightBox.getXsize()), (float) (highlightBox.getYsize()), (float) (highlightBox.getZsize()), r, g, b, a);

            tesselator.end();
            poseStack.popPose();

            // Restore RenderSystem state
            RenderSystem.enableCull();
            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
        }

        /**
         * Helper method to draw the 6 faces of a solid-colored box.
         */
        private static void drawSolidBox(BufferBuilder buffer, Matrix4f matrix, float sizeX, float sizeY, float sizeZ, float r, float g, float b, float a) {
            // Bottom face (Y=0)
            buffer.vertex(matrix, 0, 0, 0).color(r, g, b, a).endVertex();
            buffer.vertex(matrix, 0, 0, sizeZ).color(r, g, b, a).endVertex();
            buffer.vertex(matrix, sizeX, 0, sizeZ).color(r, g, b, a).endVertex();
            buffer.vertex(matrix, sizeX, 0, 0).color(r, g, b, a).endVertex();
            // Top face (Y=sizeY)
            buffer.vertex(matrix, 0, sizeY, 0).color(r, g, b, a).endVertex();
            buffer.vertex(matrix, sizeX, sizeY, 0).color(r, g, b, a).endVertex();
            buffer.vertex(matrix, sizeX, sizeY, sizeZ).color(r, g, b, a).endVertex();
            buffer.vertex(matrix, 0, sizeY, sizeZ).color(r, g, b, a).endVertex();
            // North face (-Z)
            buffer.vertex(matrix, 0, 0, 0).color(r, g, b, a).endVertex();
            buffer.vertex(matrix, sizeX, 0, 0).color(r, g, b, a).endVertex();
            buffer.vertex(matrix, sizeX, sizeY, 0).color(r, g, b, a).endVertex();
            buffer.vertex(matrix, 0, sizeY, 0).color(r, g, b, a).endVertex();
            // South face (+Z)
            buffer.vertex(matrix, 0, 0, sizeZ).color(r, g, b, a).endVertex();
            buffer.vertex(matrix, 0, sizeY, sizeZ).color(r, g, b, a).endVertex();
            buffer.vertex(matrix, sizeX, sizeY, sizeZ).color(r, g, b, a).endVertex();
            buffer.vertex(matrix, sizeX, 0, sizeZ).color(r, g, b, a).endVertex();
            // West face (-X)
            buffer.vertex(matrix, 0, 0, 0).color(r, g, b, a).endVertex();
            buffer.vertex(matrix, 0, sizeY, 0).color(r, g, b, a).endVertex();
            buffer.vertex(matrix, 0, sizeY, sizeZ).color(r, g, b, a).endVertex();
            buffer.vertex(matrix, 0, 0, sizeZ).color(r, g, b, a).endVertex();
            // East face (+X)
            buffer.vertex(matrix, sizeX, 0, 0).color(r, g, b, a).endVertex();
            buffer.vertex(matrix, sizeX, 0, sizeZ).color(r, g, b, a).endVertex();
            buffer.vertex(matrix, sizeX, sizeY, sizeZ).color(r, g, b, a).endVertex();
            buffer.vertex(matrix, sizeX, sizeY, 0).color(r, g, b, a).endVertex();
        }
        
        /**
         * Performs a raycast to find the block an entity is looking at.
         * @param entity The entity to raycast from.
         * @param maxDistance The maximum distance to check.
         * @return The BlockPos of the targeted block, or null if no block is hit.
         */
        private static BlockPos getBlockLookingAt(Entity entity, double maxDistance) {
            var rayTraceResult = entity.level().clip(new ClipContext(entity.getEyePosition(1f), entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(maxDistance)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity));
            return rayTraceResult.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK ? rayTraceResult.getBlockPos() : null;
        }
    }
}
