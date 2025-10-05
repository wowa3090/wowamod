package net.wowamod;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader; // Правильный импорт
import net.minecraft.util.RandomSource; // Для getExpDrop
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks; // Для получения опыта с блока
import net.wowamod.procedures.SoulSystemWProcedure;
import net.wowamod.procedures.SoulSystemWProcedure.ISoulCapability;
import net.wowamod.procedures.SoulSystemWProcedure.SoulType;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.InteractionHand;
// import net.minecraftforge.event.entity.projectile.ProjectileImpactEvent; // УДАЛЕНО: Событие не существует в 1.20.1

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.HashSet;
import java.util.Set;

// --- Основной класс мода, подписывается на события MOD EventBus ---
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class SoulsSystemWEffects {

    // Уникальные ID для атрибутных модификаторов
    private static final UUID DETERMINATION_DAMAGE_MODIFIER_ID = UUID.randomUUID();
    private static final UUID INTEGRITY_JUMP_MODIFIER_ID = UUID.randomUUID();
    // JUSTICE_RANGED_DAMAGE_MODIFIER_ID не существует, используем ATTACK_DAMAGE с условием
    // JUSTICE_MOB_DAMAGE_BOOST_ID для увеличения урона по конкретным мобам
    private static final UUID JUSTICE_MOB_DAMAGE_BOOST_ID = UUID.randomUUID();
    // JUSTICE_BOW_DAMAGE_BOOST_ID для временного увеличения урона при выстреле из лука/арбалета
    private static final UUID JUSTICE_BOW_DAMAGE_BOOST_ID = UUID.randomUUID();

    // Таймеры для эффектов
    private static final Map<UUID, Integer> kindnessRegenTimers = new HashMap<>();
    private static final Map<UUID, Integer> doubleJumpCooldowns = new HashMap<>();
    private static final Map<UUID, Boolean> hasJumped = new HashMap<>();
    // Таймер для временного увеличения урона от лука (Justice)
    private static final Map<UUID, Integer> justiceBowBoostTimers = new HashMap<>(); // Добавлено

    // Множество для проверки "злых" мобов - теперь public static final для доступа из внутреннего класса
    public static final Set<ResourceLocation> JUSTICE_TARGET_MOBS = new HashSet<>();
    static {
        JUSTICE_TARGET_MOBS.add(new ResourceLocation("minecraft", "vindicator"));
        JUSTICE_TARGET_MOBS.add(new ResourceLocation("minecraft", "pillager"));
        JUSTICE_TARGET_MOBS.add(new ResourceLocation("minecraft", "witch"));
    }

    public SoulsSystemWEffects() {
    }

    // --- Событие MOD EventBus: Инициализация ---
    @SubscribeEvent
    public static void init(FMLCommonSetupEvent event) {
        new SoulsSystemWEffects();
        // MinecraftForge.EVENT_BUS.register(SoulsSystemWEffects.class); // Больше не регистрируем весь класс на Forge EventBus здесь
    }

    // --- Внутренний класс для обработчиков MOD EventBus ---
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    private static class ModBusEvents {
        // Удален serverLoad из-за несовместимости ServerStartingEvent с MOD EventBus
        // @SubscribeEvent
        // public static void serverLoad(ServerStartingEvent event) {
        // }

        @OnlyIn(Dist.CLIENT)
        @SubscribeEvent
        public static void clientLoad(FMLClientSetupEvent event) {
            // Этот метод корректен, так как FMLClientSetupEvent реализует IModBusEvent
        }
    }

    // --- Методы для управления атрибутами души (вызываются из ForgeHandlers) ---
    // Метод для обновления эффектов при смене души
    private static void updateSoulEffects(ServerPlayer player) {
        SoulSystemWProcedure.getCapability(player).ifPresent(cap -> {
            SoulType currentSoul = cap.getCurrentSoul();
            boolean soulDetermined = cap.isSoulDetermined();

            // Удаляем все предыдущие атрибутные модификаторы
            removeSoulAttributes(player);

            if (soulDetermined) {
                // Применяем атрибуты в зависимости от души (кроме PATIENCE, BRAVERY, JUSTICE_RANGED)
                switch (currentSoul) {
                    case DETERMINATION:
                        applyDeterminationAttributes(player);
                        break;
                    case INTEGRITY:
                        applyIntegrityAttributes(player);
                        break;
                    case JUSTICE:
                        // Justice не добавляет постоянный атрибут для ranged, но может добавлять для mob damage boost
                        // Это делается в onLivingHurt, если игрок атакует моба
                        break;
                    // JUSTICE больше не использует атрибут для дальнего боя
                }
            }

            // Сбрасываем таймеры и состояния для пассивных эффектов
            if (!soulDetermined || currentSoul != SoulType.KINDNESS) {
                kindnessRegenTimers.remove(player.getUUID());
            } else {
                kindnessRegenTimers.put(player.getUUID(), 0); // Инициализируем таймер, если душа Kindness
            }
            if (!soulDetermined || currentSoul != SoulType.INTEGRITY) {
                doubleJumpCooldowns.remove(player.getUUID());
                hasJumped.remove(player.getUUID());
            }
            // Сбрасываем таймер для Justice Bow Boost
            if (!soulDetermined || currentSoul != SoulType.JUSTICE) {
                 justiceBowBoostTimers.remove(player.getUUID());
            }
        });
    }

    // Метод для удаления всех атрибутных модификаторов, связанных с душами
    private static void removeSoulAttributes(ServerPlayer player) {
        AttributeInstance attackDamage = player.getAttribute(Attributes.ATTACK_DAMAGE);
        AttributeInstance jumpStrength = player.getAttribute(Attributes.JUMP_STRENGTH);
        // Убираем rangedDamage из удаления

        if (attackDamage != null) {
            attackDamage.removeModifier(DETERMINATION_DAMAGE_MODIFIER_ID);
            attackDamage.removeModifier(JUSTICE_MOB_DAMAGE_BOOST_ID);
            attackDamage.removeModifier(JUSTICE_BOW_DAMAGE_BOOST_ID); // Удаляем временный модификатор
        }
        if (jumpStrength != null) {
            jumpStrength.removeModifier(INTEGRITY_JUMP_MODIFIER_ID);
        }
        // Убираем удаление rangedDamage
    }

    // Применение атрибутов
    private static void applyDeterminationAttributes(ServerPlayer player) {
        AttributeInstance attackDamage = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage != null) {
            // Увеличение урона на 15% (примерное среднее значение)
            attackDamage.addTransientModifier(new AttributeModifier(
                DETERMINATION_DAMAGE_MODIFIER_ID,
                "Soul Determination Damage Boost",
                0.15, // 15%
                AttributeModifier.Operation.MULTIPLY_BASE
            ));
        }
    }

    private static void applyIntegrityAttributes(ServerPlayer player) {
        AttributeInstance jumpStrength = player.getAttribute(Attributes.JUMP_STRENGTH);
        if (jumpStrength != null) {
            // Увеличение силы прыжка на 20%
            jumpStrength.addTransientModifier(new AttributeModifier(
                INTEGRITY_JUMP_MODIFIER_ID,
                "Soul Integrity Jump Boost",
                0.2, // 20%
                AttributeModifier.Operation.MULTIPLY_BASE
            ));
        }
    }

    // JUSTICE больше не использует атрибут для дальнего боя, убираем метод applyJusticeAttributes

    // --- Пассивные эффекты (вызываются из ForgeHandlers) ---
    private static void handleKindnessEffect(ServerPlayer player) {
        int timer = kindnessRegenTimers.getOrDefault(player.getUUID(), 0);
        timer++;
        if (timer >= 100) { // 100 тиков = 5 секунд
            if (player.getHealth() < player.getMaxHealth()) {
                player.heal(2.0f); // Регенерация 2 сердца
                // Визуальный эффект сердца (только для клиента)
                Level level = player.level();
                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(
                        ParticleTypes.HEART,
                        player.getX(), player.getY() + 1.5, player.getZ(),
                        1, 0.1, 0.1, 0.1, 0.0
                    );
                }
            }
            timer = 0;
        }
        kindnessRegenTimers.put(player.getUUID(), timer);
    }

    private static void handleIntegrityEffect(ServerPlayer player, PlayerTickEvent event) {
        // Проверка двойного прыжка - используем onGround() и флаг.
        if (player.onGround()) {
            hasJumped.put(player.getUUID(), false); // Сброс флага при приземлении
        }

        // Проверяем, пытается ли игрок прыгнуть. В PlayerTickEvent фазе END, игрок мог уже начать прыжок.
        // Мы не можем напрямую проверить нажатие клавиши здесь без дополнительного отслеживания.
        // Упрощённый подход: если игрок в воздухе, не летит, не падает быстро (deltaMovement.y > -0.08 примерно), и не прыгал до этого.
        // Но это менее надёжно. Используем флаг и предположение, что прыжок произошёл, если игрок в воздухе и не на земле.
        // Более точная реализация требует отслеживания нажатия клавиши прыжка в другом событии (например, InputEvent).
        // Для простоты, реализуем двойной прыжок как возможность прыгнуть ещё раз в воздухе, если флаг не установлен.
        // Проверим вертикальную скорость (грубо).
        boolean isRisingOrNeutralY = player.getDeltaMovement().y >= -0.08; // Не падает быстро
        boolean wantsToJump = player.getAbilities().mayfly || player.isFallFlying() || isRisingOrNeutralY; // Упрощённая проверка, не идеальная
        // Лучше использовать флаг, установленный при отрыве от земли или при сбросе onGround, если игрок пытается прыгнуть.
        // Попробуем упрощённо: если игрок в воздухе, не на земле, и флаг не установлен, считаем это первым прыжком.
        // Если флаг установлен и игрок в воздухе, проверяем кулдаун и возможность второго прыжка.
        if (!player.onGround() && !player.isFallFlying() && !player.getAbilities().flying) {
             if (hasJumped.getOrDefault(player.getUUID(), false)) {
                 // Проверяем кулдаун
                 int cooldown = doubleJumpCooldowns.getOrDefault(player.getUUID(), 0);
                 if (cooldown <= 0) {
                     // Выполняем "второй прыжок" с увеличенной высотой
                     AttributeInstance jumpAttribute = player.getAttribute(Attributes.JUMP_STRENGTH);
                     double baseJumpHeight = 0.42F; // Стандартная высота
                     double jumpMultiplier = jumpAttribute != null ? (1.0 + jumpAttribute.getValue()) : 1.0;
                     double jumpBoost = baseJumpHeight * 1.1; // Дополнительный импульс для двойного прыжка
                     player.setDeltaMovement(player.getDeltaMovement().x, jumpBoost, player.getDeltaMovement().z);
                     player.hasImpulse = true;
                     // hasJumped остаётся true после второго прыжка, пока не приземлится
                     doubleJumpCooldowns.put(player.getUUID(), 10); // Кулдаун 10 тиков
                 }
             } else if (!hasJumped.getOrDefault(player.getUUID(), true)) { // Первый прыжок (или момент отрыва)
                 // Устанавливаем флаг, как будто первый прыжок (отрыв от земли) уже произошёл.
                 // Это не идеально, так как игрок мог просто упасть.
                 // Более точная реализация требует отслеживания `player.wasOnGround` и `player.onGround` или нажатия клавиши.
                 // Для MCreator и простоты, можно считать, что если игрок в воздухе и флаг false, это начало воздушной фазы.
                 hasJumped.put(player.getUUID(), true);
             }
        } else if (player.onGround()) {
             // Если игрок на земле, сбрасываем флаг, если он был установлен в воздухе
             //hasJumped.put(player.getUUID(), false); // Уже сделано выше при onGround
        }

        // Обновление кулдауна
        int cooldown = doubleJumpCooldowns.getOrDefault(player.getUUID(), 0);
        if (cooldown > 0) {
            cooldown--;
            if (cooldown == 0) {
                doubleJumpCooldowns.remove(player.getUUID()); // Удаляем, если 0
            } else {
                doubleJumpCooldowns.put(player.getUUID(), cooldown);
            }
        }
    }

    private static void handleJusticeBowBoost(ServerPlayer player) {
        int timer = justiceBowBoostTimers.getOrDefault(player.getUUID(), 0);
        if (timer > 0) {
            timer--;
            if (timer <= 0) {
                justiceBowBoostTimers.remove(player.getUUID());
                // Удаляем временный модификатор урона
                AttributeInstance attackDamage = player.getAttribute(Attributes.ATTACK_DAMAGE);
                if (attackDamage != null) {
                    attackDamage.removeModifier(JUSTICE_BOW_DAMAGE_BOOST_ID);
                }
            } else {
                justiceBowBoostTimers.put(player.getUUID(), timer);
            }
        }
    }


    // --- Внутренний класс для обработчиков Forge EventBus ---
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
    private static class ForgeHandlers {

        // --- События для проверки смены души и управления атрибутами ---
        @SubscribeEvent
        public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
            if (event.getEntity() instanceof ServerPlayer player) {
                updateSoulEffects(player); // Вызываем метод из основного класса
            }
        }

        @SubscribeEvent
        public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
            if (event.getEntity() instanceof ServerPlayer player) {
                updateSoulEffects(player); // Вызываем метод из основного класса
            }
        }

        @SubscribeEvent
        public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
            if (event.getEntity() instanceof ServerPlayer player) {
                updateSoulEffects(player); // Вызываем метод из основного класса
            }
        }

        // --- Пассивные эффекты в PlayerTickEvent ---
        @SubscribeEvent
        public static void onPlayerTick(PlayerTickEvent event) {
            if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
                return;
            }

            SoulSystemWProcedure.getCapability(player).ifPresent(cap -> {
                if (!cap.isSoulDetermined()) return; // Не активна

                SoulType soul = cap.getCurrentSoul();

                switch (soul) {
                    case KINDNESS:
                        handleKindnessEffect(player); // Вызываем метод из основного класса
                        break;
                    case INTEGRITY:
                        handleIntegrityEffect(player, event); // Вызываем метод из основного класса
                        break;
                    case JUSTICE:
                        // Обработка временного увеличения урона от лука/арбалета (Justice)
                        handleJusticeBowBoost(player); // Вызываем метод из основного класса
                        break;
                    // Другие эффекты обрабатываются в других событиях
                }
            });
        }

        @SubscribeEvent
        public static void onPlayerAttack(AttackEntityEvent event) {
            if (!(event.getEntity() instanceof ServerPlayer player)) return;

            SoulSystemWProcedure.getCapability(player).ifPresent(cap -> {
                if (!cap.isSoulDetermined()) return;

                SoulType soul = cap.getCurrentSoul();

                if (soul == SoulType.BRAVERY) {
                    // Урон по площади при атаке (ближний бой)
                    Level level = player.level();
                    Vec3 pos = player.position();
                    AABB aabb = new AABB(pos.x() - 3, pos.y() - 1, pos.z() - 3, pos.x() + 3, pos.y() + 3, pos.z() + 3);
                    for (Entity entity : level.getEntities(player, aabb)) {
                        if (entity != event.getTarget() && entity != player && entity instanceof LivingEntity livingEntity) {
                            // Наносим урон по площади (например, 20% от основного урона)
                            float areaDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.2F;
                            livingEntity.hurt(level.damageSources().playerAttack(player), areaDamage);
                        }
                    }
                } else if (soul == SoulType.JUSTICE) {
                     // Проверка на моба-злодея (Vindicator, Pillager, Witch) при атаке
                     if (event.getTarget() instanceof Mob mob) {
                         ResourceLocation mobType = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()); // Используем getKey()
                         if (SoulsSystemWEffects.JUSTICE_TARGET_MOBS.contains(mobType)) {
                              // Увеличиваем урон (например, на 25%) - это делается в onLivingHurt
                              // Для простоты, просто добавим эффект свечения и пометим цель.
                         }
                     }
                     // Эффект свечения на цель
                     if (event.getTarget() instanceof LivingEntity target) {
                         target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0)); // 200 тиков = 10 секунд
                     }
                }
            });
        }

        // --- Эффекты при получении урона ---
        @SubscribeEvent
        public static void onLivingHurt(LivingHurtEvent event) {
            // --- Эффекты для игрока, получающего урон ---
            if (event.getEntity() instanceof ServerPlayer player) {
                SoulSystemWProcedure.getCapability(player).ifPresent(cap -> {
                    if (!cap.isSoulDetermined()) return;

                    SoulType soul = cap.getCurrentSoul();

                    if (soul == SoulType.PATIENCE) {
                        // Уменьшение получаемого урона (например, на 15%)
                        float reduction = 0.15f;
                        event.setAmount(event.getAmount() * (1.0f - reduction));
                    } else if (soul == SoulType.JUSTICE) {
                         // Увеличение сопротивления урону от злодеев
                         if (event.getSource().getEntity() instanceof Mob attacker) {
                             ResourceLocation attackerType = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(attacker.getType()); // Используем getKey()
                             if (SoulsSystemWEffects.JUSTICE_TARGET_MOBS.contains(attackerType)) {
                                 // Увеличиваем сопротивление (уменьшаем получаемый урон) от этих мобов (например, на 20%)
                                 float justiceReduction = 0.20f;
                                 event.setAmount(event.getAmount() * (1.0f - justiceReduction));
                             }
                         }
                    }
                });
            }

            // --- Эффекты для игрока, наносящего урон ---
            if (event.getSource().getEntity() instanceof ServerPlayer player) {
                SoulSystemWProcedure.getCapability(player).ifPresent(cap -> {
                    if (!cap.isSoulDetermined()) return;

                    SoulType soul = cap.getCurrentSoul();

                    // Увеличение урона, наносимого игроком злым мобам (Justice)
                    if (soul == SoulType.JUSTICE && event.getEntity() instanceof Mob targetMob) {
                        ResourceLocation targetType = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(targetMob.getType()); // Используем getKey()
                        if (SoulsSystemWEffects.JUSTICE_TARGET_MOBS.contains(targetType)) {
                            // Увеличиваем урон, наносимый игроком (Justice) этим мобам (например, на 25%)
                            float justiceBoost = 0.25f;
                            event.setAmount(event.getAmount() * (1.0f + justiceBoost));
                        }
                    }
                });
            }

            // --- Эффекты Justice (увеличенный урон от лука/арбалета) и Bravery (урон по площади от снаряда) ---
            DamageSource source = event.getSource();
            // Проверяем, является ли источник урона снарядом (стрелой, болтом и т.п.), выпущенным игроком
            if (source.getDirectEntity() instanceof AbstractArrow arrow && source.getEntity() instanceof ServerPlayer player) {
                SoulSystemWProcedure.getCapability(player).ifPresent(cap -> {
                    if (!cap.isSoulDetermined()) return;

                    SoulType soul = cap.getCurrentSoul();

                    // --- Эффект Justice: Увеличенный урон от лука/арбалета ---
                    if (soul == SoulType.JUSTICE) {
                        // Проверяем, держал ли игрок лук или арбалет
                        if (player.getMainHandItem().getItem() instanceof BowItem || player.getMainHandItem().getItem() instanceof CrossbowItem) {
                            // Увеличиваем урон от выстрела (грубо, добавляя модификатор к ATTACK_DAMAGE на короткое время)
                            AttributeInstance attackDamage = player.getAttribute(Attributes.ATTACK_DAMAGE);
                            if (attackDamage != null) {
                                // Удаляем старый модификатор, если он есть
                                attackDamage.removeModifier(JUSTICE_BOW_DAMAGE_BOOST_ID);
                                // Добавляем временный модификатор (например, 15% урона)
                                attackDamage.addTransientModifier(new AttributeModifier(
                                    JUSTICE_BOW_DAMAGE_BOOST_ID,
                                    "Soul Justice Ranged Damage Boost",
                                    0.15, // 15%
                                    AttributeModifier.Operation.MULTIPLY_BASE
                                ));
                                // Устанавливаем таймер для удаления модификатора (например, 10 тиков)
                                SoulsSystemWEffects.justiceBowBoostTimers.put(player.getUUID(), 10); // Используем поле из основного класса
                            }
                        }
                    }
                    // --- Эффект Bravery: Урон по площади от снаряда ---
                    else if (soul == SoulType.BRAVERY) {
                        // Проверяем, что цель - живое существо (не игрок сам себя)
                        // Используем стандартный instanceof и cast вместо pattern variable из-за конфликта типов
                        if (event.getEntity() instanceof LivingEntity && event.getEntity() != player) {
                            LivingEntity target = (LivingEntity) event.getEntity(); // Явное приведение типа
                            // Урон по площади вокруг точки попадания
                            Level level = player.level();
                            Vec3 pos = target.position(); // Позиция цели
                            // Определяем радиус урона по площади (например, 3 блока)
                            double radius = 4.0;
                            AABB aabb = new AABB(pos.x() - radius, pos.y() - 1, pos.z() - radius, pos.x() + radius, pos.y() + 3, pos.z() + radius);
                            // Проходим по всем сущностям в области
                            for (Entity entity : level.getEntities(player, aabb)) {
                                // Проверяем, что сущность не является целью (той, в которую попал снаряд),
                                // не является самим игроком и является живым существом
                                if (entity != target && entity != player && entity instanceof LivingEntity livingEntity) {
                                    // Наносим урон по площади (например, 20% от урона снаряда)
                                    float areaDamage = event.getAmount() * 0.2F; // Используем часть полученного урона
                                    livingEntity.hurt(level.damageSources().arrow(arrow, player), areaDamage);
                                }
                            }
                        }
                    }
                });
            }
        } // <-- ЗАКРЫВАЕТ МЕТОД `onLivingHurt`

        // --- Эффекты при добыче опыта с мобов ---
        @SubscribeEvent
        public static void onLivingExperienceDrop(LivingExperienceDropEvent event) {
            if (!(event.getAttackingPlayer() instanceof ServerPlayer player)) return;

            SoulSystemWProcedure.getCapability(player).ifPresent(cap -> {
                if (!cap.isSoulDetermined()) return;

                SoulType soul = cap.getCurrentSoul();

                if (soul == SoulType.PERSEVERANCE) {
                    // Увеличение получаемого опыта (например, на 25%)
                    int originalExp = event.getDroppedExperience();
                    int bonusExp = (int) (originalExp * 0.25);
                    event.setDroppedExperience(originalExp + bonusExp);
                }
            });
        }

        // --- Эффекты Perseverance при добыче блоков ---
        @SubscribeEvent
        public static void onBlockBreak(BlockEvent.BreakEvent event) {
            if (!(event.getPlayer() instanceof ServerPlayer player)) return;

            SoulSystemWProcedure.getCapability(player).ifPresent(cap -> {
                if (!cap.isSoulDetermined()) return;

                SoulType soul = cap.getCurrentSoul();

                if (soul == SoulType.PERSEVERANCE) {
                    Level level = player.level();
                    BlockState state = event.getState();
                    // Получаем уровень зачарования Удачи/Шёлкового прикосновения
                    int fortuneLevel = player.getMainHandItem().getEnchantmentLevel(net.minecraft.world.item.enchantment.Enchantments.BLOCK_FORTUNE);
                    int silkTouchLevel = player.getMainHandItem().getEnchantmentLevel(net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH);

                    // Используем getExpDrop, передавая уровень Удачи и Шёлкового прикосновения
                    int potentialExp = 0;
                    if (silkTouchLevel == 0) { // Опыт не выпадает при Шёлковом прикосновении
                        potentialExp = state.getExpDrop(level, level.getRandom(), event.getPos(), fortuneLevel, silkTouchLevel); // Правильная сигнатура
                    }

                    if (potentialExp > 0 && level instanceof ServerLevel serverLevel) {
                        // Увеличиваем количество опыта (например, на 25%)
                        int bonusExp = (int) (potentialExp * 0.25);
                        int totalExp = potentialExp + bonusExp;
                        if (totalExp > 0) {
                            // Добавляем сферу опыта вручную
                            net.minecraft.world.entity.ExperienceOrb.award(serverLevel, player.position(), totalExp);
                        }
                    }
                }
            });
        }

        // --- УДАЛЕН МЕТОД onProjectileImpact(ProjectileImpactEvent event) ---
        // Событие net.minecraftforge.event.entity.projectile.ProjectileImpactEvent не существует в Forge 1.20.1
        // Его функциональность перенесена в LivingHurtEvent (см. выше)
    } // <-- ЗАКРЫВАЕТ КЛАСС `ForgeHandlers`
} // <-- ЗАКРЫВАЕТ ВНЕШНИЙ КЛАСС `SoulsSystemWEffects`