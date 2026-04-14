package net.wowamod.block;

import net.minecraft.core.Direction;
// Убрана ошибочная строка import net.minecraft;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks; // ДОБАВЛЕНО: для Blocks.AIR
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader; // ДОБАВЛЕНО: для проверки выживания блока
import net.minecraft.world.level.LevelAccessor; // ДОБАВЛЕНО: для обновления соседей
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;
import java.util.List;
import net.minecraft.world.level.levelgen.feature.BlueIceFeature;
import net.minecraft.world.level.levelgen.feature.BlueIceFeature;

public class YellowemeraldcrystalBlock extends Block {
    // 1. Создаем свойство направления (поддерживает все 6 сторон)
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public YellowemeraldcrystalBlock() {
        super(BlockBehaviour.Properties.of()
            .sound(SoundType.AMETHYST)
            .strength(2.4f, 10f)
            .lightLevel(s -> 7)
            .noOcclusion()
            .isRedstoneConductor((bs, br, bp) -> false)
        );
        // 2. Устанавливаем значение по умолчанию (вниз, т.е. стоит на полу)
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
    }

    // 3. Определяем, какую сторону выбрал игрок (или генератор) при установке
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    // 4. Регистрируем свойство в системе состояний блока
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    // Стандартные методы для поддержки вращения (нужно для структур и механизмов)
    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
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

    // 5. Обновляем хитбокс в зависимости от направления
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        Direction direction = state.getValue(FACING);
        // Базовая форма (когда кристалл на полу)
        VoxelShape baseShape = Shapes.or(
            box(7.25, 0, 7.25, 8.75, 4.25, 8.75), 
            box(7.25, 0, 7.25, 8.75, 4.75, 8.75), 
            box(7, 0.25, 7, 8.5, 3.25, 8.5), 
            box(7.25, 0, 7.25, 8.75, 4, 8.75), 
            box(7, 0, 6.75, 8.5, 4, 8.25)
        );

        return switch (direction) {
            case UP ->    Block.box(5.0, 0.0, 5.0, 11.0, 9.0, 11.0);    // На полу
            case DOWN ->  Block.box(5.0, 7.0, 5.0, 11.0, 16.0, 11.0);   // На потолке
            case NORTH -> Block.box(5.0, 5.0, 7.0, 11.0, 11.0, 16.0);   // На северной стене
            case SOUTH -> Block.box(5.0, 5.0, 0.0, 11.0, 11.0, 9.0);    // На южной стене
            case WEST ->  Block.box(7.0, 5.0, 5.0, 16.0, 11.0, 11.0);   // На западной стене
            case EAST ->  Block.box(0.0, 5.0, 5.0, 9.0, 11.0, 11.0);    // На восточной стене
        };
    }

    private VoxelShape rotateShape(Direction from, Direction to, VoxelShape shape) {
        // Заглушка для вращения хитбокса (оставляем как было для простоты)
        return shape; 
    }

    // УЛУЧШЕННАЯ ЛОГИКА ВЫЖИВАНИЯ БЛОКА (Теперь поддерживает потолки и стены)
    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        Direction direction = state.getValue(FACING);
        // Получаем позицию блока, к которому прикреплен кристалл (противоположная сторона от направления кристалла)
        BlockPos attachedPos = pos.relative(direction.getOpposite());
        BlockState attachedState = world.getBlockState(attachedPos);
        
        // Проверяем, является ли грань опорного блока твердой
        return attachedState.isFaceSturdy(world, attachedPos, direction); 
    }

    // Обновляет состояние блока, если соседи изменились (например, сломали опору)
    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor world, BlockPos currentPos, BlockPos facingPos) {
        // Если блок больше не может выживать (опора пропала), превращаем его в воздух
        // При этом из него автоматически выпадет предмет, если настроена таблица лута (Loot Table)
        if (!state.canSurvive(world, currentPos)) {
            return Blocks.AIR.defaultBlockState(); 
        }
        return super.updateShape(state, facing, facingState, world, currentPos, facingPos);
    }
}