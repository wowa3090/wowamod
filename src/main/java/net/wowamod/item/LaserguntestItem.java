package net.wowamod.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.wowamod.init.Universe3090ModParticleTypes;
import org.joml.Vector3f;

import java.util.List;
import java.util.Optional;

public class LaserguntestItem extends Item {
    
    // Настройки лазера
    private static final float DAMAGE = 17.0F;
    private static final double RANGE = 170.0D;
    private static final float PARTICLE_DENSITY = 0.13F;
    private static final Vector3f LASER_COLOR = new Vector3f(0.95F, 0.05F, 0.05F);
    private static final Vector3f CORE_COLOR = new Vector3f(1.0F, 0.8F, 0.8F);
    private static final int USE_DURATION = 72000;
    private static final double BEAM_OFFSET = 1.4;
    private static final float CUSTOM_PARTICLE_CHANCE = 0.3F; // Шанс появления кастомных частиц
    
    // Эффекты для целей
    private static final int FIRE_DURATION = 60; // 3 секунды (60 тиков)
    private static final int SLOWNESS_DURATION = 40; // 2 секунды (40 тиков)
    private static final int SLOWNESS_AMPLIFIER = 1; // Уровень замедления (II)
    
    // Теги для прозрачных блоков
    private static final TagKey<Block> GLASS_TAG = BlockTags.create(new ResourceLocation("minecraft", "glass"));
    private static final TagKey<Block> GLASS_PANES_TAG = BlockTags.create(new ResourceLocation("minecraft", "glass_panes"));
    
    public LaserguntestItem() {
        super(new Item.Properties()
            .stacksTo(1)
            .rarity(Rarity.EPIC));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return USE_DURATION;
    }

    @Override
    public void onUseTick(Level world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (!(user instanceof Player player)) return;
        
        Vec3 start = player.getEyePosition(1.0F).add(player.getViewVector(1.0F).scale(BEAM_OFFSET));
        Vec3 view = player.getViewVector(1.0F);
        Vec3 end = start.add(view.x * RANGE, view.y * RANGE, view.z * RANGE);
        
        LaserHitResult laserHit = traceLaser(world, player, start, end);
        
        if (!world.isClientSide()) {
            // Нанесение урона и эффектов
            if (remainingUseTicks % 4 == 0 && 
                laserHit.entity() != null && 
                laserHit.entity() instanceof LivingEntity) {
                
                LivingEntity target = (LivingEntity) laserHit.entity();
                
                // Нанесение урона
                target.hurt(player.damageSources().playerAttack(player), DAMAGE / 5);
                
                // Поджигание цели
                target.setSecondsOnFire(FIRE_DURATION / 20); // Конвертация тиков в секунды
                
                // Постепенное замедление
                applyProgressiveSlowness(target);
            }
            
            // Серверные эффекты
            spawnLaserEffects((ServerLevel) world, start, laserHit.position());
        } else {
            // Клиентские эффекты с регулировкой громкости
            if (remainingUseTicks % 5 == 0) {
                float volume = player.isShiftKeyDown() ? 0.1F : 0.3F;
                world.playSound(player, player.blockPosition(), SoundEvents.BEACON_AMBIENT, 
                               SoundSource.PLAYERS, volume, 1.8F);
            }
            
            // Клиентские частицы
            spawnClientLaserEffects(world, start, laserHit.position());
        }
    }

    // Применение прогрессирующего замедления
    private void applyProgressiveSlowness(LivingEntity target) {
        // Проверяем текущий эффект замедления
        MobEffectInstance currentSlowness = target.getEffect(MobEffects.MOVEMENT_SLOWDOWN);
        
        int duration = SLOWNESS_DURATION;
        int amplifier = SLOWNESS_AMPLIFIER;
        
        // Если уже есть эффект замедления, усиливаем его
        if (currentSlowness != null) {
            duration = Math.max(currentSlowness.getDuration(), SLOWNESS_DURATION);
            amplifier = Math.min(currentSlowness.getAmplifier() + 1, 3); // Максимум уровень IV
        }
        
        // Применяем новый эффект замедления
        target.addEffect(new MobEffectInstance(
            MobEffects.MOVEMENT_SLOWDOWN, 
            duration, 
            amplifier,
            false, // Не частицы
            true // Видимый эффект
        ));
    }

    @Override
    public void releaseUsing(ItemStack stack, Level world, LivingEntity user, int timeCharged) {
        if (world.isClientSide()) {
            world.playSound(user, user.blockPosition(), SoundEvents.BEACON_DEACTIVATE, 
                           SoundSource.PLAYERS, 0.5F, 1.0F);
        }
    }

    // Результат попадания лазера
    private record LaserHitResult(Vec3 position, Entity entity) {}
    
    // Трассировка луча
    private LaserHitResult traceLaser(Level world, Player player, Vec3 start, Vec3 end) {
        ClipContext context = new ClipContext(start, end, 
            ClipContext.Block.COLLIDER, 
            ClipContext.Fluid.NONE, 
            player
        );
        
        BlockHitResult blockHit = world.clip(context);
        Vec3 hitPos = blockHit.getLocation();
        
        EntityHitResult entityHit = findEntityOnPath(world, player, start, hitPos);
        
        return entityHit != null 
            ? new LaserHitResult(entityHit.getLocation(), entityHit.getEntity())
            : new LaserHitResult(hitPos, null);
    }

    private EntityHitResult findEntityOnPath(Level world, Player player, Vec3 start, Vec3 end) {
        AABB searchArea = new AABB(start, end).inflate(0.5);
        
        return world.getEntities(player, searchArea, e -> e instanceof LivingEntity && e != player)
            .stream()
            .filter(entity -> canHitEntity(start, end, entity))
            .map(entity -> new EntityHitResult(entity))
            .findFirst()
            .orElse(null);
    }
    
    private boolean canHitEntity(Vec3 start, Vec3 end, Entity entity) {
        AABB entityBox = entity.getBoundingBox();
        Optional<Vec3> hitPos = entityBox.clip(start, end);
        return hitPos.isPresent();
    }

    // Генерация эффектов лазера на сервере
    private void spawnLaserEffects(ServerLevel world, Vec3 start, Vec3 end) {
        Vec3 direction = end.subtract(start).normalize();
        double distance = start.distanceTo(end);
        
        // Основной луч с кастомными частицами
        spawnLaserBeam(world, start, end, distance);
        
        // Эффект попадания
        if (distance > 1.0) {
            // Только электрические искры (без фейерверка)
            world.sendParticles(ParticleTypes.ELECTRIC_SPARK, end.x, end.y, end.z, 5, 0.1, 0.1, 0.1, 0.01);
            
            // Кастомные частицы в точке попадания
            world.sendParticles(Universe3090ModParticleTypes.TESTLASERPARTICLE.get(), 
                              end.x, end.y, end.z, 
                              8, 0.2, 0.2, 0.2, 0.05);
        }
        
        // Кастомные частицы в основании луча
        world.sendParticles(Universe3090ModParticleTypes.TESTLASERPARTICLE.get(), 
                          start.x, start.y, start.z, 
                          5, 0.2, 0.2, 0.2, 0.05);
    }
    
    // Генерация луча с кастомными частицами
    private void spawnLaserBeam(ServerLevel world, Vec3 start, Vec3 end, double distance) {
        Vec3 direction = end.subtract(start).normalize();
        
        // Ядро луча
        DustParticleOptions coreParticle = new DustParticleOptions(CORE_COLOR, 0.9F);
        for (double d = 0; d < distance; d += PARTICLE_DENSITY) {
            Vec3 pos = start.add(direction.scale(d));
            world.sendParticles(coreParticle, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
            
            // Кастомные частицы вдоль луча
            if (world.random.nextFloat() < CUSTOM_PARTICLE_CHANCE) {
                world.sendParticles(Universe3090ModParticleTypes.TESTLASERPARTICLE.get(),
                                  pos.x, pos.y, pos.z,
                                  1, 0.05, 0.05, 0.05, 0.01);
            }
        }
        
        // Внешний слой
        DustParticleOptions outerParticle = new DustParticleOptions(LASER_COLOR, 0.7F);
        for (double d = 0; d < distance; d += PARTICLE_DENSITY * 1.5) {
            Vec3 pos = start.add(direction.scale(d));
            world.sendParticles(outerParticle, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
        }
    }
    
    // Упрощенные эффекты на клиенте
    private void spawnClientLaserEffects(Level world, Vec3 start, Vec3 end) {
        Vec3 direction = end.subtract(start).normalize();
        double distance = start.distanceTo(end);
        
        // Основные частицы
        DustParticleOptions dust = new DustParticleOptions(LASER_COLOR, 0.8F);
        for (double d = 0; d < distance; d += PARTICLE_DENSITY * 2) {
            Vec3 pos = start.add(direction.scale(d));
            world.addParticle(dust, pos.x, pos.y, pos.z, 0, 0, 0);
        }
        
        // Ядро луча на клиенте
        DustParticleOptions coreDust = new DustParticleOptions(CORE_COLOR, 1.0F);
        for (double d = 0; d < distance; d += PARTICLE_DENSITY * 4) {
            Vec3 pos = start.add(direction.scale(d));
            world.addParticle(coreDust, pos.x, pos.y, pos.z, 0, 0, 0);
        }
        
        // Кастомные частицы вдоль луча (клиент)
        for (double d = 0; d < distance; d += PARTICLE_DENSITY * 3) {
            if (world.random.nextFloat() < CUSTOM_PARTICLE_CHANCE) {
                Vec3 pos = start.add(direction.scale(d));
                world.addParticle(Universe3090ModParticleTypes.TESTLASERPARTICLE.get(),
                               pos.x, pos.y, pos.z,
                               0, 0, 0);
            }
        }
        
        // Кастомные частицы в основании луча (клиент)
        for (int i = 0; i < 3; i++) {
            double offsetX = world.random.nextGaussian() * 0.15;
            double offsetY = world.random.nextGaussian() * 0.15;
            double offsetZ = world.random.nextGaussian() * 0.15;
            
            world.addParticle(Universe3090ModParticleTypes.TESTLASERPARTICLE.get(),
                            start.x + offsetX, start.y + offsetY, start.z + offsetZ,
                            0, 0, 0);
        }

    }

    // Геттеры для обработчика освещения
    public static double getBeamOffset() {
        return BEAM_OFFSET;
    }

    public static double getRange() {
        return RANGE;
    }

    @Override
    public void appendHoverText(ItemStack itemstack, Level world, List<Component> list, TooltipFlag flag) {
        super.appendHoverText(itemstack, world, list, flag);
        list.add(Component.literal("§cW.I.P"));
        list.add(Component.literal("§cЭнергетическая лазерная пушка"));
        list.add(Component.literal("§7§oУдерживайте ПКМ для активации луча"));
        list.add(Component.literal("§4⚡ Урон: " + DAMAGE + " в секунду"));
        list.add(Component.literal("§b◆ Дальность: " + RANGE + " блоков"));
        list.add(Component.literal("§e\uD83D\uDD25 Поджигает цели на 3 секунды"));
        list.add(Component.literal("§9⏱ Постепенно замедляет цели"));
    }

}