package net.wowamod.block;

import net.wowamod.block.entity.ChargerBlockEntity;
import net.wowamod.init.Universe3090ModBlocks; // Импорт реестра блоков

import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;

import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams; // Для получения Entity при разрушении
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.ChatFormatting;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Collections;

public class ChargerBlock extends Block implements EntityBlock {
	public ChargerBlock() {
		super(BlockBehaviour.Properties.of().sound(SoundType.METAL).strength(5f, 100f).requiresCorrectToolForDrops().noOcclusion().isRedstoneConductor((bs, br, bp) -> false));
	}

	// --- ОТОБРАЖЕНИЕ ЭНЕРГИИ В ТУЛТИПЕ ---
	@Override
	public void appendHoverText(ItemStack itemstack, BlockGetter world, List<Component> list, TooltipFlag flag) {
		super.appendHoverText(itemstack, world, list, flag);
		
		int energy = 0;
		// Проверяем, есть ли в предмете сохраненная NBT-информация от блока
		if (itemstack.hasTag() && itemstack.getTag().contains("BlockEntityTag")) {
			CompoundTag beTag = itemstack.getTag().getCompound("BlockEntityTag");
			if (beTag.contains("energyStorage")) {
				energy = beTag.getInt("energyStorage");
			}
		}
		
		// Рассчитываем процент (1,000,000 FE = 100%)
		int percent = energy / 10000; 
		list.add(Component.literal("Энергия: " + energy + " / 1000000 FE (" + percent + "%)").withStyle(ChatFormatting.AQUA));
	}

	@Override
	public VoxelShape getVisualShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return Shapes.empty();
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return box(0, 0, 0, 16, 14, 16);
	}

	@Override
	public boolean canHarvestBlock(BlockState state, BlockGetter world, BlockPos pos, Player player) {
		if (player.getInventory().getSelected().getItem() instanceof PickaxeItem tieredItem)
			return tieredItem.getTier().getLevel() >= 2;
		return false;
	}

	// --- СОХРАНЕНИЕ ЭНЕРГИИ ПРИ РАЗРУШЕНИИ БЛОКА ---
	@Override
	public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
		List<ItemStack> dropsOriginal = super.getDrops(state, builder);
		List<ItemStack> finalDrops = !dropsOriginal.isEmpty() ? dropsOriginal : Collections.singletonList(new ItemStack(this, 1));

		BlockEntity be = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
		if (be instanceof ChargerBlockEntity charger) {
			// Получаем энергию перед тем, как блок исчезнет
			int energy = charger.getCapability(ForgeCapabilities.ENERGY).map(IEnergyStorage::getEnergyStored).orElse(0);
			if (energy > 0) {
				for (ItemStack drop : finalDrops) {
					if (drop.getItem() == this.asItem()) {
						// Записываем энергию в тег BlockEntityTag (игра автоматически загрузит её при установке блока!)
						CompoundTag tag = new CompoundTag();
						tag.putInt("energyStorage", energy);
						drop.addTagElement("BlockEntityTag", tag);
					}
				}
			}
		}
		return finalDrops;
	}

	// --- ИСПРАВЛЕННАЯ ЛОГИКА ВСТАВКИ И УМНОГО ИЗВЛЕЧЕНИЯ ---
	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (level.isClientSide) return InteractionResult.SUCCESS;

		BlockEntity tileEntity = level.getBlockEntity(pos);
		if (!(tileEntity instanceof ChargerBlockEntity be)) return InteractionResult.PASS;

		ItemStack heldItem = player.getItemInHand(hand);

		// Если рука пустая - достаем предмет с учетом ПРИОРИТЕТА
		if (heldItem.isEmpty()) {
			int extractSlot = -1;
			int extractPriority = -1;

			// Ищем лучший предмет для выдачи игроку
			for (int i = 0; i < be.getContainerSize(); i++) {
				ItemStack slotStack = be.getItem(i);
				if (slotStack.isEmpty()) continue;

				int priority = 0;
				if (slotStack.getItem() == Universe3090ModBlocks.REDSTONE_9X_ENERGIZED.get().asItem()) {
					priority = 10; // Полностью заряженный блок = Высший приоритет
				} else {
					IEnergyStorage itemEnergy = slotStack.getCapability(ForgeCapabilities.ENERGY).orElse(null);
					if (itemEnergy != null && itemEnergy.getEnergyStored() >= itemEnergy.getMaxEnergyStored()) {
						priority = 10; // Инструмент 100% заряда = Высший приоритет
					} else if (itemEnergy != null && itemEnergy.getEnergyStored() > 0) {
						priority = 5; // Частично заряженный инструмент
					} else if (slotStack.getItem() == Universe3090ModBlocks.REDSTONE_9X.get().asItem()) {
						int charge = slotStack.hasTag() ? slotStack.getTag().getInt("Charge") : 0;
						priority = charge > 0 ? 5 : 0; // Частично заряженный блок
					}
				}

				if (priority > extractPriority) {
					extractPriority = priority;
					extractSlot = i;
				}
			}

			// Выдаем предмет
			if (extractSlot != -1) {
				ItemStack stack = be.getItem(extractSlot);
				player.setItemInHand(hand, stack.copy());
				be.setItem(extractSlot, ItemStack.EMPTY);
				be.setChanged();
				level.sendBlockUpdated(pos, state, state, 3);
				return InteractionResult.CONSUME;
			}
			
			if (player.isShiftKeyDown() && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
				NetworkHooks.openScreen(serverPlayer, be, pos);
			}
			return InteractionResult.CONSUME;
		} 
		// Если в руке предмет - пытаемся вложить его в зарядник
		else {
			boolean isEnergyItem = heldItem.getCapability(ForgeCapabilities.ENERGY).map(IEnergyStorage::canReceive).orElse(false);
			boolean isCraftingItem = heldItem.getItem() == Universe3090ModBlocks.REDSTONE_9X.get().asItem() || 
									 heldItem.getItem() == Universe3090ModBlocks.REDSTONE_9X_ENERGIZED.get().asItem();
			
			if (isEnergyItem || isCraftingItem) {
				for (int i = 0; i < be.getContainerSize(); i++) {
					ItemStack slotItem = be.getItem(i);
					
					if (slotItem.isEmpty()) {
						be.setItem(i, heldItem.copy());
						player.setItemInHand(hand, ItemStack.EMPTY);
						be.setChanged();
						level.sendBlockUpdated(pos, state, state, 3);
						return InteractionResult.CONSUME;
					} 
					else if (isCraftingItem && ItemStack.isSameItemSameTags(slotItem, heldItem)) {
						int spaceLeft = slotItem.getMaxStackSize() - slotItem.getCount();
						if (spaceLeft > 0) {
							int toMove = Math.min(spaceLeft, heldItem.getCount());
							slotItem.grow(toMove);
							heldItem.shrink(toMove);
							be.setChanged();
							level.sendBlockUpdated(pos, state, state, 3);
							return InteractionResult.CONSUME;
						}
					}
				}
			}
			
			if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
				NetworkHooks.openScreen(serverPlayer, be, pos);
			}
			return InteractionResult.CONSUME;
		}
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (!level.isClientSide) {
			return (lvl, pos, st, entity) -> {
				if (entity instanceof ChargerBlockEntity charger) {
					ChargerBlockEntity.serverTick(lvl, pos, st, charger);
				}
			};
		}
		return null;
	}

	@Override
	public MenuProvider getMenuProvider(BlockState state, Level worldIn, BlockPos pos) {
		BlockEntity tileEntity = worldIn.getBlockEntity(pos);
		return tileEntity instanceof MenuProvider menuProvider ? menuProvider : null;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new ChargerBlockEntity(pos, state);
	}

	@Override
	public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			BlockEntity blockEntity = world.getBlockEntity(pos);
			if (blockEntity instanceof ChargerBlockEntity be) {
				// При разрушении выкидываем предметы из слотов на землю (энергия НЕ выбрасывается, она сохраняется в коде getDrops)
				Containers.dropContents(world, pos, be);
				world.updateNeighbourForOutputSignal(pos, this);
			}
			super.onRemove(state, world, pos, newState, isMoving);
		}
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState blockState, Level world, BlockPos pos) {
		BlockEntity tileentity = world.getBlockEntity(pos);
		if (tileentity instanceof ChargerBlockEntity be)
			return AbstractContainerMenu.getRedstoneSignalFromContainer(be);
		else
			return 0;
	}
}