package net.wowamod;

import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SuperFormServerHandler {

    // --- КОНФИГУРАЦИЯ ID (Namespace: universe3090) ---
    private static final String MOD_ID = "universe3090";
    
    private static final ResourceLocation SUPER_FORM_EFFECT_ID = new ResourceLocation(MOD_ID, "super_form");
    private static final ResourceLocation RING_ID = new ResourceLocation(MOD_ID, "ring");
    
    // Все 7 изумрудов хаоса
    private static final ResourceLocation RED_EMERALD_ID = new ResourceLocation(MOD_ID, "redemerald");
    private static final ResourceLocation GREEN_EMERALD_ID = new ResourceLocation(MOD_ID, "greenemerald");
    private static final ResourceLocation BLUE_EMERALD_ID = new ResourceLocation(MOD_ID, "blueemerald");
    private static final ResourceLocation LIGHTBLUE_EMERALD_ID = new ResourceLocation(MOD_ID, "lightblueemerald");
    private static final ResourceLocation PURPLE_EMERALD_ID = new ResourceLocation(MOD_ID, "purpleemerald");
    private static final ResourceLocation WHITE_EMERALD_ID = new ResourceLocation(MOD_ID, "whiteemerald");
    private static final ResourceLocation YELLOW_EMERALD_ID = new ResourceLocation(MOD_ID, "yellowemerald");

    // UUID для атрибутов 
    private static final UUID SPEED_MOD_UUID = UUID.fromString("a8b8c8d8-e8f8-48a8-b8c8-d8e8f8a8b8c8");
    private static final UUID DAMAGE_MOD_UUID = UUID.fromString("b9c9d9e9-f9a9-59b9-c9d9-e9f9a9b9c9d9");

    // Хранилище состояния полета до активации (чтобы вернуть как было)
    private static final Map<UUID, Boolean> previousFlightState = new HashMap<>();

    public SuperFormServerHandler() {
    }

    // --- СОБЫТИЯ ---

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) {
            return;
        }

        if (event.player instanceof ServerPlayer player) {
            handleSuperFormTick(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            deactivateSuperForm(player, false); 
            previousFlightState.remove(player.getUUID());
        }
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("superform")
            .then(Commands.literal("toggle")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    toggleSuperForm(player);
                    return 1;
                })
            )
        );
    }

    // --- ЛОГИКА ---

    public static void toggleSuperForm(ServerPlayer player) {
        MobEffect superFormEffect = BuiltInRegistries.MOB_EFFECT.get(SUPER_FORM_EFFECT_ID);
        if (superFormEffect == null) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cЭффект Super Form не найден в игре!"));
            return;
        }

        if (player.hasEffect(superFormEffect)) {
            deactivateSuperForm(player, true);
        } else {
            if (canActivate(player)) {
                activateSuperForm(player);
            } else {
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cНедостаточно ресурсов (Нужно 50 Колец и все 7 Изумрудов Хаоса)"));
            }
        }
    }

    private static boolean canActivate(ServerPlayer player) {
        int rings = getItemCount(player, BuiltInRegistries.ITEM.get(RING_ID));
        
        int red = getItemCount(player, BuiltInRegistries.ITEM.get(RED_EMERALD_ID));
        int green = getItemCount(player, BuiltInRegistries.ITEM.get(GREEN_EMERALD_ID));
        int blue = getItemCount(player, BuiltInRegistries.ITEM.get(BLUE_EMERALD_ID));
        int lightblue = getItemCount(player, BuiltInRegistries.ITEM.get(LIGHTBLUE_EMERALD_ID));
        int purple = getItemCount(player, BuiltInRegistries.ITEM.get(PURPLE_EMERALD_ID));
        int white = getItemCount(player, BuiltInRegistries.ITEM.get(WHITE_EMERALD_ID));
        int yellow = getItemCount(player, BuiltInRegistries.ITEM.get(YELLOW_EMERALD_ID));

        return rings >= 50 && red >= 1 && green >= 1 && blue >= 1 && lightblue >= 1 && purple >= 1 && white >= 1 && yellow >= 1;
    }

    private static void activateSuperForm(ServerPlayer player) {
        MobEffect superFormEffect = BuiltInRegistries.MOB_EFFECT.get(SUPER_FORM_EFFECT_ID);
        if (superFormEffect == null) return;

        previousFlightState.put(player.getUUID(), player.getAbilities().mayfly);

        player.addEffect(new MobEffectInstance(superFormEffect, 600, 0, false, false, true));
        // ДОБАВЛЕНО: Ванильное свечение (чтобы видеть сквозь стены). false параметры скрывают партиклы зелья.
        player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 600, 0, false, false, false));
        
        applySuperFormAttributes(player);
        
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§6§lСУПЕРФОРМА АКТИВИРОВАНА!"));
        player.level().playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    private static void handleSuperFormTick(ServerPlayer player) {
        MobEffect superFormEffect = BuiltInRegistries.MOB_EFFECT.get(SUPER_FORM_EFFECT_ID);
        if (superFormEffect == null) return;

        if (player.hasEffect(superFormEffect)) {
            Item ringItem = BuiltInRegistries.ITEM.get(RING_ID);
            int rings = getItemCount(player, ringItem);

            if (rings > 0) {
                if (player.tickCount % 20 == 0) {
                    consumeItem(player, ringItem, 1);
                    player.addEffect(new MobEffectInstance(superFormEffect, 30, 0, false, false, true)); 
                    player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 30, 0, false, false, false)); // Поддерживаем свечение
                }
                
                applySuperFormAttributes(player);
                performSuperMechanics(player);
            } else {
                deactivateSuperForm(player, true);
            }
        } else {
            removeSuperFormAttributes(player);
            if (player.gameMode.getGameModeForPlayer() != net.minecraft.world.level.GameType.CREATIVE) {
                player.getAbilities().flying = false;
                player.getAbilities().mayfly = false;
                player.onUpdateAbilities();
            }
        }
    }

    private static void deactivateSuperForm(ServerPlayer player, boolean sendMessage) {
        MobEffect superFormEffect = BuiltInRegistries.MOB_EFFECT.get(SUPER_FORM_EFFECT_ID);
        if (superFormEffect != null) {
            player.removeEffect(superFormEffect);
        }
        player.removeEffect(MobEffects.GLOWING); // Убираем свечение сквозь стены

        removeSuperFormAttributes(player);

        boolean wasAbleToFly = previousFlightState.getOrDefault(player.getUUID(), false);
        if (player.gameMode.getGameModeForPlayer() != net.minecraft.world.level.GameType.CREATIVE) {
            player.getAbilities().flying = false;
            player.getAbilities().mayfly = wasAbleToFly; 
        } else {
            player.getAbilities().mayfly = true; 
        }
        
        player.onUpdateAbilities();
        player.setMaxUpStep(0.6f); 
        player.setInvulnerable(false); 

        previousFlightState.remove(player.getUUID());

        if (sendMessage) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cСУПЕРФОРМА ДЕАКТИВИРОВАНА (Кольца закончились)"));
            player.level().playSound(null, player.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.0f, 1.0f);
        }
    }

    private static void applySuperFormAttributes(ServerPlayer player) {
        player.getAbilities().mayfly = true;
        player.getAbilities().flying = true;
        player.onUpdateAbilities();
        player.setInvulnerable(true);
        player.setMaxUpStep(1.5f);

        removeSuperFormAttributes(player);

        if (player.getAttribute(Attributes.MOVEMENT_SPEED) != null) {
            player.getAttribute(Attributes.MOVEMENT_SPEED).addTransientModifier(
                new AttributeModifier(SPEED_MOD_UUID, "super_form_speed", 1.0, AttributeModifier.Operation.MULTIPLY_TOTAL)
            );
        }
        if (player.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            player.getAttribute(Attributes.ATTACK_DAMAGE).addTransientModifier(
                new AttributeModifier(DAMAGE_MOD_UUID, "super_form_damage", 20.0, AttributeModifier.Operation.ADDITION)
            );
        }
    }

    private static void removeSuperFormAttributes(ServerPlayer player) {
        if (player.getAttribute(Attributes.MOVEMENT_SPEED) != null && player.getAttribute(Attributes.MOVEMENT_SPEED).getModifier(SPEED_MOD_UUID) != null) {
            player.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(SPEED_MOD_UUID);
        }
        if (player.getAttribute(Attributes.ATTACK_DAMAGE) != null && player.getAttribute(Attributes.ATTACK_DAMAGE).getModifier(DAMAGE_MOD_UUID) != null) {
            player.getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(DAMAGE_MOD_UUID);
        }
    }

    private static void performSuperMechanics(ServerPlayer player) {
        Level level = player.level();
        
        // 1. Урон по области при приближении
        double range = 3.0;
        AABB aabb = player.getBoundingBox().inflate(range);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, aabb, e -> e != player && !e.isAlliedTo(player));
        
        for (LivingEntity entity : entities) {
            if (player.distanceTo(entity) < range) {
                entity.hurt(player.damageSources().playerAttack(player), 5.0f);
                double dx = entity.getX() - player.getX();
                double dz = entity.getZ() - player.getZ();
                entity.push(dx, 0.5, dz);
            }
        }

        // 2. Уничтожение блоков в радиусе вокруг игрока
        BlockPos center = player.blockPosition();
        
        // Цикл ломает всё в радиусе 1 блока (от -1 до 1 по X и Z)
        // И по высоте от блока под ногами (-1) до блока над головой (2)
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 2; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    destroyWeakBlock(level, center.offset(dx, dy, dz));
                }
            }
        }
    }

    private static void destroyWeakBlock(Level level, BlockPos pos) {
        if (!level.isClientSide()) {
            BlockState state = level.getBlockState(pos);
            
            // Получаем прочность блока
            // 0.0 - листва/трава, 1.5 - камень, 2.0 - дерево, 3.0 - руды, 5.0 - железный блок, 50.0 - обсидиан
            float hardness = state.getDestroySpeed(level, pos);
            
            // Если прочность в пределах от 0 до 5.0 (будет ломать почти всё, кроме обсидиана и бедрока)
            if (!state.is(Blocks.BEDROCK) && !state.is(BlockTags.WITHER_IMMUNE) && hardness >= 0 && hardness <= 5.0f) {
                
                // true = выпадает дроп, и автоматически играет ванильный звук ломания блока 
                level.destroyBlock(pos, true);
            }
        }
    }

    private static int getItemCount(Player player, Item item) {
        if (item == null) return 0;
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() == item) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static void consumeItem(Player player, Item item, int amount) {
        if (item == null) return;
        int remaining = amount;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() == item) {
                int take = Math.min(stack.getCount(), remaining);
                stack.shrink(take);
                remaining -= take;
                if (remaining <= 0) break;
            }
        }
    }
}