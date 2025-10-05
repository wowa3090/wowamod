package net.wowamod.procedures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

// Main class containing the soul system logic and capability
@Mod.EventBusSubscriber
public class SoulSystemWProcedure {

    // Use the provided MODID
    public static final String MODID = "universe3090"; // Replace with your actual mod ID
    public static final ResourceLocation SOUL_CAPABILITY_ID = new ResourceLocation(MODID, "soul_data");

    // Enum for soul types
    public enum SoulType {
        DETERMINATION, BRAVERY, JUSTICE, KINDNESS, PATIENCE, INTEGRITY, PERSEVERANCE, NONE
    }

    // Capability interface
    public interface ISoulCapability {
        void increaseSoulValue(SoulType type, int amount);
        void decreaseSoulValue(SoulType type, int amount);
        int getSoulValue(SoulType type);
        void setSoulValue(SoulType type, int value);
        void determineSoul();
        boolean isSoulDetermined();
        SoulType getCurrentSoul();
        void resetSoul();
        void setSoulDetermined(boolean determined);
        void markPlayerInteraction(UUID otherPlayerUUID); // For Integrity
        boolean hasInteractedWith(UUID otherPlayerUUID); // For Integrity
        void setPlayerInCave(boolean inCave); // For Bravery
        boolean isPlayerInCave(); // For Bravery
        CompoundTag serializeNBT(); // Add this to interface
        void deserializeNBT(CompoundTag nbt); // Add this to interface
        Set<UUID> getInteractedPlayers(); // Add this to interface
    }

    // Default implementation of the capability
    public static class SoulCapability implements ISoulCapability {
        private int determination = 0;
        private int bravery = 0;
        private int justice = 0;
        private int kindness = 0;
        private int patience = 0;
        private int integrity = 0;
        private int perseverance = 0;
        private boolean soulDetermined = false;
        private SoulType currentSoul = SoulType.NONE;
        private final Set<UUID> interactedPlayers = new HashSet<>();
        private boolean playerInCave = false;

        private static final int SOUL_DETERMINATION_THRESHOLD = 100; // Adjust as needed
        private static final int CAVE_CHECK_INTERVAL = 100; // Ticks between cave checks
        private int caveCheckTimer = 0;

        @Override
        public void increaseSoulValue(SoulType type, int amount) {
            if (soulDetermined) return; // Don't update if soul is already determined
            switch (type) {
                case DETERMINATION -> determination += amount;
                case BRAVERY -> bravery += amount;
                case JUSTICE -> justice += amount;
                case KINDNESS -> kindness += amount;
                case PATIENCE -> patience += amount;
                case INTEGRITY -> integrity += amount;
                case PERSEVERANCE -> perseverance += amount;
            }
            // Check if any value has crossed the threshold after increment
            if (!soulDetermined && (determination >= SOUL_DETERMINATION_THRESHOLD || bravery >= SOUL_DETERMINATION_THRESHOLD ||
                    justice >= SOUL_DETERMINATION_THRESHOLD || kindness >= SOUL_DETERMINATION_THRESHOLD ||
                    patience >= SOUL_DETERMINATION_THRESHOLD || integrity >= SOUL_DETERMINATION_THRESHOLD ||
                    perseverance >= SOUL_DETERMINATION_THRESHOLD)) {
                determineSoul();
            }
        }

        @Override
        public void decreaseSoulValue(SoulType type, int amount) {
            if (soulDetermined) return; // Don't update if soul is already determined
            switch (type) {
                case DETERMINATION -> determination = Math.max(0, determination - amount);
                case BRAVERY -> bravery = Math.max(0, bravery - amount);
                case JUSTICE -> justice = Math.max(0, justice - amount);
                case KINDNESS -> kindness = Math.max(0, kindness - amount);
                case PATIENCE -> patience = Math.max(0, patience - amount);
                case INTEGRITY -> integrity = Math.max(0, integrity - amount);
                case PERSEVERANCE -> perseverance = Math.max(0, perseverance - amount);
            }
        }

        @Override
        public int getSoulValue(SoulType type) {
            return switch (type) {
                case DETERMINATION -> determination;
                case BRAVERY -> bravery;
                case JUSTICE -> justice;
                case KINDNESS -> kindness;
                case PATIENCE -> patience;
                case INTEGRITY -> integrity;
                case PERSEVERANCE -> perseverance;
                default -> 0;
            };
        }

        @Override
        public void setSoulValue(SoulType type, int value) {
            if (soulDetermined) return; // Don't update if soul is already determined
            switch (type) {
                case DETERMINATION -> determination = Math.max(0, value);
                case BRAVERY -> bravery = Math.max(0, value);
                case JUSTICE -> justice = Math.max(0, value);
                case KINDNESS -> kindness = Math.max(0, value);
                case PATIENCE -> patience = Math.max(0, value);
                case INTEGRITY -> integrity = Math.max(0, value);
                case PERSEVERANCE -> perseverance = Math.max(0, value);
            }
        }

        @Override
        public void determineSoul() {
            if (soulDetermined) return; // Already determined

            SoulType determinedSoul = SoulType.NONE;
            int highestValue = -1;

            // Find the highest value among the soul types
            if (determination > highestValue) {
                highestValue = determination;
                determinedSoul = SoulType.DETERMINATION;
            }
            if (bravery > highestValue) {
                highestValue = bravery;
                determinedSoul = SoulType.BRAVERY;
            }
            if (justice > highestValue) {
                highestValue = justice;
                determinedSoul = SoulType.JUSTICE;
            }
            if (kindness > highestValue) {
                highestValue = kindness;
                determinedSoul = SoulType.KINDNESS;
            }
            if (patience > highestValue) {
                highestValue = patience;
                determinedSoul = SoulType.PATIENCE;
            }
            if (integrity > highestValue) {
                highestValue = integrity;
                determinedSoul = SoulType.INTEGRITY;
            }
            if (perseverance > highestValue) {
                highestValue = perseverance;
                determinedSoul = SoulType.PERSEVERANCE;
            }

            // If there's a tie or no value reached threshold, default to NONE or handle as needed
            if (highestValue >= SOUL_DETERMINATION_THRESHOLD) {
                this.currentSoul = determinedSoul;
                this.soulDetermined = true;
                System.out.println("Player's soul has been determined: " + determinedSoul.name()); // Debug
            } else {
                System.out.println("No soul type reached the threshold yet."); // Debug
            }
        }

        @Override
        public boolean isSoulDetermined() {
            return soulDetermined;
        }

        @Override
        public SoulType getCurrentSoul() {
            return currentSoul;
        }

        @Override
        public void resetSoul() {
            determination = 0;
            bravery = 0;
            justice = 0;
            kindness = 0;
            patience = 0;
            integrity = 0;
            perseverance = 0;
            soulDetermined = false;
            currentSoul = SoulType.NONE;
            interactedPlayers.clear();
            playerInCave = false;
        }

        @Override
        public void setSoulDetermined(boolean determined) {
            this.soulDetermined = determined;
        }

        // NBT serialization for saving/loading
        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putInt("Determination", determination);
            tag.putInt("Bravery", bravery);
            tag.putInt("Justice", justice);
            tag.putInt("Kindness", kindness);
            tag.putInt("Patience", patience);
            tag.putInt("Integrity", integrity);
            tag.putInt("Perseverance", perseverance);
            tag.putBoolean("SoulDetermined", soulDetermined);
            tag.putString("CurrentSoul", currentSoul.name());
            tag.putBoolean("PlayerInCave", playerInCave);

            // Serialize the set of interacted players
            net.minecraft.nbt.ListTag uuidList = new net.minecraft.nbt.ListTag();
            for (UUID uuid : interactedPlayers) {
                uuidList.add(net.minecraft.nbt.StringTag.valueOf(uuid.toString()));
            }
            tag.put("InteractedPlayers", uuidList);

            return tag;
        }

        // NBT deserialization for saving/loading
        @Override
        public void deserializeNBT(CompoundTag tag) {
            determination = tag.getInt("Determination");
            bravery = tag.getInt("Bravery");
            justice = tag.getInt("Justice");
            kindness = tag.getInt("Kindness");
            patience = tag.getInt("Patience");
            integrity = tag.getInt("Integrity");
            perseverance = tag.getInt("Perseverance");
            soulDetermined = tag.getBoolean("SoulDetermined");
            String soulName = tag.getString("CurrentSoul");
            try {
                this.currentSoul = SoulType.valueOf(soulName);
            } catch (IllegalArgumentException e) {
                this.currentSoul = SoulType.NONE; // Default if invalid name
            }
            playerInCave = tag.getBoolean("PlayerInCave");

            // Deserialize the set of interacted players
            interactedPlayers.clear();
            net.minecraft.nbt.ListTag uuidList = tag.getList("InteractedPlayers", net.minecraft.nbt.Tag.TAG_STRING);
            for (int i = 0; i < uuidList.size(); ++i) {
                try {
                    UUID uuid = UUID.fromString(uuidList.getString(i));
                    interactedPlayers.add(uuid);
                } catch (IllegalArgumentException e) {
                    // Ignore invalid UUIDs
                }
            }
        }

        @Override
        public void markPlayerInteraction(UUID otherPlayerUUID) {
            if (soulDetermined) return; // Don't update if soul is already determined
            if (!interactedPlayers.contains(otherPlayerUUID)) {
                interactedPlayers.add(otherPlayerUUID);
                // Increase Integrity when interacting with a new player
                increaseSoulValue(SoulType.INTEGRITY, 5); // Adjust amount
                System.out.println("Player gained Integrity from interacting with a new player.");
            }
        }

        @Override
        public boolean hasInteractedWith(UUID otherPlayerUUID) {
            return interactedPlayers.contains(otherPlayerUUID);
        }

        @Override
        public void setPlayerInCave(boolean inCave) {
            this.playerInCave = inCave;
        }

        @Override
        public boolean isPlayerInCave() {
            return playerInCave;
        }

        @Override
        public Set<UUID> getInteractedPlayers() {
            return interactedPlayers;
        }

        // Method to potentially increase Bravery if player is in a cave
        public void tickCaveCheck(Player player) {
            if (soulDetermined) return;
            caveCheckTimer++;
            if (caveCheckTimer >= CAVE_CHECK_INTERVAL) {
                caveCheckTimer = 0;
                Level level = player.level();
                BlockPos playerPos = player.blockPosition();
                // A simple check: if the block above the player is solid and light level is low
                BlockPos checkPos = playerPos.above();
                BlockState stateAbove = level.getBlockState(checkPos);
                // Use getLightEmission or check skylight/brightness more thoroughly if needed
                // In 1.20.1, getBrightness is used with LightLayer.
                // getLightEmission gets the light level *emitted* by the block, not the ambient light.
                // getBrightness(LightLayer, BlockPos) is the correct way to get ambient light.
                int effectiveSkyLight = level.getBrightness(LightLayer.SKY, playerPos);
                int effectiveBlockLight = level.getBrightness(LightLayer.BLOCK, playerPos);
                int maxLight = Math.max(effectiveSkyLight, effectiveBlockLight);

                boolean isUnderground = stateAbove.isSolid() || maxLight < 7; // Rough check for underground/cave

                if (isUnderground && !playerInCave) {
                    setPlayerInCave(true);
                    // Increase Bravery when entering a cave
                    increaseSoulValue(SoulType.BRAVERY, 1); // Small increment per interval
                    System.out.println("Player " + player.getName().getString() + " gained Bravery from being in a cave.");
                } else if (!isUnderground && playerInCave) {
                    setPlayerInCave(false);
                }
            }
        }
    }

    // Capability Provider
    public static class SoulCapabilityProvider implements ICapabilitySerializable<CompoundTag> {
        public static final Capability<ISoulCapability> SOUL_CAPABILITY = net.minecraftforge.common.capabilities.CapabilityManager.get(new net.minecraftforge.common.capabilities.CapabilityToken<>() {});

        private ISoulCapability instance = null;
        private final LazyOptional<ISoulCapability> lazyOptional = LazyOptional.of(this::createInstance);

        private ISoulCapability createInstance() {
            if (instance == null) {
                instance = new SoulCapability();
            }
            return instance;
        }

        @Override
        public CompoundTag serializeNBT() {
            // Call the method on the actual capability instance
            return createInstance().serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            // Call the method on the actual capability instance
            createInstance().deserializeNBT(nbt);
        }

        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            if (cap == SOUL_CAPABILITY) {
                return lazyOptional.cast();
            }
            return LazyOptional.empty();
        }
    }

    // Event handler to attach the capability to players
    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(SOUL_CAPABILITY_ID, new SoulCapabilityProvider());
        }
    }

    // Helper method to get the capability instance for a player
    public static LazyOptional<ISoulCapability> getCapability(Player player) {
        return player.getCapability(SoulCapabilityProvider.SOUL_CAPABILITY);
    }

    // Event handler to tick the cave check for players
    @SubscribeEvent
    public static void onEntityTick(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Player player) {
            getCapability(player).ifPresent(cap -> {
                ((SoulCapability) cap).tickCaveCheck(player);
            });
        }
    }

    // Event handlers to update soul values based on actions
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player) {
            getCapability(player).ifPresent(cap -> {
                // Increase PATIENCE when player takes damage
                cap.increaseSoulValue(SoulType.PATIENCE, 1); // Adjust amount as needed
                System.out.println("Player " + player.getName().getString() + " gained Patience. Current value: " + cap.getSoulValue(SoulType.PATIENCE));
            });
        }
        // Increase DETERMINATION when player deals damage (if the source is a player)
        if (event.getSource().getEntity() instanceof Player player) {
            getCapability(player).ifPresent(cap -> {
                // Increase DETERMINATION slightly for dealing damage
                cap.increaseSoulValue(SoulType.DETERMINATION, 1); // Adjust amount as needed
                System.out.println("Player " + player.getName().getString() + " gained Determination from dealing damage. Current value: " + cap.getSoulValue(SoulType.DETERMINATION));
            });
        }
    }

	@SubscribeEvent
	public static void onLivingDeath(LivingDeathEvent event) {
	    LivingEntity target = event.getEntity();
	    if (target instanceof Player killerPlayer) {
	        // Increase DETERMINATION for killing another player? (Consider lore implications)
	        // Or maybe increase DETERMINATION more significantly?
	        getCapability(killerPlayer).ifPresent(cap -> {
	            cap.increaseSoulValue(SoulType.DETERMINATION, 5); // Significant boost for killing a player
	            System.out.println("Player " + killerPlayer.getName().getString() + " gained Determination from killing a player. Current value: " + cap.getSoulValue(SoulType.DETERMINATION));
	        });
	    } else if (target instanceof Mob mob) {
	        DamageSource source = event.getSource();
	        Entity sourceEntity = source.getEntity();
	        Entity directEntity = source.getDirectEntity();
	
	        // Проверяем, является ли sourceEntity или directEntity игроком
	        if (sourceEntity instanceof Player player) { // Используем сокращенную запись только внутри блока if
	            // Теперь player - это final или effectively final в этой области видимости
	            getCapability(player).ifPresent(cap -> {
	                // Increase DETERMINATION for killing hostile mobs
	                if (mob.getType().getCategory() == MobCategory.MONSTER) { // Use == for enum comparison
	                    cap.increaseSoulValue(SoulType.DETERMINATION, 2); // Adjust amount
	                    System.out.println("Player " + player.getName().getString() + " gained Determination from killing a hostile mob. Current value: " + cap.getSoulValue(SoulType.DETERMINATION));
	                }
	                // Increase JUSTICE for killing hostile mobs (or specific types?)
	                cap.increaseSoulValue(SoulType.JUSTICE, 1); // Adjust amount
	                System.out.println("Player " + player.getName().getString() + " gained Justice from killing a mob. Current value: " + cap.getSoulValue(SoulType.JUSTICE));
	            });
	        } else if (directEntity instanceof Player player) { // Используем сокращенную запись только внутри блока if
	             // Теперь player - это final или effectively final в этой области видимости
	             getCapability(player).ifPresent(cap -> {
	                 // Increase DETERMINATION for killing hostile mobs
	                 if (mob.getType().getCategory() == MobCategory.MONSTER) { // Use == for enum comparison
	                     cap.increaseSoulValue(SoulType.DETERMINATION, 2); // Adjust amount
	                     System.out.println("Player " + player.getName().getString() + " gained Determination from killing a hostile mob. Current value: " + cap.getSoulValue(SoulType.DETERMINATION));
	                 }
	                 // Increase JUSTICE for killing hostile mobs (or specific types?)
	                 cap.increaseSoulValue(SoulType.JUSTICE, 1); // Adjust amount
	                 System.out.println("Player " + player.getName().getString() + " gained Justice from killing a mob. Current value: " + cap.getSoulValue(SoulType.JUSTICE));
	             });
	        }
	        // Если ни sourceEntity, ни directEntity не являются игроком, ничего не делаем.
	    }
	}

    // Trigger for harvesting crops
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        // Avoid instanceof pattern variable - check type explicitly if needed later
        if (player != null) { // Ensure player is not null
            Level world = player.level(); // Use player.level() instead of event.getLevel()
            BlockPos pos = event.getPos();
            BlockState state = world.getBlockState(pos);

            // Check if the block broken is a crop (instanceof CropBlock is reliable)
            if (state.getBlock() instanceof CropBlock cropBlock) {
                 getCapability(player).ifPresent(cap -> {
                     cap.increaseSoulValue(SoulType.KINDNESS, 2); // Adjust amount
                     System.out.println("Player " + player.getName().getString() + " gained Kindness from harvesting crops. Current value: " + cap.getSoulValue(SoulType.KINDNESS));
                 });
            }
        }
    }

    // Trigger for healing animals (Right-clicking with food item)
    @SubscribeEvent
    public static void onPlayerInteractEntity(PlayerInteractEvent.EntityInteractSpecific event) {
        Player player = event.getEntity();
        Entity target = event.getTarget();
        InteractionHand hand = event.getHand();
        ItemStack stack = player.getItemInHand(hand);

        // Check if the target is an animal and the item is a food it likes
        if (target instanceof Animal animal && animal.isFood(stack)) {
            // Check if the animal is not already at full health
            if (animal.getHealth() < animal.getMaxHealth()) {
                 getCapability(player).ifPresent(cap -> {
                     cap.increaseSoulValue(SoulType.KINDNESS, 3); // Adjust amount
                     System.out.println("Player " + player.getName().getString() + " gained Kindness from healing an animal. Current value: " + cap.getSoulValue(SoulType.KINDNESS));
                 });
                 // Optionally, you could still allow the healing action by not cancelling the event,
                 // or cancel it if you want the soul system action to replace it.
                 // event.setCancellationResult(InteractionResult.SUCCESS); // Example of modifying result
            }
        }
    }

    // Trigger for using anvil (repairing items - Perseverance)
    @SubscribeEvent
    public static void onAnvilRepair(AnvilRepairEvent event) {
        if (event.getEntity() instanceof Player) { // Проверяем тип
            Player player = (Player) event.getEntity(); // Приводим к типу Player
            getCapability(player).ifPresent(cap -> {
                // Increase Perseverance based on the repair cost or a fixed amount
                cap.increaseSoulValue(SoulType.PERSEVERANCE, 1); // Adjust amount, maybe use event.getCost() for scaling
                System.out.println("Player " + player.getName().getString() + " gained Perseverance from repairing an item. Current value: " + cap.getSoulValue(SoulType.PERSEVERANCE));
            });
        }
    }


    // More robust trigger for giving items to other players (Integrity)
    @SubscribeEvent
    public static void onPlayerInteractEntityGive(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        Entity target = event.getTarget();

        if (target instanceof Player targetPlayer) {
            // Check if the player clicked the other player with an item in their hand
            ItemStack stackInHand = player.getItemInHand(event.getHand());
            if (!stackInHand.isEmpty()) {
                // Check if the item was successfully transferred (e.g., shift-clicked onto the other player)
                // This is difficult to detect directly with this event without tracking inventory changes.
                // A simpler proxy: any interaction with another player could count towards integrity.
                // Or, track if an item was dropped by player A and picked up by player B.
                // For this example, we'll just mark the interaction.
                getCapability(player).ifPresent(cap -> cap.markPlayerInteraction(targetPlayer.getUUID()));
                getCapability(targetPlayer).ifPresent(cap -> cap.markPlayerInteraction(player.getUUID()));
            }
        }
    }


    // Command execution methods (these would be called from command implementations)
    public static void executeResetSoul(Player player) {
        getCapability(player).ifPresent(ISoulCapability::resetSoul);
        if (player instanceof ServerPlayer serverPlayer) {
            // Optionally send a message to the player
            // serverPlayer.sendSystemMessage(Component.literal("Your soul has been reset."));
            System.out.println("Soul reset for player: " + player.getName().getString());
        }
    }

    public static void executeChangeSoul(Player player, SoulType newSoul) {
        // Changing soul after determination might not fit the lore. Resetting might be more appropriate.
        // This method allows direct setting, use cautiously.
        getCapability(player).ifPresent(cap -> {
            if (!cap.isSoulDetermined()) {
                // If not determined, allow setting values to influence future determination
                cap.setSoulValue(newSoul, SoulCapability.SOUL_DETERMINATION_THRESHOLD); // Force this type to win next check
            } else {
                // If already determined, maybe just a visual/temporary change or a reset is needed
                System.out.println("Soul already determined for player " + player.getName().getString() + ". Cannot change directly.");
                // Consider sending a message or resetting first.
            }
        });
    }

    // Main execution method (example usage, might not be directly called like this in all contexts)
    public static void execute(Player sourcePlayer) {
        // Example: Print current soul status
        getCapability(sourcePlayer).ifPresent(cap -> {
            System.out.println("--- Soul Status for " + sourcePlayer.getName().getString() + " ---");
            System.out.println("Determination: " + cap.getSoulValue(SoulType.DETERMINATION));
            System.out.println("Bravery: " + cap.getSoulValue(SoulType.BRAVERY));
            System.out.println("Justice: " + cap.getSoulValue(SoulType.JUSTICE));
            System.out.println("Kindness: " + cap.getSoulValue(SoulType.KINDNESS));
            System.out.println("Patience: " + cap.getSoulValue(SoulType.PATIENCE));
            System.out.println("Integrity: " + cap.getSoulValue(SoulType.INTEGRITY));
            System.out.println("Perseverance: " + cap.getSoulValue(SoulType.PERSEVERANCE));
            System.out.println("Soul Determined: " + cap.isSoulDetermined());
            System.out.println("Current Soul: " + cap.getCurrentSoul().name());
            System.out.println("In Cave: " + cap.isPlayerInCave());
            System.out.println("Interacted Players Count: " + cap.getInteractedPlayers().size()); // Access the set directly
        });
    }
}