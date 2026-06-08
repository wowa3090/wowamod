package net.wowamod.block.entity;

import net.wowamod.init.Universe3090ModBlockEntities;

import net.minecraftforge.items.wrapper.SidedInvWrapper;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.Capability;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

// Добавленные импорты для рендера
import net.minecraft.network.Connection;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.stream.IntStream;

public class ChargerBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer {
	private NonNullList<ItemStack> stacks = NonNullList.<ItemStack>withSize(9, ItemStack.EMPTY);
	private final LazyOptional<? extends IItemHandler>[] handlers = SidedInvWrapper.create(this, Direction.values());

	// Параметры Draconic Evolution: Буфер 1,000,000 FE, Скорость ввода/вывода 100,000 FE в тик
	private final EnergyStorage energyStorage = new EnergyStorage(1000000, 100000, 100000, 0) {
		@Override
		public int receiveEnergy(int maxReceive, boolean simulate) {
			int retval = super.receiveEnergy(maxReceive, simulate);
			if (!simulate) {
				setChanged();
				level.sendBlockUpdated(worldPosition, level.getBlockState(worldPosition), level.getBlockState(worldPosition), 2);
			}
			return retval;
		}

		@Override
		public int extractEnergy(int maxExtract, boolean simulate) {
			int retval = super.extractEnergy(maxExtract, simulate);
			if (!simulate) {
				setChanged();
				level.sendBlockUpdated(worldPosition, level.getBlockState(worldPosition), level.getBlockState(worldPosition), 2);
			}
			return retval;
		}
	};

	public ChargerBlockEntity(BlockPos position, BlockState state) {
		super(Universe3090ModBlockEntities.CHARGER.get(), position, state);
	}

	// --- ТИК (Передача энергии в предметы) ---
	public static void serverTick(Level level, BlockPos pos, BlockState state, ChargerBlockEntity blockEntity) {
		boolean isDirty = false;
		int energyStored = blockEntity.energyStorage.getEnergyStored();

		if (energyStored > 0) {
			for (int i = 0; i < blockEntity.stacks.size(); i++) {
				ItemStack stack = blockEntity.stacks.get(i);
				if (!stack.isEmpty()) {
					IEnergyStorage itemEnergy = stack.getCapability(ForgeCapabilities.ENERGY).orElse(null);
					
					// Если предмет принимает энергию
					if (itemEnergy != null && itemEnergy.canReceive()) {
						int maxTransfer = Math.min(energyStored, 100000); // Скорость передачи внутрь предмета
						int received = itemEnergy.receiveEnergy(maxTransfer, false);
						
						if (received > 0) {
							blockEntity.energyStorage.extractEnergy(received, false);
							energyStored -= received;
							isDirty = true;
						}
					}
				}
				// Выходим из цикла, если кончилась энергия
				if (energyStored <= 0) break;
			}
		}

		// Сохраняем изменения
		if (isDirty) {
			blockEntity.setChanged();
		}
	}

	@Override
	public void load(CompoundTag compound) {
		super.load(compound);
		if (!this.tryLoadLootTable(compound))
			this.stacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
		ContainerHelper.loadAllItems(compound, this.stacks);
		if (compound.get("energyStorage") instanceof IntTag intTag)
			energyStorage.deserializeNBT(intTag);
	}

	@Override
	public void saveAdditional(CompoundTag compound) {
		super.saveAdditional(compound);
		if (!this.trySaveLootTable(compound)) {
			ContainerHelper.saveAllItems(compound, this.stacks);
		}
		compound.put("energyStorage", energyStorage.serializeNBT());
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	// Оставляем только ОДИН правильный метод getUpdateTag (ванильный)
	@Override
	public CompoundTag getUpdateTag() {
		return this.saveWithFullMetadata();
	}

	@Override
	public int getContainerSize() {
		return stacks.size();
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack itemstack : this.stacks)
			if (!itemstack.isEmpty())
				return false;
		return true;
	}

	@Override
	public Component getDefaultName() {
		return Component.literal("charger");
	}

	@Override
	public int getMaxStackSize() {
		return 64;
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inventory) {
		return ChestMenu.oneRow(id, inventory);
	}

	@Override
	public Component getDisplayName() {
		return Component.literal("Charger");
	}

	@Override
	protected NonNullList<ItemStack> getItems() {
		return this.stacks;
	}

	@Override
	protected void setItems(NonNullList<ItemStack> stacks) {
		this.stacks = stacks;
	}

	@Override
	public boolean canPlaceItem(int index, ItemStack stack) {
		return true;
	}

	@Override
	public int[] getSlotsForFace(Direction side) {
		return IntStream.range(0, this.getContainerSize()).toArray();
	}

	@Override
	public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
		return this.canPlaceItem(index, stack);
	}

	@Override
	public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
		return true;
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
		if (!this.remove && facing != null && capability == ForgeCapabilities.ITEM_HANDLER)
			return handlers[facing.ordinal()].cast();
		if (!this.remove && capability == ForgeCapabilities.ENERGY)
			return LazyOptional.of(() -> energyStorage).cast();
		return super.getCapability(capability, facing);
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
		for (LazyOptional<? extends IItemHandler> handler : handlers)
			handler.invalidate();
	}

	// Читаем пакет, когда он приходит на клиент, чтобы обновить инвентарь для рендера
	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
		super.onDataPacket(net, pkt);
		if (pkt.getTag() != null) {
			this.load(pkt.getTag());
		}
	}

	// Увеличиваем область рендера, чтобы предмет не исчезал, когда отводишь взгляд
	@Override
	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition).inflate(1.0D); 
	}
}