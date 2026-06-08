package net.wowamod.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EmeraldShovelItem extends ShovelItem {

    // Настройки энергии
    public static final int MAX_ENERGY = 100000;
    public static final int COST_NORMAL = 50;
    public static final int COST_FAST = 150;
    public static final int COST_3X3 = 100; // за каждый блок в радиусе 3х3

    // Названия режимов
    private static final String[] MODES = {"Обычный (1x1)", "Быстрый (1x1)", "Экскаватор (3x3)"};

    public EmeraldShovelItem() {
        super(new Tier() {
            public int getUses() { return 4096; }
            public float getSpeed() { return 15f; }
            public float getAttackDamageBonus() { return 3f; }
            public int getLevel() { return 6; }
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
            int currentMode = tag.getInt("ShovelMode");
            int nextMode = (currentMode + 1) % 3;
            tag.putInt("ShovelMode", nextMode);

            player.displayClientMessage(Component.literal("Режим лопаты: " + MODES[nextMode]).withStyle(ChatFormatting.GREEN), true);
        }
        return InteractionResultHolder.success(stack);
    }

    // --- СКОРОСТЬ КОПАНИЯ (Зависит от энергии и Быстрого режима) ---
    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        IEnergyStorage energy = stack.getCapability(ForgeCapabilities.ENERGY).orElse(null);
        int currentEnergy = energy != null ? energy.getEnergyStored() : 0;
        int mode = stack.hasTag() ? stack.getTag().getInt("ShovelMode") : 0;

        int requiredEnergy = switch (mode) {
            case 1 -> COST_FAST;
            case 2 -> COST_3X3;
            default -> COST_NORMAL;
        };

        // Если энергии нет, блокируем скорость (чтобы не копал)
        if (currentEnergy < requiredEnergy) return 0.1f;

        float baseSpeed = super.getDestroySpeed(stack, state);

        // Если блок копается лопатой (скорость > 1) и включен Быстрый режим
        if (baseSpeed > 1.0f && mode == 1) {
            return baseSpeed * 2.5f; // Ускоряем в 2.5 раза
        }
        return baseSpeed;
    }

    // --- ЗАПРЕТ НА ПОЛУЧЕНИЕ ДРОПА БЕЗ ЭНЕРГИИ ---
    @Override
    public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, Player player) {
        IEnergyStorage energy = itemstack.getCapability(ForgeCapabilities.ENERGY).orElse(null);
        int mode = itemstack.hasTag() ? itemstack.getTag().getInt("ShovelMode") : 0;
        
        int cost = switch (mode) {
            case 1 -> COST_FAST;
            case 2 -> COST_3X3;
            default -> COST_NORMAL;
        };

        // Отменяем событие ломания блока, если энергии нет
        return energy == null || energy.getEnergyStored() < cost;
    }

    // --- ЛОГИКА РАЗРУШЕНИЯ И ЭКСКАВАТОР (3х3) ---
    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        if (level.isClientSide || !(entity instanceof Player player)) return true; // ИСПРАВЛЕНИЕ: безопасный возврат true

        IEnergyStorage energy = stack.getCapability(ForgeCapabilities.ENERGY).orElse(null);
        if (energy == null) return false;

        int mode = stack.hasTag() ? stack.getTag().getInt("ShovelMode") : 0;

        // Обработка обычных режимов (0 и 1)
        if (mode != 2) {
            int cost = mode == 1 ? COST_FAST : COST_NORMAL;
            energy.extractEnergy(cost, false); // ИСПРАВЛЕНИЕ: Убран каст к (EnergyStorage)
            return true; // ИСПРАВЛЕНИЕ: БОЛЬШЕ НЕ ВЫЗЫВАЕМ super.mineBlock!
        }

        // --- ОБРАБОТКА ЭКСКАВАТОРА 3х3 ---
        // Списываем энергию за первый блок
        energy.extractEnergy(COST_3X3, false); // ИСПРАВЛЕНИЕ: Убран каст к (EnergyStorage)

        // Трассировка луча (Raycast), чтобы понять, на какую сторону блока смотрит игрок
        Vec3 eyePos = player.getEyePosition(1.0f);
        Vec3 viewVec = player.getViewVector(1.0f);
        Vec3 rayEnd = eyePos.add(viewVec.x * 5.0, viewVec.y * 5.0, viewVec.z * 5.0);
        BlockHitResult result = level.clip(new ClipContext(eyePos, rayEnd, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        
        Direction hitFace = result.getDirection();

        // Ломаем 8 соседних блоков
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue; // Центр ломается сам

                BlockPos targetPos = switch (hitFace.getAxis()) {
                    case Y -> pos.offset(i, 0, j); // Копаем сверху/снизу (X-Z)
                    case X -> pos.offset(0, i, j); // Копаем стену сбоку (Y-Z)
                    case Z -> pos.offset(i, j, 0); // Копаем стену спереди (X-Y)
                };

                BlockState targetState = level.getBlockState(targetPos);

                // Защита: Убеждаемся, что блок - это земля/песок/гравий (скорость лопаты на нем > 1.0f)
                // И что это не неразрушимый блок (getDestroySpeed >= 0)
                if (stack.getDestroySpeed(targetState) > 1.0f && targetState.getDestroySpeed(level, targetPos) >= 0) {
                    if (energy.getEnergyStored() >= COST_3X3) {
                        energy.extractEnergy(COST_3X3, false); // ИСПРАВЛЕНИЕ: Убран каст к (EnergyStorage)

                        BlockEntity blockEntity = targetState.hasBlockEntity() ? level.getBlockEntity(targetPos) : null;
                        Block.dropResources(targetState, level, targetPos, blockEntity, player, stack);
                        level.destroyBlock(targetPos, false);
                    }
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
        int mode = stack.hasTag() ? stack.getTag().getInt("ShovelMode") : 0;

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