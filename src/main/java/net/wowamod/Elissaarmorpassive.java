package net.wowamod;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent; // Импорт остался, если используется где-то ещё
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.lwjgl.glfw.GLFW; // Импорт GLFW остался, так как он может пригодиться в будущем, но не используется в этом фрагменте
import net.wowamod.init.Universe3090ModItems;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;
import java.util.UUID;

// --- CAPABILITY INTERFACE AND IMPLEMENTATION (Moved to top level) ---
@AutoRegisterCapability
interface IExtraHeartsCapability {
    int getExtraHearts();
    void setExtraHearts(int hearts);
}

class ExtraHeartsCapabilityImpl implements IExtraHeartsCapability {
    private int extraHearts = 0;
    @Override public int getExtraHearts() { return extraHearts; }
    @Override public void setExtraHearts(int hearts) { this.extraHearts = hearts; }
}

class ExtraHeartsProvider implements ICapabilityProvider {
    private final LazyOptional<IExtraHeartsCapability> optional = LazyOptional.of(ExtraHeartsCapabilityImpl::new);
    @Nonnull @Override public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return Elissaarmorpassive.EXTRA_HEARTS_CAPABILITY == cap ? optional.cast() : LazyOptional.empty();
    }
}

public class Elissaarmorpassive {

    public static final Capability<IExtraHeartsCapability> EXTRA_HEARTS_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
    public static final DeferredRegister<?> MOB_EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, "universe3090");

	// UUIDs для модификаторов атрибутов, чтобы избежать дублирования
	private static final UUID HEALTH_MODIFIER_UUID = UUID.fromString("a1b64a5c-9b0d-e2f1-a1b2-c3d4e5f6a7b8");
	private static final UUID SPEED_MODIFIER_UUID = UUID.fromString("b2c74b6d-8c1e-f3f2-b2c3-d4e5f6a7b8c9"); // 'g' -> 'f'
	private static final UUID JUMP_MODIFIER_UUID = UUID.fromString("c3d84c7e-7d2f-f4f3-c3d4-e5f6a7b8c9d0"); // 'g' -> 'f'

    @Mod.EventBusSubscriber(modid = "universe3090", bus = Mod.EventBusSubscriber.Bus.FORGE)
	public static class ElissaarmorEvents {
	
	    // --- ЛОГИКА АТРИБУТОВ И ЭФФЕКТОВ ---
	
	    @SubscribeEvent
	    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
	        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide) return;
	
	        // Получаем предметы из нужных слотов
	        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
	        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
	        ItemStack leggings = player.getItemBySlot(EquipmentSlot.LEGS);
	        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
	
	        // Получаем предметы из нашего мода для сравнения
	        Item elissaHelmet = Universe3090ModItems.ELISSAARMOR_HELMET.get();
	        Item elissaChestplate = Universe3090ModItems.ELISSAARMOR_CHESTPLATE.get();
	        Item elissaLeggings = Universe3090ModItems.ELISSAARMOR_LEGGINGS.get();
	        Item elissaBoots = Universe3090ModItems.ELISSAARMOR_BOOTS.get();
	
	        // --- НАГРУДНИК: Доп. сердца и медленное падение ---
	        handleAttributeModifier(player, chestplate.getItem() == elissaChestplate, Attributes.MAX_HEALTH, HEALTH_MODIFIER_UUID, "Elissa Chestplate Health", 20.0, AttributeModifier.Operation.ADDITION);
	        if (chestplate.getItem() == elissaChestplate) {
	            // Регенерация
	            if (player.level().getGameTime() % (20 * 15) == 0) { // Каждые 10 секунд
	                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0)); // Реген I на 5 сек (восстановит 2 сердца)
	            }
	        }
	        
	        // --- ПОНОЖИ: Скорость и анти-замедление ---
	        handleAttributeModifier(player, leggings.getItem() == elissaLeggings, Attributes.MOVEMENT_SPEED, SPEED_MODIFIER_UUID, "Elissa Leggings Speed", 0.35, AttributeModifier.Operation.MULTIPLY_TOTAL);
	        if (leggings.getItem() == elissaLeggings) {
	            if (player.level().getBlockState(player.blockPosition().below()).is(Blocks.SOUL_SAND) || player.level().getBlockState(player.blockPosition().below()).is(Blocks.SOUL_SOIL)) {
	                if (player.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
	                    player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
	                }
	            }
	        }
	
	        // --- БОТИНКИ: Сила прыжка ---
	        if (boots.getItem() == elissaBoots) {
	            player.addEffect(new MobEffectInstance(MobEffects.JUMP, 41, 1, false, false, true)); // Прыжок II (усилитель 1) на 2 секунды
	        }
	    }
	    
	    // Вспомогательный метод для добавления/удаления модификаторов
	    private static void handleAttributeModifier(Player player, boolean shouldApply, Attribute attribute, UUID uuid, String name, double value, AttributeModifier.Operation op) {
	        var attributeInstance = player.getAttribute(attribute);
	        if (attributeInstance == null) return;
	
	        var modifier = new AttributeModifier(uuid, name, value, op);
	        
	        if (shouldApply) {
	            if (!attributeInstance.hasModifier(modifier)) {
	                attributeInstance.addPermanentModifier(modifier);
	            }
	        } else {
	            if (attributeInstance.hasModifier(modifier)) {
	                attributeInstance.removeModifier(uuid);
	            }
	        }
	    }
	    
	    // --- БОТИНКИ: Отмена урона от падения ---
	    @SubscribeEvent
	    public static void onLivingFall(LivingFallEvent event) {
	        if (event.getEntity() instanceof Player player) {
	            if (player.getItemBySlot(EquipmentSlot.FEET).getItem() == Universe3090ModItems.ELISSAARMOR_BOOTS.get()) {
	                event.setCanceled(true);
	            }
	        }
	    }
	
	    // --- ПОЛНЫЙ СЕТ: Поглощение урона ---
	    @SubscribeEvent
	    public static void onLivingHurt(LivingHurtEvent event) {
	        if (event.getEntity() instanceof Player player && isFullSetEquipped(player)) {
	            float damage = event.getAmount();
	            float reductionPercentage = 0.50F + (0.10F * (float) Math.random()); // 55% to 70%
	            event.setAmount(damage * (1.0F - reductionPercentage));
	        }
	    }
	
	    private static boolean isFullSetEquipped(Player player) {
	        return player.getItemBySlot(EquipmentSlot.HEAD).getItem() == Universe3090ModItems.ELISSAARMOR_HELMET.get() &&
	               player.getItemBySlot(EquipmentSlot.CHEST).getItem() == Universe3090ModItems.ELISSAARMOR_CHESTPLATE.get() &&
	               player.getItemBySlot(EquipmentSlot.LEGS).getItem() == Universe3090ModItems.ELISSAARMOR_LEGGINGS.get() &&
	               player.getItemBySlot(EquipmentSlot.FEET).getItem() == Universe3090ModItems.ELISSAARMOR_BOOTS.get();
	    }
	
	    // --- CAPABILITIES ---
	    @SubscribeEvent
	    public static void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
	        if (event.getObject() instanceof Player) {
	            event.addCapability(new ResourceLocation("universe3090", "extra_hearts"), new ExtraHeartsProvider());
	        }
	    }
	
	    @SubscribeEvent
	    public static void onPlayerClone(PlayerEvent.Clone event) {
	        event.getOriginal().getCapability(EXTRA_HEARTS_CAPABILITY).ifPresent(oldCap -> {
	            event.getEntity().getCapability(EXTRA_HEARTS_CAPABILITY).ifPresent(newCap -> newCap.setExtraHearts(oldCap.getExtraHearts()));
	        });
	    }
	
	}

    // --- НОВЫЙ КЛАСС ДЛЯ КЛИЕНТСКИХ FORGE СОБЫТИЙ ---
    @Mod.EventBusSubscriber(modid = "universe3090", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ElissaarmorClientForgeEvents {

        @SubscribeEvent
        @OnlyIn(Dist.CLIENT) // Убедимся, что метод помечен как клиентский
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            // Убедимся, что мы на фазе END тика
            if (event.phase != TickEvent.Phase.END) return;

            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;

            // --- ШЛЕМ: Логика свечения сущностей ---
            // Вызываем метод из клиентского класса
            Elissaarmorpassive.ElissaarmorClientEvents.handleHelmetGlow(mc.player);
        }
    }

    @Mod.EventBusSubscriber(modid = "universe3090", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ElissaarmorClientEvents {
        
        // Список сущностей, которым мы включили свечение - ТЕПЕРЬ ПРАВИЛЬНО НАХОДИТСЯ ЗДЕСЬ
        private static final Set<Entity> glowingEntities = Sets.newHashSet();
        
        @OnlyIn(Dist.CLIENT)
        public static void handleHelmetGlow(Player player) { // Сделан public static
            boolean wearingHelmet = player.getItemBySlot(EquipmentSlot.HEAD).getItem() == Universe3090ModItems.ELISSAARMOR_HELMET.get();
            
            // Если шлем не надет, выключаем свечение у всех, кого мы подсветили ранее
            if (!wearingHelmet) {
                if (!glowingEntities.isEmpty()) {
                    glowingEntities.forEach(e -> e.setGlowingTag(false)); // Исправлено: используем публичный метод
                    glowingEntities.clear();
                }
                return;
            }

            // Создаем новый сет для подсвеченных в этом тике
            Set<Entity> currentlySeenEntities = Sets.newHashSet();
            List<Entity> nearbyEntities = player.level().getEntities(player, player.getBoundingBox().inflate(32.0), e -> e instanceof LivingEntity && e != player);

            for (Entity entity : nearbyEntities) {
                entity.setGlowingTag(true); // Исправлено: используем публичный метод
                currentlySeenEntities.add(entity);
            }

            // Выключаем свечение у тех, кто вышел из радиуса
            for (Entity oldEntity : glowingEntities) {
                if (!currentlySeenEntities.contains(oldEntity)) {
                    oldEntity.setGlowingTag(false); // Исправлено: используем публичный метод
                }
            }

            // Обновляем наш основной сет
            glowingEntities.clear();
            glowingEntities.addAll(currentlySeenEntities);
        }
    }
}