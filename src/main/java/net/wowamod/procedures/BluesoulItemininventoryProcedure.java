package net.wowamod.procedures;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

// Интерфейс для хранения состояния прыжков
interface IDoubleJumpCapability {
    int getJumpsLeft();
    void setJumpsLeft(int jumps);
    void resetJumps();
    void useJump();
    boolean canJump();
}

// Реализация интерфейса
class DoubleJumpCapability implements IDoubleJumpCapability {
    private int jumpsLeft;
    private static final int MAX_JUMPS = 2; // Два прыжка: обычный и двойной
    
    public DoubleJumpCapability() {
        this.jumpsLeft = 1; // Игрок может использовать двойной прыжок после первого прыжка
    }
    
    public DoubleJumpCapability(int jumpsLeft) {
        this.jumpsLeft = jumpsLeft;
    }
    
    @Override
    public int getJumpsLeft() {
        return jumpsLeft;
    }
    
    @Override
    public void setJumpsLeft(int jumps) {
        this.jumpsLeft = Math.max(0, Math.min(jumps, MAX_JUMPS));
    }
    
    @Override
    public void resetJumps() {
        this.jumpsLeft = 1; // После приземления игрок получает один дополнительный прыжок
    }
    
    @Override
    public void useJump() {
        if (jumpsLeft > 0) {
            jumpsLeft--;
        }
    }
    
    @Override
    public boolean canJump() {
        return jumpsLeft > 0;
    }
}

// Провайдер Capability
class DoubleJumpProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static final Capability<IDoubleJumpCapability> DOUBLE_JUMP_CAPABILITY =
            CapabilityManager.get(new CapabilityToken<>() {});
    
    private final IDoubleJumpCapability capability = new DoubleJumpCapability();
    private final LazyOptional<IDoubleJumpCapability> optional = LazyOptional.of(() -> capability);
    
    @Override
    public @Nonnull <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == DOUBLE_JUMP_CAPABILITY) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }
    
    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("jumpsLeft", capability.getJumpsLeft());
        return tag;
    }
    
    @Override
    public void deserializeNBT(CompoundTag tag) {
        capability.setJumpsLeft(tag.getInt("jumpsLeft"));
    }
}

// Обработчик событий двойного прыжка
@EventBusSubscriber(modid = "universe3090", bus = Bus.FORGE)
class DoubleJumpHandler {
    
    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity() instanceof Player player) {
            LazyOptional<IDoubleJumpCapability> capability = player.getCapability(DoubleJumpProvider.DOUBLE_JUMP_CAPABILITY);
            capability.ifPresent(doubleJump -> {
                // Проверяем, есть ли у игрока предмет bluesoul в инвентаре
                boolean hasDoubleJumpItem = hasBluesoulItem(player);
                
                if (doubleJump.canJump() && hasDoubleJumpItem) {
                    // Используем прыжок
                    doubleJump.useJump();
                    
                    // Добавляем эффект второго прыжка - увеличиваем импульс прыжка
                    player.setDeltaMovement(player.getDeltaMovement().x, 0.42F, player.getDeltaMovement().z);
                    
                    // Воспроизводим звук двойного прыжка
                    Level level = player.level();
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.ELYTRA_FLYING, SoundSource.PLAYERS, 0.5F, 1.0F);
                }
            });
        }
    }
    
    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (event.getEntity() instanceof Player player) {
            LazyOptional<IDoubleJumpCapability> capability = player.getCapability(DoubleJumpProvider.DOUBLE_JUMP_CAPABILITY);
            capability.ifPresent(doubleJump -> {
                // Сбрасываем количество прыжков при приземлении
                doubleJump.resetJumps();
            });
        }
    }
    
    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Player player) {
            // Убеждаемся, что у игрока есть capability при входе в мир
            LazyOptional<IDoubleJumpCapability> capability = player.getCapability(DoubleJumpProvider.DOUBLE_JUMP_CAPABILITY);
            if (!capability.isPresent()) {
                player.reviveCaps();
                player.getCapability(DoubleJumpProvider.DOUBLE_JUMP_CAPABILITY).ifPresent(doubleJump -> {
                    doubleJump.resetJumps();
                });
            }
        }
    }
    
    // Вспомогательный метод для проверки наличия предмета bluesoul
    private static boolean hasBluesoulItem(Player player) {
        // Проверяем в руках
        if (isBluesoulItem(player.getMainHandItem()) || isBluesoulItem(player.getOffhandItem())) {
            return true;
        }
        
        // Проверяем в броне
        for (int i = 0; i < player.getInventory().armor.size(); i++) {
            if (isBluesoulItem(player.getInventory().getArmor(i))) {
                return true;
            }
        }
        
        return false;
    }
    
    // Вспомогательный метод для проверки конкретного предмета
    private static boolean isBluesoulItem(ItemStack stack) {
        // Проверка по названию отображения предмета
        return stack != null && stack.getItem() != null && 
               stack.getDisplayName().getString().toLowerCase().contains("bluesoul");
    }
}

// Обработчик прикрепления capability к сущностям
@EventBusSubscriber(modid = "universe3090", bus = Bus.FORGE)
class CapabilityAttacher {
    
    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(new ResourceLocation("universe3090", "double_jump"), new DoubleJumpProvider());
        }
    }
}

// Регистрация capability в моде
@EventBusSubscriber(modid = "universe3090", bus = Bus.MOD)
class WowaModCapabilityRegistration {
    
    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(IDoubleJumpCapability.class);
    }
}

// Обновленная процедура для интеграции с предметом bluesoul
public class BluesoulItemininventoryProcedure {
    public static void execute(Entity entity, ItemStack itemstack) {
        if (entity == null)
            return;
        if (!(entity instanceof Player _plrCldCheck1 && _plrCldCheck1.getCooldowns().isOnCooldown(itemstack.getItem()))) {
            if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
                _entity.addEffect(new MobEffectInstance(MobEffects.JUMP, 60, 1, false, false));
            if (entity instanceof Player _player) {
                _player.getCooldowns().addCooldown(itemstack.getItem(), 60);
                
                // Добавляем возможность двойного прыжка при наличии предмета в инвентаре
                LazyOptional<IDoubleJumpCapability> capability = _player.getCapability(DoubleJumpProvider.DOUBLE_JUMP_CAPABILITY);
                capability.ifPresent(doubleJump -> {
                    // Игрок может использовать двойной прыжок, если предмет в инвентаре
                    doubleJump.resetJumps(); // Обновляем количество прыжков
                });
            }
        }
    }
}