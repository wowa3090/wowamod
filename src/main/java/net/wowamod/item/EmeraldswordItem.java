package net.wowamod.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent; // НОВЫЙ ИМПОРТ
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Mod.EventBusSubscriber(modid = "universe3090", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EmeraldswordItem extends SwordItem {

    public static final int MAX_ENERGY = 100000;
    public static final int ENERGY_COST_BASE = 500;   
    public static final int ENERGY_COST_AOE = 550;    
    public static final int ENERGY_COST_WAVE = 525;   

    private static final String[] MODES = {"Одиночный", "По площади", "Волновая атака"};
    
    private static boolean isProcessingAttack = false;

    // ГАРАНТИРОВАННАЯ РЕГИСТРАЦИЯ РЕНДЕРЕРА НА КЛИЕНТЕ БЕЗ ВЫЛЕТОВ СЕРВЕРА
    static {
        if (net.minecraftforge.fml.loading.FMLEnvironment.dist == net.minecraftforge.api.distmarker.Dist.CLIENT) {
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(ClientWaveRenderer.class);
        }
    }

    public EmeraldswordItem() {
        super(new Tier() {
            public int getUses() { return 4096; }
            public float getSpeed() { return 4f; }
            public float getAttackDamageBonus() { return 0f; } 
            public int getLevel() { return 1; }
            public int getEnchantmentValue() { return 2; }
            public Ingredient getRepairIngredient() { return Ingredient.of(); }
        }, 3, -2f, new Item.Properties().fireResistant().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player.isShiftKeyDown()) {
            CompoundTag tag = stack.getOrCreateTag();
            int currentMode = tag.getInt("AttackMode");
            int nextMode = (currentMode + 1) % 3;
            tag.putInt("AttackMode", nextMode);

            player.displayClientMessage(Component.literal("Режим меча: " + MODES[nextMode]).withStyle(ChatFormatting.GREEN), true);
        }
        return InteractionResultHolder.success(stack);
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (isProcessingAttack) return;

        if (!(event.getSource().getEntity() instanceof Player player)) return;

        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof EmeraldswordItem)) return;

        Level level = player.level();
        if (level.isClientSide) return; 

        LivingEntity mainTarget = event.getEntity();
        
        stack.getCapability(ForgeCapabilities.ENERGY).ifPresent(energyStorage -> {
            int currentEnergy = energyStorage.getEnergyStored();
            int mode = stack.hasTag() ? stack.getTag().getInt("AttackMode") : 0;
            
            int cost = switch (mode) {
                case 1 -> ENERGY_COST_AOE;
                case 2 -> ENERGY_COST_WAVE;
                default -> ENERGY_COST_BASE;
            };

            if (currentEnergy < cost) {
                return;
            }

            try {
                isProcessingAttack = true; 

                energyStorage.extractEnergy(cost, false);
                
                float energyBaseDamage = Mth.randomBetweenInclusive(player.getRandom(), 25, 30);
                float fullDamage = event.getAmount() - 4.0f + energyBaseDamage;
                if (fullDamage < 1.0f) fullDamage = 1.0f; 

                switch (mode) {
                    case 0: 
                        event.setAmount(fullDamage);
                        break;

                    case 1: 
                        AABB aoeBox = mainTarget.getBoundingBox().inflate(1.5D); 
                        List<LivingEntity> aoeTargets = level.getEntitiesOfClass(LivingEntity.class, aoeBox, 
                                e -> e != player && e.isAlive());
                        
                        int targetCount = aoeTargets.size();
                        float distributedDamage = fullDamage / targetCount;
                        
                        event.setAmount(distributedDamage); 

                        for (LivingEntity aoeTarget : aoeTargets) {
                            if (aoeTarget != mainTarget) {
                                aoeTarget.invulnerableTime = 0; 
                                aoeTarget.hurt(level.damageSources().playerAttack(player), distributedDamage);
                            }
                        }
                        break;

                    case 2: 
                        event.setAmount(fullDamage * 0.75f); 

                        Vec3 lookVec = player.getLookAngle().normalize();
                        Vec3 waveCenter = mainTarget.position().add(lookVec.x * 2.0, lookVec.y, lookVec.z * 2.0);
                        AABB waveBox = new AABB(waveCenter.x - 1.5, waveCenter.y - 1, waveCenter.z - 1.5, 
                                                waveCenter.x + 1.5, waveCenter.y + 1, waveCenter.z + 1.5);
                        
                        List<LivingEntity> waveTargets = level.getEntitiesOfClass(LivingEntity.class, waveBox, 
                                e -> e != player && e != mainTarget && e.isAlive());

                        float waveDamage = fullDamage * 0.25f;
                        
                        for (LivingEntity waveTarget : waveTargets) {
                            waveTarget.invulnerableTime = 0;
                            waveTarget.hurt(level.damageSources().playerAttack(player), waveDamage);
                        }
                        break;
                }
            } finally {
                isProcessingAttack = false; 
            }
        });
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        return true; // Логику отрисовки эффектов перенесли на клиентский эвент атаки ниже
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

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> list, TooltipFlag flag) {
        int energy = stack.hasTag() ? stack.getTag().getInt("Energy") : 0;
        int mode = stack.hasTag() ? stack.getTag().getInt("AttackMode") : 0;

        list.add(Component.literal("Энергия: " + energy + " / " + MAX_ENERGY + " FE").withStyle(ChatFormatting.AQUA));
        list.add(Component.literal("Режим: " + MODES[mode]).withStyle(ChatFormatting.GOLD));
        list.add(Component.literal("Shift + ПКМ для смены режима").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        
        super.appendHoverText(stack, level, list, flag);
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return 0;
    }

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
        return 0x00FFFF; 
    }

    // =========================================================================================
    // --- ВНУТРЕННИЙ КЛИЕНТСКИЙ РЕНДЕРЕР (БЕЗ АННОТАЦИИ @EventBusSubscriber ДЛЯ СТАБИЛЬНОСТИ) ---
    // =========================================================================================
    public static class ClientWaveRenderer {
        private static final Map<UUID, Long> SPARKING_ENTITIES = new ConcurrentHashMap<>();
        private static final List<WaveEffect> ACTIVE_WAVES = new CopyOnWriteArrayList<>();

        public static void addSpark(UUID uuid) {
            SPARKING_ENTITIES.put(uuid, System.currentTimeMillis() + 800); 
        }

        public static void addWave(Vec3 pos) {
            ACTIVE_WAVES.add(new WaveEffect(pos, System.currentTimeMillis()));
        }

        private static class WaveEffect {
            final Vec3 pos;
            final long startTime;

            WaveEffect(Vec3 pos, long startTime) {
                this.pos = pos;
                this.startTime = startTime;
            }
        }

        // КЛИЕНТСКИЙ ПЕРЕХВАТ НАЧАЛА АТАКЫ ДЛЯ МГНОВЕННОГО СПАВНА ЭФФЕКТОВ
        @SubscribeEvent
        public static void onPlayerAttack(AttackEntityEvent event) {
            Player player = event.getEntity();
            // Выполняем только на клиенте
            if (player.level().isClientSide && player == Minecraft.getInstance().player) {
                ItemStack stack = player.getMainHandItem();
                if (stack.getItem() instanceof EmeraldswordItem) {
                    int mode = stack.hasTag() ? stack.getTag().getInt("AttackMode") : 0;
                    if (mode == 2 && event.getTarget() instanceof LivingEntity livingTarget) {
                        Vec3 lookVec = player.getLookAngle().normalize();
                        Vec3 waveCenter = livingTarget.position().add(lookVec.x * 2.0, lookVec.y, lookVec.z * 2.0);
                        
                        addWave(waveCenter);

                        AABB waveBox = new AABB(waveCenter.x - 1.5, waveCenter.y - 1, waveCenter.z - 1.5, 
                                                waveCenter.x + 1.5, waveCenter.y + 1, waveCenter.z + 1.5);
                        List<LivingEntity> waveTargets = player.level().getEntitiesOfClass(LivingEntity.class, waveBox, 
                                e -> e != player && e != livingTarget && e.isAlive());

                        for (LivingEntity waveTarget : waveTargets) {
                            addSpark(waveTarget.getUUID());
                        }
                        
                        addSpark(livingTarget.getUUID());
                    }
                }
            }
        }

        // ОРИГИНАЛЬНАЯ ОТРИСОВКА ИСКР ВОКРУГ МОБА
        @SubscribeEvent
        public static void onRenderLiving(RenderLivingEvent.Post<?, ?> event) {
            LivingEntity entity = event.getEntity();
            Long expireTime = SPARKING_ENTITIES.get(entity.getUUID());
            if (expireTime == null) return;

            if (System.currentTimeMillis() > expireTime) {
                SPARKING_ENTITIES.remove(entity.getUUID());
                return;
            }

            PoseStack poseStack = event.getPoseStack();
            MultiBufferSource buffer = event.getMultiBufferSource();
            VertexConsumer consumer = buffer.getBuffer(RenderType.lightning());

            RandomSource random = entity.getRandom();
            float w = entity.getBbWidth();
            float h = entity.getBbHeight();

            for (int i = 0; i < 6; i++) {
                float sx = (random.nextFloat() - 0.5f) * w;
                float sy = random.nextFloat() * h;
                float sz = (random.nextFloat() - 0.5f) * w;

                float ex = sx + (random.nextFloat() - 0.5f) * 0.5f;
                float ey = sy + (random.nextFloat() - 0.5f) * 0.5f;
                float ez = sz + (random.nextFloat() - 0.5f) * 0.5f;

                int red = 255;
                int green = random.nextBoolean() ? 0 : 255; 
                int blue = green;

                drawSparkLine(poseStack, consumer, sx, sy, sz, ex, ey, ez, random, red, green, blue);
            }
        }

        private static void drawSparkLine(PoseStack poseStack, VertexConsumer consumer, 
                                          float sx, float sy, float sz, 
                                          float ex, float ey, float ez, 
                                          RandomSource random, int r, int g, int b) {
            poseStack.pushPose();
            Matrix4f matrix = poseStack.last().pose();

            float mx1 = sx + (ex - sx) * 0.33f + (random.nextFloat() - 0.5f) * 0.15f;
            float my1 = sy + (ey - sy) * 0.33f + (random.nextFloat() - 0.5f) * 0.15f;
            float mz1 = sz + (ez - sz) * 0.33f + (random.nextFloat() - 0.5f) * 0.15f;

            float mx2 = sx + (ex - sx) * 0.66f + (random.nextFloat() - 0.5f) * 0.15f;
            float my2 = sy + (ey - sy) * 0.66f + (random.nextFloat() - 0.5f) * 0.15f;
            float mz2 = sz + (ez - sz) * 0.66f + (random.nextFloat() - 0.5f) * 0.15f;

            drawSegment(matrix, consumer, sx, sy, sz, mx1, my1, mz1, r, g, b);
            drawSegment(matrix, consumer, mx1, my1, mz1, mx2, my2, mz2, r, g, b);
            drawSegment(matrix, consumer, mx2, my2, mz2, ex, ey, ez, r, g, b);

            poseStack.popPose();
        }

        private static void drawSegment(Matrix4f matrix, VertexConsumer consumer, 
                                        float x1, float y1, float z1, 
                                        float x2, float y2, float z2, 
                                        int r, int g, int b) {
            float thickness = 0.025f; 
            consumer.vertex(matrix, x1 - thickness, y1, z1).color(r, g, b, 255).endVertex();
            consumer.vertex(matrix, x2 - thickness, y2, z2).color(r, g, b, 255).endVertex();
            consumer.vertex(matrix, x2 + thickness, y2, z2).color(r, g, b, 255).endVertex();
            consumer.vertex(matrix, x1 + thickness, y1, z1).color(r, g, b, 255).endVertex();
        }

        // ОРИГИНАЛЬНАЯ ОТРИСОВКА САМОЙ 3Д ВОЛНЫ В МИРЕ
        @SubscribeEvent
        public static void onRenderLevel(RenderLevelStageEvent event) {
            if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
            if (ACTIVE_WAVES.isEmpty()) return;

            Minecraft mc = Minecraft.getInstance();
            Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();
            PoseStack poseStack = event.getPoseStack();

            poseStack.pushPose();
            poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

            VertexConsumer consumer = mc.renderBuffers().bufferSource().getBuffer(RenderType.lightning());
            long now = System.currentTimeMillis();

            for (WaveEffect wave : ACTIVE_WAVES) {
                long age = now - wave.startTime;
                if (age > 600) { 
                    ACTIVE_WAVES.remove(wave);
                    continue;
                }

                float radius = (age / 600.0F) * 2.5F;
                float thickness = 0.15F;

                drawWaveRing(poseStack, consumer, wave.pos, radius, thickness, age);
            }

            poseStack.popPose();
        }

        private static void drawWaveRing(PoseStack poseStack, VertexConsumer consumer, Vec3 pos, float radius, float thickness, long age) {
            Matrix4f matrix = poseStack.last().pose();
            int segments = 24; 
            
            int alpha = (int) ((1.0F - (age / 600.0F)) * 255);
            if (alpha < 0) alpha = 0;

            int r = 255;
            int g = (int) ((1.0F - (age / 350.0F)) * 255); 
            if (g < 0) g = 0;
            int b = g;

            for (int i = 0; i < segments; i++) {
                double angle1 = (i * 2 * Math.PI) / segments;
                double angle2 = ((i + 1) * 2 * Math.PI) / segments;

                float x1 = (float) (pos.x + Math.cos(angle1) * radius);
                float z1 = (float) (pos.z + Math.sin(angle1) * radius);
                float x2 = (float) (pos.x + Math.cos(angle2) * radius);
                float z2 = (float) (pos.z + Math.sin(angle2) * radius);

                float x1Inner = (float) (pos.x + Math.cos(angle1) * (radius - thickness));
                float z1Inner = (float) (pos.z + Math.sin(angle1) * (radius - thickness));
                float x2Inner = (float) (pos.x + Math.cos(angle2) * (radius - thickness));
                float z2Inner = (float) (pos.z + Math.sin(angle2) * (radius - thickness));

                consumer.vertex(matrix, x1Inner, (float)pos.y + 0.1F, z1Inner).color(r, g, b, alpha).endVertex();
                consumer.vertex(matrix, x2Inner, (float)pos.y + 0.1F, z2Inner).color(r, g, b, alpha).endVertex();
                consumer.vertex(matrix, x2, (float)pos.y + 0.1F, z2).color(r, g, b, alpha).endVertex();
                consumer.vertex(matrix, x1, (float)pos.y + 0.1F, z1).color(r, g, b, alpha).endVertex();
            }
        }
    }
}