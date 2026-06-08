package net.wowamod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer; // НОВЫЙ ИМПОРТ
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.wowamod.block.entity.ChargerBlockEntity;
import net.wowamod.init.Universe3090ModBlockEntities;

// ИСПРАВЛЕНИЕ 1: Убрали жесткий modid. Forge подставит его сам.
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ChargerBlockEntityRenderer implements BlockEntityRenderer<ChargerBlockEntity> {

    public ChargerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}

    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                (BlockEntityType<ChargerBlockEntity>) Universe3090ModBlockEntities.CHARGER.get(), 
                ChargerBlockEntityRenderer::new
        );
    }

    @Override
    public void render(ChargerBlockEntity blockEntity, float partialTick, PoseStack poseStack, 
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        
        ItemStack stackToRender = ItemStack.EMPTY;
        for (int i = 0; i < blockEntity.getContainerSize(); i++) {
            ItemStack slotItem = blockEntity.getItem(i);
            if (!slotItem.isEmpty()) {
                stackToRender = slotItem;
                break;
            }
        }

        // Если блок пуст - не рендерим
        if (stackToRender.isEmpty()) return;

        poseStack.pushPose();

        long time = blockEntity.getLevel() != null ? blockEntity.getLevel().getGameTime() : 0;
        float tick = time + partialTick; 
        
        float offset = (float) Math.sin(tick / 10.0F) * 0.1F;
        float rotation = tick * 3.0F;

        poseStack.translate(0.5D, 0.9D + offset, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        poseStack.scale(0.85F, 0.85F, 0.85F); 

        // ИСПРАВЛЕНИЕ 3: Берем свет не из самого блока, а из пространства НАД ним!
        int lightAbove = LevelRenderer.getLightColor(blockEntity.getLevel(), blockEntity.getBlockPos().above());

        Minecraft.getInstance().getItemRenderer().renderStatic(
                stackToRender,
                ItemDisplayContext.GROUND,
                lightAbove, // Передаем исправленный свет
                packedOverlay,
                poseStack,
                bufferSource,
                blockEntity.getLevel(),
                (int) blockEntity.getBlockPos().asLong()
        );

        poseStack.popPose();
    }
}