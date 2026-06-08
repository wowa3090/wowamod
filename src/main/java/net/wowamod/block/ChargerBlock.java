package net.wowamod.block;

import net.wowamod.block.entity.ChargerBlockEntity;

import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;

import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.storage.loot.LootParams;
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

import javax.annotation.Nullable;
import java.util.List;
import java.util.Collections;

public class ChargerBlock extends Block implements EntityBlock {
	public ChargerBlock() {
		super(BlockBehaviour.Properties.of().sound(SoundType.METAL).strength(5f, 100f).requiresCorrectToolForDrops().noOcclusion().isRedstoneConductor((bs, br, bp) -> false));
	}

	@Override
	public void appendHoverText(ItemStack itemstack, BlockGetter world, List<Component> list, TooltipFlag flag) {
		super.appendHoverText(itemstack, world, list, flag);
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

	@Override
	public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
		List<ItemStack> dropsOriginal = super.getDrops(state, builder);
		if (!dropsOriginal.isEmpty())
			return dropsOriginal;
		return Collections.singletonList(new ItemStack(this, 1));
	}

	// --- ЛОГИКА DRACONIC EVOLUTION (Быстрый ПКМ без интерфейса) ---
	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (level.isClientSide) return InteractionResult.SUCCESS;

		BlockEntity tileEntity = level.getBlockEntity(pos);
		if (!(tileEntity instanceof ChargerBlockEntity be)) return InteractionResult.PASS;

		ItemStack heldItem = player.getItemInHand(hand);

		// Если рука пустая - достаем заряженный предмет (ПКМ пустой рукой)
		if (heldItem.isEmpty()) {
			for (int i = 0; i < be.getContainerSize(); i++) {
				ItemStack stack = be.getItem(i);
				if (!stack.isEmpty()) {
					player.setItemInHand(hand, stack.copy());
					be.setItem(i, ItemStack.EMPTY);
					be.setChanged();
					level.sendBlockUpdated(pos, state, state, 3);
					return InteractionResult.CONSUME;
				}
			}
			
			// Если блок пуст и игрок нажал Shift+ПКМ - открываем GUI (для совместимости)
			if (player.isShiftKeyDown() && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
				NetworkHooks.openScreen(serverPlayer, be, pos);
			}
			return InteractionResult.CONSUME;
		} 
		// Если в руке предмет - пытаемся вложить его в зарядник
		else {
			boolean isEnergyItem = heldItem.getCapability(ForgeCapabilities.ENERGY).map(IEnergyStorage::canReceive).orElse(false);
			
			if (isEnergyItem) {
				for (int i = 0; i < be.getContainerSize(); i++) {
					if (be.getItem(i).isEmpty()) {
						be.setItem(i, heldItem.copy());
						player.setItemInHand(hand, ItemStack.EMPTY);
						be.setChanged();
						level.sendBlockUpdated(pos, state, state, 3);
						return InteractionResult.CONSUME;
					}
				}
			}
			
			// Если предмет нельзя зарядить или места нет - открываем GUI
			if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
				NetworkHooks.openScreen(serverPlayer, be, pos);
			}
			return InteractionResult.CONSUME;
		}
	}

	// --- ПОДКЛЮЧАЕМ МЕТОД ОБНОВЛЕНИЯ (TICK) ДЛЯ ЗАРЯДКИ ---
	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (!level.isClientSide) {
			return (lvl, pos, st, be) -> {
				if (be instanceof ChargerBlockEntity charger) {
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