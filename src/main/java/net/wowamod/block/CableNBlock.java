package net.wowamod.block;

import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.wowamod.procedures.CableUpdateTickEnergyProcedure;
import net.wowamod.block.entity.CableNBlockEntity;

import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Containers;
import net.minecraft.util.RandomSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

public class CableNBlock extends Block implements EntityBlock {
    // 1. Создаем 6 направлений соединений
    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final BooleanProperty UP = BooleanProperty.create("up");
    public static final BooleanProperty DOWN = BooleanProperty.create("down");

    // Словарь для удобного доступа к свойствам по направлению
    public static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = new HashMap<>();
    static {
        PROPERTY_BY_DIRECTION.put(Direction.NORTH, NORTH);
        PROPERTY_BY_DIRECTION.put(Direction.EAST, EAST);
        PROPERTY_BY_DIRECTION.put(Direction.SOUTH, SOUTH);
        PROPERTY_BY_DIRECTION.put(Direction.WEST, WEST);
        PROPERTY_BY_DIRECTION.put(Direction.UP, UP);
        PROPERTY_BY_DIRECTION.put(Direction.DOWN, DOWN);
    }

    public CableNBlock() {
        super(BlockBehaviour.Properties.of().sound(SoundType.WOOL).strength(2.5f).noOcclusion().pushReaction(PushReaction.BLOCK).isRedstoneConductor((bs, br, bp) -> false));
        // Состояние по умолчанию - кубик без соединений
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(NORTH, false).setValue(EAST, false)
                .setValue(SOUTH, false).setValue(WEST, false)
                .setValue(UP, false).setValue(DOWN, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
    }

    // 2. Логика проверки: можно ли подключиться к соседу?
    private boolean canConnect(LevelAccessor world, BlockPos pos, Direction dir) {
        BlockPos neighborPos = pos.relative(dir);
        BlockState neighborState = world.getBlockState(neighborPos);
        
        // Подключаемся к такому же кабелю
        if (neighborState.getBlock() instanceof CableNBlock) {
            return true;
        }
        
        // Подключаемся к механизмам с энергией
        BlockEntity blockEntity = world.getBlockEntity(neighborPos);
        if (blockEntity != null) {
            // Проверяем энергию именно с той стороны, с которой входит кабель
            return blockEntity.getCapability(ForgeCapabilities.ENERGY, dir.getOpposite()).isPresent();
        }
        return false;
    }

    // 3. Вычисляем все стороны при установке блока в мире
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        return this.defaultBlockState()
                .setValue(NORTH, canConnect(world, pos, Direction.NORTH))
                .setValue(EAST, canConnect(world, pos, Direction.EAST))
                .setValue(SOUTH, canConnect(world, pos, Direction.SOUTH))
                .setValue(WEST, canConnect(world, pos, Direction.WEST))
                .setValue(UP, canConnect(world, pos, Direction.UP))
                .setValue(DOWN, canConnect(world, pos, Direction.DOWN));
    }

    // 4. Мгновенно обновляем соединения, если рядом поставили/сломали механизм
    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor world, BlockPos currentPos, BlockPos facingPos) {
        return state.setValue(PROPERTY_BY_DIRECTION.get(facing), canConnect(world, currentPos, facing));
    }

    // 5. Динамический хитбокс (коллизия), который меняется вместе с соединениями!
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        // Центральное ядро кабеля
        VoxelShape shape = box(5, 5, 5, 11, 11, 11);

        // Добавляем отростки в зависимости от соединений
        if (state.getValue(NORTH)) shape = Shapes.or(shape, box(5, 5, 0, 11, 11, 5));
        if (state.getValue(SOUTH)) shape = Shapes.or(shape, box(5, 5, 11, 11, 11, 16));
        if (state.getValue(EAST))  shape = Shapes.or(shape, box(11, 5, 5, 16, 11, 11));
        if (state.getValue(WEST))  shape = Shapes.or(shape, box(0, 5, 5, 5, 11, 11));
        if (state.getValue(UP))    shape = Shapes.or(shape, box(5, 11, 5, 11, 16, 11));
        if (state.getValue(DOWN))  shape = Shapes.or(shape, box(5, 0, 5, 11, 5, 11));

        return shape;
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return Shapes.empty(); // Оставляем пустым для правильного рендера света
    }

    // --- ОСТАЛЬНОЙ КОД ОСТАВЛЕН БЕЗ ИЗМЕНЕНИЙ (ТВОИ ПРОЦЕДУРЫ И ТАЙЛЫ) --- //

    @Override
    public void appendHoverText(ItemStack itemstack, BlockGetter world, List<Component> list, TooltipFlag flag) {
        super.appendHoverText(itemstack, world, list, flag);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) { return true; }

    @Override
    public int getLightBlock(BlockState state, BlockGetter worldIn, BlockPos pos) { return 0; }

    @Override
    public BlockPathTypes getBlockPathType(BlockState state, BlockGetter world, BlockPos pos, Mob entity) { return BlockPathTypes.BLOCKED; }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> dropsOriginal = super.getDrops(state, builder);
        if (!dropsOriginal.isEmpty()) return dropsOriginal;
        return Collections.singletonList(new ItemStack(this, 1));
    }

    @Override
    public void onPlace(BlockState blockstate, Level world, BlockPos pos, BlockState oldState, boolean moving) {
        super.onPlace(blockstate, world, pos, oldState, moving);
        world.scheduleTick(pos, this, 1);
    }

    @Override
    public void tick(BlockState blockstate, ServerLevel world, BlockPos pos, RandomSource random) {
        super.tick(blockstate, world, pos, random);
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        CableUpdateTickEnergyProcedure.execute(world, x, y, z);
        world.scheduleTick(pos, this, 1);
    }

    @Override
    public MenuProvider getMenuProvider(BlockState state, Level worldIn, BlockPos pos) {
        BlockEntity tileEntity = worldIn.getBlockEntity(pos);
        return tileEntity instanceof MenuProvider menuProvider ? menuProvider : null;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CableNBlockEntity(pos, state);
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
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof CableNBlockEntity be) {
                Containers.dropContents(world, pos, be);
                world.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, world, pos, newState, isMoving);
        }
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) { return true; }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level world, BlockPos pos) {
        BlockEntity tileentity = world.getBlockEntity(pos);
        if (tileentity instanceof CableNBlockEntity be) return AbstractContainerMenu.getRedstoneSignalFromContainer(be);
        return 0;
    }
}