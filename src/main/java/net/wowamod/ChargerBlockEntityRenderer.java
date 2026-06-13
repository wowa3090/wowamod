package net.wowamod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.wowamod.block.entity.ChargerBlockEntity;
import net.wowamod.init.Universe3090ModBlockEntities;
import net.wowamod.init.Universe3090ModBlocks; // ДОБАВЛЕН ИМПОРТ РЕЕСТРА БЛОКОВ

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
        int bestPriority = -1;

        // --- УМНЫЙ ПОИСК ПРЕДМЕТА ДЛЯ РЕНДЕРА ---
        // Ищем тот предмет, который важнее всего показать игроку
        for (int i = 0; i < blockEntity.getContainerSize(); i++) {
            ItemStack slotStack = blockEntity.getItem(i);
            if (slotStack.isEmpty()) continue;

            int priority = 0;
            if (slotStack.getItem() == Universe3090ModBlocks.REDSTONE_9X_ENERGIZED.get().asItem()) {
                priority = 10; // Высший приоритет: Полностью заряженный блок
            } else {
                IEnergyStorage itemEnergy = slotStack.getCapability(ForgeCapabilities.ENERGY).orElse(null);
                if (itemEnergy != null && itemEnergy.getEnergyStored() >= itemEnergy.getMaxEnergyStored()) {
                    priority = 10; // Высший приоритет: Инструмент 100% заряда
                } else if (itemEnergy != null && itemEnergy.getEnergyStored() > 0) {
                    priority = 5; // Средний приоритет: Заряжающийся инструмент
                } else if (slotStack.getItem() == Universe3090ModBlocks.REDSTONE_9X.get().asItem()) {
                    int charge = slotStack.hasTag() ? slotStack.getTag().getInt("Charge") : 0;
                    priority = charge > 0 ? 5 : 0; // Средний приоритет: Заряжающийся блок
                }
            }

            // Если приоритет этого слота выше, чем у предыдущих, рендерим его!
            if (priority > bestPriority) {
                bestPriority = priority;
                stackToRender = slotStack;
            }
        }

        // Если все слоты пустые - ничего не рендерим
        if (stackToRender.isEmpty()) return;

        poseStack.pushPose();

        long time = blockEntity.getLevel() != null ? blockEntity.getLevel().getGameTime() : 0;
        float tick = time + partialTick; 
        
        float offset = (float) Math.sin(tick / 10.0F) * 0.1F;
        float rotation = tick * 3.0F;

        poseStack.translate(0.5D, 0.9D + offset, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        poseStack.scale(0.85F, 0.85F, 0.85F); 

        int lightAbove = LevelRenderer.getLightColor(blockEntity.getLevel(), blockEntity.getBlockPos().above());

        Minecraft.getInstance().getItemRenderer().renderStatic(
                stackToRender,
                ItemDisplayContext.GROUND,
                lightAbove, 
                packedOverlay,
                poseStack,
                bufferSource,
                blockEntity.getLevel(),
                (int) blockEntity.getBlockPos().asLong()
        );

        poseStack.popPose();
    }
}