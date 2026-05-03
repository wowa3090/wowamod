package net.wowamod.block;

import net.wowamod.block.entity.MWEmitterBlockBlockEntity;
import net.wowamod.block.entity.MWReceiverBlockBlockEntity;

import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.StateDefinition; // ДОБАВЛЕНО
import net.minecraft.world.level.block.state.properties.BooleanProperty; // ДОБАВЛЕНО
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Collections;

public class MWEmitterBlockBlock extends Block implements EntityBlock {

    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    
    public MWEmitterBlockBlock() {
        super(BlockBehaviour.Properties.of()
                .sound(SoundType.METAL)
                .strength(2f, 10f)
                .requiresCorrectToolForDrops()
                .noOcclusion()
                .isRedstoneConductor((bs, br, bp) -> false));
        this.registerDefaultState(this.stateDefinition.any().setValue(ACTIVE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE);
    }

    @Override
    public void appendHoverText(ItemStack itemstack, BlockGetter world, List<Component> list, TooltipFlag flag) {
        super.appendHoverText(itemstack, world, list, flag);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return true;
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter worldIn, BlockPos pos) {
        return 0;
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return box(1, 1, 1, 16, 16, 16);
    }

    @Override
    public boolean canHarvestBlock(BlockState state, BlockGetter world, BlockPos pos, Player player) {
        if (player.getInventory().getSelected().getItem() instanceof PickaxeItem tieredItem)
            return tieredItem.getTier().getLevel() >= 2;
        return false;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> dropsOriginal = super.getDrops(state, builder);
        if (!dropsOriginal.isEmpty())
            return dropsOriginal;
        return Collections.singletonList(new ItemStack(this, 1));
    }

    // --- ЛОГИКА БЕЗОПАСНОСТИ И ВЗАИМОДЕЙСТВИЯ ---

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (placer instanceof Player player && level.getBlockEntity(pos) instanceof MWEmitterBlockBlockEntity be) {
            be.setOwnerUUID(player.getUUID());
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (world.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof MWEmitterBlockBlockEntity emitter) {
            
            if (emitter.getOwnerUUID() == null) {
                System.out.println("[WOWA-LOG] Блок восстановлен! Назначен владелец: " + player.getName().getString());
                emitter.setOwnerUUID(player.getUUID());
            }

            if (!emitter.getOwnerUUID().equals(player.getUUID())) {
                player.displayClientMessage(Component.literal("§cВы не можете управлять чужим раздатчиком!"), true);
                return InteractionResult.FAIL;
            }

            if (!(world.getBlockEntity(pos.below()) instanceof MWReceiverBlockBlockEntity)) {
                player.displayClientMessage(Component.literal("§eРаздатчик (Шар) должен быть установлен на Приемник (Куб)!"), true);
                return InteractionResult.PASS;
            }

            MenuProvider menuProvider = state.getMenuProvider(world, pos);
            if (menuProvider != null && player instanceof ServerPlayer serverPlayer) {
                net.wowamod.network.wave.WaveManager manager = net.wowamod.network.wave.WaveManager.get((net.minecraft.server.level.ServerLevel) world);
                java.util.List<String> names = manager.getAllNetworkNames(player.getUUID());
                System.out.println("[WOWA-LOG] Отправляем сети игроку: " + names.toString());
                net.wowamod.network.MWNetwork.sendToPlayer(new net.wowamod.network.S2CSyncWavesPacket(names), serverPlayer);
                
                NetworkHooks.openScreen(serverPlayer, menuProvider, pos);
            }
        }
        return InteractionResult.CONSUME;
    }

    // --- ЛОГИКА BLOCK ENTITY ---

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MWEmitterBlockBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : (lvl, pos, blockState, t) -> {
            if (t instanceof MWEmitterBlockBlockEntity be) {
                be.tick(lvl, pos, blockState);
            }
        };
    }

    @Override
    public MenuProvider getMenuProvider(BlockState state, Level worldIn, BlockPos pos) {
        BlockEntity tileEntity = worldIn.getBlockEntity(pos);
        return tileEntity instanceof MenuProvider menuProvider ? menuProvider : null;
    }

    @Override
    public boolean triggerEvent(BlockState state, Level world, BlockPos pos, int eventID, int eventParam) {
        super.triggerEvent(state, world, pos, eventID, eventParam);
        BlockEntity blockEntity = world.getBlockEntity(pos);
        return blockEntity != null && blockEntity.triggerEvent(eventID, eventParam);
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            world.updateNeighbourForOutputSignal(pos, this); 
            super.onRemove(state, world, pos, newState, isMoving);
        }
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return false; 
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level world, BlockPos pos) {
        return 0;
    }
}