package net.wowamod.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// НОВЫЕ ИМПОРТЫ ДЛЯ СВЕТОВЫХ И ЗВУКОВЫХ ЭФФЕКТОВ
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class EmeraldAxeItem extends AxeItem {

    // Настройки энергии
    public static final int MAX_ENERGY = 100000;
    public static final int COST_NORMAL = 50;
    public static final int COST_FAST = 100;
    // 50 * 1.05 = 52.5 (Округляем до 53 FE за каждое сломанное бревно)
    public static final int COST_TREECAP = 53; 

    // Лимит блоков для Треекапитатора (чтобы не повесить сервер на гигантских деревьях)
    private static final int MAX_TREE_SIZE = 512;

    private static final String[] MODES = {"Обычный", "Быстрый", "Треекапитатор"};

    public EmeraldAxeItem() {
        super(new Tier() {
            public int getUses() { return 4096; }
            public float getSpeed() { return 4f; }
            public float getAttackDamageBonus() { return 8f; }
            public int getLevel() { return 1; }
            public int getEnchantmentValue() { return 30; }
            public Ingredient getRepairIngredient() { return Ingredient.of(); }
        }, 1, -3f, new Item.Properties().fireResistant().stacksTo(1));
    }

    // --- СМЕНА РЕЖИМА (Shift + ПКМ) ---
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player.isShiftKeyDown()) {
            CompoundTag tag = stack.getOrCreateTag();
            int currentMode = tag.getInt("AxeMode");
            int nextMode = (currentMode + 1) % 3;
            tag.putInt("AxeMode", nextMode);

            player.displayClientMessage(Component.literal("Режим топора: " + MODES[nextMode]).withStyle(ChatFormatting.GREEN), true);
        }
        return InteractionResultHolder.success(stack);
    }

    // --- СКОРОСТЬ КОПАНИЯ (Листва и Быстрый режим) ---
    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        IEnergyStorage energy = stack.getCapability(ForgeCapabilities.ENERGY).orElse(null);
        int currentEnergy = energy != null ? energy.getEnergyStored() : 0;
        int mode = stack.hasTag() ? stack.getTag().getInt("AxeMode") : 0;

        int requiredEnergy = switch (mode) {
            case 1 -> COST_FAST;
            case 2 -> COST_TREECAP;
            default -> COST_NORMAL;
        };

        // Без энергии скорость падает почти до нуля
        if (currentEnergy < requiredEnergy) return 0.1f;

        float baseSpeed = super.getDestroySpeed(stack, state);

        if (mode == 1) { // Быстрый режим
            if (state.is(BlockTags.LEAVES)) {
                return 10000.0f; // Моментальное разрушение листвы
            }
            if (baseSpeed > 1.0f) {
                return baseSpeed * 6.0f; // ИСПРАВЛЕНИЕ: ускорено с 1.5x до 6.0x
            }
        }
        return baseSpeed;
    }

    // --- ЗАПРЕТ НА ПОЛУЧЕНИЕ ДРОПА БЕЗ ЭНЕРГИИ ---
    @Override
    public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, Player player) {
        IEnergyStorage energy = itemstack.getCapability(ForgeCapabilities.ENERGY).orElse(null);
        int mode = itemstack.hasTag() ? itemstack.getTag().getInt("AxeMode") : 0;
        int cost = mode == 1 ? COST_FAST : (mode == 2 ? COST_TREECAP : COST_NORMAL);

        // Отменяем ломание блока, если энергии не хватает
        return energy == null || energy.getEnergyStored() < cost;
    }

    // --- ЛОГИКА РАЗРУШЕНИЯ БЛОКОВ И ТРЕЕКАПИТАТОР ---
    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        if (level.isClientSide || !(entity instanceof Player player)) return true; // безопасный возврат true

        IEnergyStorage energy = stack.getCapability(ForgeCapabilities.ENERGY).orElse(null);
        if (energy == null) return false;

        int mode = stack.hasTag() ? stack.getTag().getInt("AxeMode") : 0;

        // Если это не Треекапитатор ИЛИ блок не является бревном (например, ломаем доски)
        if (mode != 2 || !state.is(BlockTags.LOGS)) {
            int cost = mode == 1 ? COST_FAST : COST_NORMAL;
            energy.extractEnergy(cost, false); // Убран каст к (EnergyStorage)
            return true; // БОЛЬШЕ НЕ ВЫЗЫВАЕМ super.mineBlock!
        }

        // --- ЛОГИКА ТРЕЕКАПИТАТОРА (Сруб всего дерева) ---
        // Списываем энергию за первый блок
        energy.extractEnergy(COST_TREECAP, false); // Убран каст к (EnergyStorage)

        // Воспроизводим глубокий энерго-звук старта сруба
        level.playSound(null, pos, SoundEvents.CONDUIT_ACTIVATE, SoundSource.BLOCKS, 1.0F, 1.4F);

        // Алгоритм поиска в ширину (BFS) для поиска всех связанных бревен
        Queue<BlockPos> queue = new LinkedList<>();
        Set<BlockPos> visited = new HashSet<>();
        
        visited.add(pos); // Стартовый блок мы уже сломали

        // Добавляем соседей стартового блока в очередь
        for (BlockPos neighbor : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
            queue.add(neighbor.immutable());
            visited.add(neighbor.immutable());
        }

        int blocksBroken = 1;

        while (!queue.isEmpty() && blocksBroken < MAX_TREE_SIZE) {
            BlockPos currentPos = queue.poll();
            BlockState currentState = level.getBlockState(currentPos);

            // Если найденный блок - это бревно
            if (currentState.is(BlockTags.LOGS)) {
                // Проверяем, хватит ли энергии на следующий блок
                if (energy.getEnergyStored() >= COST_TREECAP) {
                    energy.extractEnergy(COST_TREECAP, false); // Убран каст к (EnergyStorage)

                    // Дропаем ресурсы и удаляем блок
                    BlockEntity be = currentState.hasBlockEntity() ? level.getBlockEntity(currentPos) : null;
                    Block.dropResources(currentState, level, currentPos, be, player, stack);
                    level.destroyBlock(currentPos, false);
                    
                    blocksBroken++;

                    // КАСТОМНЫЕ ЭФФЕКТЫ ДЛЯ КАЖДОГО СЛОМАННОГО БРЕВНА
                    if (level instanceof ServerLevel serverLevel) {
                        // 1. Зеленые изумрудные звездочки
                        serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, 
                            currentPos.getX() + 0.5, currentPos.getY() + 0.5, currentPos.getZ() + 0.5, 
                            4, 0.25, 0.25, 0.25, 0.05);
                        // 2. Голубые электрические разряды
                        serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, 
                            currentPos.getX() + 0.5, currentPos.getY() + 0.5, currentPos.getZ() + 0.5, 
                            3, 0.2, 0.2, 0.2, 0.15);
                    }
                    
                    // Энергетический хрустальный звук разрушения
                    level.playSound(null, currentPos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.5F, 1.2F + level.random.nextFloat() * 0.4F);

                    // Ищем соседей этого бревна (продолжаем поиск по стволу вверх)
                    for (BlockPos neighbor : BlockPos.betweenClosed(currentPos.offset(-1, -1, -1), currentPos.offset(1, 1, 1))) {
                        if (!visited.contains(neighbor)) {
                            queue.add(neighbor.immutable());
                            visited.add(neighbor.immutable());
                        }
                    }
                } else {
                    // Энергия закончилась, прекращаем поиск и сруб
                    break;
                }
            }
        }

        return true; // БОЛЬШЕ НЕ ВЫЗЫВАЕМ super.mineBlock!
    }

    // --- ДИНАМИЧЕСКИЙ ЭНЕРГО-ПРОВАЙДЕР БЕЗ КЭШИРОВАНИЯ ---
    public static class ItemEnergyStorage implements IEnergyStorage {
        private final ItemStack stack;
        private final int capacity;

        public ItemEnergyStorage(ItemStack stack, int capacity) {
            this.stack = stack;
            this.capacity = capacity;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int energy = getEnergyStored();
            int received = Math.min(this.capacity - energy, maxReceive);
            if (!simulate && received > 0) {
                setEnergy(energy + received);
            }
            return received;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            int energy = getEnergyStored();
            int extracted = Math.min(energy, maxExtract);
            if (!simulate && extracted > 0) {
                setEnergy(energy - extracted);
            }
            return extracted;
        }

        @Override
        public int getEnergyStored() {
            return this.stack.hasTag() ? this.stack.getTag().getInt("Energy") : 0;
        }

        @Override
        public int getMaxEnergyStored() {
            return this.capacity;
        }

        @Override
        public boolean canExtract() { return true; }

        @Override
        public boolean canReceive() { return true; }

        private void setEnergy(int energy) {
            this.stack.getOrCreateTag().putInt("Energy", energy);
        }
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ICapabilityProvider() {
            private final LazyOptional<IEnergyStorage> energyOptional = LazyOptional.of(() -> new ItemEnergyStorage(stack, MAX_ENERGY));

            @NotNull
            @Override
            public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
                if (cap == ForgeCapabilities.ENERGY) return energyOptional.cast();
                return LazyOptional.empty();
            }
        };
    }

    // --- ОТОБРАЖЕНИЕ В ТУЛТИПЕ ---
    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> list, TooltipFlag flag) {
        int energy = stack.hasTag() ? stack.getTag().getInt("Energy") : 0;
        int mode = stack.hasTag() ? stack.getTag().getInt("AxeMode") : 0;

        list.add(Component.literal("Энергия: " + energy + " / " + MAX_ENERGY + " FE").withStyle(ChatFormatting.AQUA));
        list.add(Component.literal("Режим: " + MODES[mode]).withStyle(ChatFormatting.GOLD));
        list.add(Component.literal("Shift + ПКМ для смены режима").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));

        super.appendHoverText(stack, level, list, flag);
    }

    // --- ОТКЛЮЧАЕМ ВАНИЛЬНУЮ ПРОЧНОСТЬ ---
    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return 0;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        return true; 
    }

    // --- ДЕЛАЕМ ПОЛОСКУ ЭНЕРГИИ ВМЕСТО ПРОЧНОСТИ ---
    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true; 
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return stack.getCapability(ForgeCapabilities.ENERGY)
                .map(e -> Math.round(13.0F * e.getEnergyStored() / (float) e.getMaxEnergyStored()))
                .orElse(0);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x00FFFF; // Голубой (Cyan)
    }
}