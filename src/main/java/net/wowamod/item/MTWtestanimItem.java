package net.wowamod.item;

import software.bernie.geckolib.util.GeckoLibUtil;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.constant.DataTickets;

import net.wowamod.item.renderer.MTWtestanimItemRenderer;

import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.network.chat.Component;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.player.LocalPlayer;

import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.List;

public class MTWtestanimItem extends Item implements GeoItem {
	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
	
	public String animationprocedure = "empty"; 
	
	public static final int MAX_ENERGY = 500000;
	public static final int ENERGY_PER_TICK = 500; 

	private static final RawAnimation ACTIVE_ANIM = RawAnimation.begin().thenLoop("animation.M.T.W_animated.idle");
	private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("animation.M.T.W_animated.idle2");

	public MTWtestanimItem() {
		super(new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC));
	}

	@Override
	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
		super.initializeClient(consumer);
		consumer.accept(new IClientItemExtensions() {
			private final BlockEntityWithoutLevelRenderer renderer = new MTWtestanimItemRenderer();

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return renderer;
			}

			@Override
			public boolean applyForgeHandTransform(PoseStack poseStack, LocalPlayer player, HumanoidArm arm, ItemStack itemInHand, float partialTick, float equipProcess, float swingProcess) {
				int i = arm == HumanoidArm.RIGHT ? 1 : -1;
				poseStack.translate(i * 0.56F, -0.52F, -0.72F);
				return true;
			}
		});
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return slotChanged || oldStack.getItem() != newStack.getItem();
	}

	// =========================================================
	// ЛОГИКА ИСПОЛЬЗОВАНИЯ И ТРАТЫ ЭНЕРГИИ (ПКМ)
	// =========================================================

	@Override
	public int getUseDuration(ItemStack stack) {
		return 72000; 
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.NONE; 
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		IEnergyStorage energy = stack.getCapability(ForgeCapabilities.ENERGY).orElse(null);

		if (energy != null && energy.getEnergyStored() >= ENERGY_PER_TICK) {
			player.startUsingItem(hand);
			stack.getOrCreateTag().putBoolean("isActive", true); 
			return InteractionResultHolder.consume(stack);
		}
		
		return InteractionResultHolder.fail(stack);
	}

	@Override
	public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int count) {
		if (entity instanceof Player player) {
			IEnergyStorage energy = stack.getCapability(ForgeCapabilities.ENERGY).orElse(null);
			
			if (energy != null && energy.getEnergyStored() >= ENERGY_PER_TICK) {
				energy.extractEnergy(ENERGY_PER_TICK, false); 
				stack.getOrCreateTag().putBoolean("isActive", true);
			} else {
				player.stopUsingItem();
				stack.getOrCreateTag().putBoolean("isActive", false);
			}
		}
	}

	@Override
	public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
		stack.getOrCreateTag().putBoolean("isActive", false);
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
		if (entity instanceof Player player) {
			if (stack.hasTag() && stack.getTag().getBoolean("isActive")) {
				if (player.getUseItem().getItem() != this) {
					stack.getOrCreateTag().putBoolean("isActive", false);
				}
			}
		}
	}

	// =========================================================
	// GECKOLIB АНИМАЦИИ (ДИНАМИЧЕСКИЙ ПЕРЕХОД)
	// =========================================================

	private PlayState animPredicate(AnimationState<MTWtestanimItem> event) {
		if (!this.animationprocedure.equals("empty")) {
			event.getController().setAnimation(RawAnimation.begin().thenPlay(this.animationprocedure));
			if (event.getController().getAnimationState() == AnimationController.State.STOPPED) {
				this.animationprocedure = "empty";
			}
			return PlayState.CONTINUE;
		}

		ItemStack stack = event.getData(DataTickets.ITEMSTACK);
		boolean isActive = stack != null && stack.hasTag() && stack.getTag().getBoolean("isActive");

		if (isActive) {
			// ИСПРАВЛЕНИЕ: Старт стрельбы — моментальный отклик (0 тиков)
			event.getController().transitionLength(0);
			return event.setAndContinue(ACTIVE_ANIM);
		} else {
			// ИСПРАВЛЕНИЕ: Конец стрельбы — плавное опускание костей (15 тиков = 0.75 сек)
			event.getController().transitionLength(15);
			return event.setAndContinue(IDLE_ANIM);
		}
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar data) {
		// Базовый контроллер (время перехода будет перезаписываться динамически выше)
		data.add(new AnimationController<>(this, "controller", 0, this::animPredicate));
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return this.cache;
	}

	// =========================================================
	// ЭНЕРГОСИСТЕМА (Хранилище и UI)
	// =========================================================

	public static class ItemEnergyStorage implements IEnergyStorage {
		private final ItemStack stack;
		private final int capacity;

		public ItemEnergyStorage(ItemStack stack, int capacity) {
			this.stack = stack;
			this.capacity = capacity;
		}
		@Override public int receiveEnergy(int maxRec, boolean sim) {
			int e = getEnergyStored(); int r = Math.min(capacity - e, maxRec);
			if (!sim && r > 0) stack.getOrCreateTag().putInt("Energy", e + r); return r;
		}
		@Override public int extractEnergy(int maxExt, boolean sim) {
			int e = getEnergyStored(); int ext = Math.min(e, maxExt);
			if (!sim && ext > 0) stack.getOrCreateTag().putInt("Energy", e - ext); return ext;
		}
		@Override public int getEnergyStored() { return stack.hasTag() ? stack.getTag().getInt("Energy") : 0; }
		@Override public int getMaxEnergyStored() { return capacity; }
		@Override public boolean canExtract() { return true; }
		@Override public boolean canReceive() { return true; }
	}

	@Nullable
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
		return new ICapabilityProvider() {
			private final LazyOptional<IEnergyStorage> opt = LazyOptional.of(() -> new ItemEnergyStorage(stack, MAX_ENERGY));
			@NotNull @Override public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
				return cap == ForgeCapabilities.ENERGY ? opt.cast() : LazyOptional.empty();
			}
		};
	}

	@Override
	public void appendHoverText(ItemStack stack, Level level, List<Component> list, TooltipFlag flag) {
		int energy = stack.hasTag() ? stack.getTag().getInt("Energy") : 0;
		list.add(Component.literal("Энергия: " + energy + " / " + MAX_ENERGY + " FE").withStyle(ChatFormatting.AQUA));
		list.add(Component.literal("Зажмите ПКМ для использования").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
		super.appendHoverText(stack, level, list, flag);
	}

	@Override public boolean isBarVisible(ItemStack stack) { return true; }
	@Override public int getBarWidth(ItemStack stack) {
		return stack.getCapability(ForgeCapabilities.ENERGY)
				.map(e -> Math.round(13.0F * e.getEnergyStored() / (float) e.getMaxEnergyStored())).orElse(0);
	}
	@Override public int getBarColor(ItemStack stack) { return 0x00FFFF; }
}