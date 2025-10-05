package net.wowamod.procedures;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player; // Импортируем Player
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel; // Импортируем ServerLevel
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.util.RandomSource; // Импортируем RandomSource
// Убираем импорт ServerPlayer

import java.util.List;

public class RdmtpprocedureProcedure {

    // Обновлённая сигнатура метода, предполагая что MCreator передаёт все необходимые параметры.
    // Имена параметров могут отличаться в зависимости от версии MCreator.
    // Часто передаётся Context с полями, или отдельные переменные.
    public static void execute(Entity entity, Level level, double x, double y, double z, ItemStack itemstack) {
        // Проверяем что сущность - игрок, уровень - серверный, и стек инструмента не пуст.
        if (entity == null || level == null || itemstack.isEmpty() || level.isClientSide()) {
            return;
        }

        if (!(entity instanceof Player player)) {
            return;
        }

        // Приводим Level к ServerLevel, так как Block.getDrops требует ServerLevel.
        if (!(level instanceof ServerLevel serverLevel)) {
            return; // Операции с блоками на клиенте недопустимы.
        }

        // --- ИСПРАВЛЕНИЕ: Добавлен цикл по оси Y для разрушения 3x3x3 ---
        // Цикл по смещениям от -1 до 1 для осей X, Y и Z, чтобы получить куб 3x3x3.
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) { // Добавлен цикл по Y
                for (int dz = -1; dz <= 1; dz++) {
                    // Вычисляем позицию соседнего блока.
                    BlockPos currentPos = new BlockPos((int) x + dx, (int) y + dy, (int) z + dz); // Учитываем dy

                    // Получаем состояние блока на этой позиции.
                    BlockState neighborState = serverLevel.getBlockState(currentPos);

                    // Проверяем, является ли блок воздухом, чтобы не пытаться его разрушать.
                    if (neighborState.isAir()) {
                        continue; // Переходим к следующему блоку.
                    }

                    // --- ИСПРАВЛЕНИЕ: Проверка на ломаемость блока ---
                    // destroySpeed <= 0 означает, что блок не может быть разрушен (например, Бедрок).
                    if (neighborState.getDestroySpeed(serverLevel, currentPos) <= 0.0F) {
                         continue; // Переходим к следующему блоку, если текущий неломаем.
                    }
                    // --- КОНЕЦ ИСПРАВЛЕНИЯ ---

                    // Получаем BlockEntity (например, сундук), если он есть.
                    BlockEntity blockEntity = serverLevel.getBlockEntity(currentPos);

                    // Получаем список предметов, которые должны выпасть из блока при разрушении с текущим инструментом.
                    // Это учитывает зачарования, тип блока и инструмента.
                    List<ItemStack> drops = Block.getDrops(neighborState, serverLevel, currentPos, blockEntity, player, itemstack);

                    // Удаляем блок, заменяя его воздухом, без вызова стандартного разрушения с инструментом (не уменьшает прочности).
                    // Флаг 3 (Block.UPDATE_NEIGHBORS | Block.UPDATE_CLIENTS) обеспечивает правильные обновления мира.
                    serverLevel.setBlock(currentPos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);

                    // Выдаем выпавшие предметы игроку.
                    for (ItemStack drop : drops) {
                        // Добавляем предмет в инвентарь игрока.
                        player.getInventory().placeItemBackInInventory(drop);
                    }
                }
            }
        }
        // --- КОНЕЦ ИСПРАВЛЕНИЯ ---

        // После разрушения всех соседних блоков (всего 27, включая центральный), уменьшаем прочность инструмента на 2.
        // Это делается *вручную*, чтобы избежать стандартного уменьшения за каждый из разрушенных блоков.
        // Предполагается, что за разрушение *всех 27 блоков* (включая основной) должно уходить 2 прочности.
        // Стандартное уменьшение за основной блок *уже* произошло до вызова этого метода,
        // но мы компенсируем это, уменьшая *всего* на 2 за всю операцию.
        // hurtAndBreak уменьшает прочность и возвращает true, если инструмент сломался.
        // Этот метод принимает LivingEntity, что должно избежать ошибки.
        // player - это LivingEntity, поэтому это должно работать.
        itemstack.hurtAndBreak(2, player, (p) -> p.broadcastBreakEvent(EquipmentSlot.MAINHAND));
    }
}