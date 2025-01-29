package net.wowamod.procedures;

import net.wowamod.world.inventory.ConstructorinterfaceMenu;
import net.wowamod.init.WowamodModBlocks;

import net.minecraftforge.network.NetworkHooks;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.MenuProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;

import io.netty.buffer.Unpooled;

public class ConstructorProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		if ((world.getBlockState(BlockPos.containing(x + 1, y, z))).getBlock() == WowamodModBlocks.REDSTONECOMMANDBLOCK.get() || (world.getBlockState(BlockPos.containing(x - 1, y, z))).getBlock() == WowamodModBlocks.REDSTONECOMMANDBLOCK.get()
				|| (world.getBlockState(BlockPos.containing(x, y, z + 1))).getBlock() == WowamodModBlocks.REDSTONECOMMANDBLOCK.get()
				|| (world.getBlockState(BlockPos.containing(x, y, z - 1))).getBlock() == WowamodModBlocks.REDSTONECOMMANDBLOCK.get()) {
			if ((world.getBlockState(BlockPos.containing(x + 1, y + 1, z))).getBlock() == WowamodModBlocks.MAGICIRONBLOCK.get() || (world.getBlockState(BlockPos.containing(x - 1, y + 1, z))).getBlock() == WowamodModBlocks.MAGICIRONBLOCK.get()
					|| (world.getBlockState(BlockPos.containing(x, y + 1, z + 1))).getBlock() == WowamodModBlocks.MAGICIRONBLOCK.get()
					|| (world.getBlockState(BlockPos.containing(x, y + 1, z - 1))).getBlock() == WowamodModBlocks.MAGICIRONBLOCK.get()) {
				if ((world.getBlockState(BlockPos.containing(x + 1, y + -1, z))).getBlock() == WowamodModBlocks.MAGICIRONBLOCK.get() || (world.getBlockState(BlockPos.containing(x - 1, y + -1, z))).getBlock() == WowamodModBlocks.MAGICIRONBLOCK.get()
						|| (world.getBlockState(BlockPos.containing(x, y + -1, z + 1))).getBlock() == WowamodModBlocks.MAGICIRONBLOCK.get()
						|| (world.getBlockState(BlockPos.containing(x, y + -1, z - 1))).getBlock() == WowamodModBlocks.MAGICIRONBLOCK.get()) {
					if (entity instanceof ServerPlayer _ent) {
						BlockPos _bpos = BlockPos.containing(x, y, z);
						NetworkHooks.openScreen((ServerPlayer) _ent, new MenuProvider() {
							@Override
							public Component getDisplayName() {
								return Component.literal("Constructorinterface");
							}

							@Override
							public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
								return new ConstructorinterfaceMenu(id, inventory, new FriendlyByteBuf(Unpooled.buffer()).writeBlockPos(_bpos));
							}
						}, _bpos);
					}
				}
			}
		}
	}
}
