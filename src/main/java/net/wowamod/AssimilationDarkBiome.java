package net.wowamod;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.DistExecutor;

import java.util.UUID;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class AssimilationDarkBiome {

    static {
        net.wowamod.custom.AssimilationClone.register();
    }

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel ASSIM_CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("universe3090", "assim_channel"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        ASSIM_CHANNEL.registerMessage(0, SyncAssimilationPacket.class, SyncAssimilationPacket::encode, SyncAssimilationPacket::decode, SyncAssimilationPacket::handle);
    }

    private static final UUID SPEED_MOD_ID = UUID.fromString("732d8492-1234-4a51-9123-1f1f1f1f1f1f");
    private static final UUID DAMAGE_MOD_ID = UUID.fromString("732d8492-5678-4a51-9123-2f2f2f2f2f2f");

    public static final Capability<AssimilationData> ASSIMILATION_CAP = CapabilityManager.get(new CapabilityToken<>() {});

    @SubscribeEvent
    public static void registerCaps(RegisterCapabilitiesEvent event) {
        event.register(AssimilationData.class);
    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void registerRenderers(net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(net.wowamod.custom.AssimilationClone.TYPE.get(), net.minecraft.client.renderer.entity.ZombieRenderer::new);
        }
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeBusEvents {

        @SubscribeEvent
        public static void onAttachCapabilities(AttachCapabilitiesEvent<net.minecraft.world.entity.Entity> event) {
            if (event.getObject() instanceof Player) {
                event.addCapability(new ResourceLocation("universe3090", "assimilation"), new AssimilationProvider());
            }
        }

        // === ИВЕНТЫ ДЛЯ СИНХРОНИЗАЦИИ ПРИ ЗАХОДЕ, СМЕРТИ И СМЕНЕ ИЗМЕРЕНИЯ ===
        @SubscribeEvent
        public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) { syncAndApply(event.getEntity()); }
        
        @SubscribeEvent
        public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) { syncAndApply(event.getEntity()); }
        
        @SubscribeEvent
        public static void onPlayerChangeDim(PlayerEvent.PlayerChangedDimensionEvent event) { syncAndApply(event.getEntity()); }

        @SubscribeEvent
        public static void onPlayerClone(PlayerEvent.Clone event) {
            if (!event.isWasDeath()) { // Сохраняем очки, если игрок не умер (например, вернулся из Энда)
                event.getOriginal().getCapability(ASSIMILATION_CAP).ifPresent(oldData -> {
                    event.getEntity().getCapability(ASSIMILATION_CAP).ifPresent(newData -> newData.points = oldData.points);
                });
            }
        }

        private static void syncAndApply(Player entity) {
            if (entity instanceof ServerPlayer player) {
                player.getCapability(ASSIMILATION_CAP).ifPresent(data -> {
                    ASSIM_CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SyncAssimilationPacket(data.points));
                    updatePlayerAttributes(player, data.getLevel());
                });
            }
        }

        @SubscribeEvent
        public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
            if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) return;

            // Срабатывает раз в секунду (каждые 20 тиков)
            if (event.player.tickCount % 20 != 0) return;

            ServerPlayer player = (ServerPlayer) event.player;
            Level world = player.level();
            BlockPos pos = player.blockPosition();

            player.getCapability(ASSIMILATION_CAP).ifPresent(data -> {
                int oldPoints = data.points;
                int oldLevel = data.getLevel();

                // НАДЕЖНАЯ ПРОВЕРКА БИОМА ДЛЯ 1.20.1
                ResourceKey<Biome> darkBiomeKey = ResourceKey.create(Registries.BIOME, new ResourceLocation("universe3090", "darkbiome"));
                boolean inDarkBiome = world.getBiome(pos).is(darkBiomeKey);
                
                // Проверка темноты (<= 4, чтобы включало ночной свет, если игрок на поверхности)
                boolean isDark = world.getMaxLocalRawBrightness(pos) <= 4; 

                // Логика начисления
                if (inDarkBiome && isDark) {
                    data.addPoints(2); 
                } else {
                    data.subPoints(2); 
                }

                int newLevel = data.getLevel();

                // Синхронизируем очки (только если они изменились)
                if (oldPoints != data.points) {
                    ASSIM_CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SyncAssimilationPacket(data.points));
                }

                // ОБНОВЛЯЕМ АТРИБУТЫ ТОЛЬКО ЕСЛИ ИЗМЕНИЛСЯ УРОВЕНЬ! (Это фиксит микро-лаги и сбросы бега)
                if (oldLevel != newLevel) {
                    updatePlayerAttributes(player, newLevel);
                }

                if (data.getLevel() >= 6) {
                    player.kill(); // 1000 очков = смерть
                }
            });
        }

        private static void updatePlayerAttributes(Player player, int level) {
            AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
            AttributeInstance damage = player.getAttribute(Attributes.ATTACK_DAMAGE);

            if (speed != null) {
                speed.removeModifier(SPEED_MOD_ID);
                // ИСПРАВЛЕНИЕ ЛОГИКИ: Добавлены 3 и 4 уровни для сохранения замедления!
                if (level == 1) speed.addTransientModifier(new AttributeModifier(SPEED_MOD_ID, "Assim Lvl 1", -0.2, AttributeModifier.Operation.MULTIPLY_TOTAL));
                else if (level >= 2 && level < 5) speed.addTransientModifier(new AttributeModifier(SPEED_MOD_ID, "Assim Lvl 2", -0.5, AttributeModifier.Operation.MULTIPLY_TOTAL));
                else if (level >= 5) speed.addTransientModifier(new AttributeModifier(SPEED_MOD_ID, "Assim Lvl 5", -1.0, AttributeModifier.Operation.MULTIPLY_TOTAL));
            }

            if (damage != null) {
                damage.removeModifier(DAMAGE_MOD_ID);
                if (level == 3) damage.addTransientModifier(new AttributeModifier(DAMAGE_MOD_ID, "Assim Lvl 3", -0.3, AttributeModifier.Operation.MULTIPLY_TOTAL));
                if (level >= 4) damage.addTransientModifier(new AttributeModifier(DAMAGE_MOD_ID, "Assim Lvl 4", -0.7, AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
        }

        @SubscribeEvent
        public static void onDeath(LivingDeathEvent event) {
            if (event.getEntity() instanceof ServerPlayer player) {
                player.getCapability(ASSIMILATION_CAP).ifPresent(data -> {
                    if (data.getLevel() >= 6) {
                        net.wowamod.custom.AssimilationClone clone = new net.wowamod.custom.AssimilationClone(
                                net.wowamod.custom.AssimilationClone.TYPE.get(), player.level());
                        clone.setPos(player.getX(), player.getY(), player.getZ());
                        player.level().addFreshEntity(clone);
                        
                        data.points = 0; // Сбрасываем после смерти
                        ASSIM_CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SyncAssimilationPacket(0));
                    }
                });
            }
        }
    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ClientEvents {
        @SubscribeEvent
        public static void onInput(MovementInputUpdateEvent event) {
            Player player = event.getEntity();
            if (player == null) return;
            player.getCapability(ASSIMILATION_CAP).ifPresent(data -> {
                if (data.getLevel() >= 5) {
                    event.getInput().forwardImpulse = 0;
                    event.getInput().leftImpulse = 0;
                    event.getInput().jumping = false;
                    event.getInput().shiftKeyDown = true;
                }
            });
        }

        @SubscribeEvent
        public static void onFOVUpdate(ViewportEvent.ComputeFov event) {
            if (event.getCamera().getEntity() instanceof Player player) {
                player.getCapability(ASSIMILATION_CAP).ifPresent(data -> {
                    if (data.getLevel() >= 1) {
                        event.setFOV(event.getFOV() - (data.getLevel() * 5f));
                    }
                });
            }
        }
    }

    public static class SyncAssimilationPacket {
        private final int points;
        public SyncAssimilationPacket(int points) { this.points = points; }
        public static void encode(SyncAssimilationPacket msg, FriendlyByteBuf buf) { buf.writeInt(msg.points); }
        public static SyncAssimilationPacket decode(FriendlyByteBuf buf) { return new SyncAssimilationPacket(buf.readInt()); }
        
        public static void handle(SyncAssimilationPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.handlePacket(msg.points));
            });
            ctx.get().setPacketHandled(true);
        }
    }

    private static class ClientPacketHandler {
        public static void handlePacket(int points) {
            Player player = Minecraft.getInstance().player;
            if (player != null) {
                player.getCapability(ASSIMILATION_CAP).ifPresent(data -> data.points = points);
            }
        }
    }

    public static class AssimilationData {
        public int points = 0;
        public void addPoints(int val) { this.points = Math.min(points + val, 1200); }
        public void subPoints(int val) { this.points = Math.max(points - val, 0); }
        public int getLevel() {
            if (points >= 1000) return 6;
            if (points >= 800) return 5;
            if (points >= 600) return 4;
            if (points >= 400) return 3;
            if (points >= 200) return 2;
            if (points >= 100) return 1;
            return 0;
        }
    }

    public static class AssimilationProvider implements net.minecraftforge.common.capabilities.ICapabilitySerializable<net.minecraft.nbt.CompoundTag> {
        private final AssimilationData data = new AssimilationData();
        private final net.minecraftforge.common.util.LazyOptional<AssimilationData> optional = net.minecraftforge.common.util.LazyOptional.of(() -> data);
        @Override public <T> net.minecraftforge.common.util.LazyOptional<T> getCapability(Capability<T> cap, net.minecraft.core.Direction side) { return ASSIMILATION_CAP.orEmpty(cap, optional); }
        @Override public net.minecraft.nbt.CompoundTag serializeNBT() {
            net.minecraft.nbt.CompoundTag nbt = new net.minecraft.nbt.CompoundTag();
            nbt.putInt("points", data.points);
            return nbt;
        }
        @Override public void deserializeNBT(net.minecraft.nbt.CompoundTag nbt) { data.points = nbt.getInt("points"); }
    }
}