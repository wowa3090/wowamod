package net.wowamod.procedures;

import net.wowamod.world.inventory.PortConstructoraMenu;
import net.wowamod.init.Universe3090ModBlocks;

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

public class PortconstructorPriShchielchkiePKMProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		if ((world.getBlockState(BlockPos.containing(x + 1, y, z))).getBlock() == Universe3090ModBlocks.REDSTONECOMMANDBLOCK.get()
				|| (world.getBlockState(BlockPos.containing(x - 1, y, z))).getBlock() == Universe3090ModBlocks.REDSTONECOMMANDBLOCK.get()
				|| (world.getBlockState(BlockPos.containing(x, y, z + 1))).getBlock() == Universe3090ModBlocks.REDSTONECOMMANDBLOCK.get()
				|| (world.getBlockState(BlockPos.containing(x, y, z - 1))).getBlock() == Universe3090ModBlocks.REDSTONECOMMANDBLOCK.get()) {
			if ((world.getBlockState(BlockPos.containing(x + 1, y, z + 1))).getBlock() == Universe3090ModBlocks.INTERFACECONSTRUCTOR.get()
					|| (world.getBlockState(BlockPos.containing(x - 1, y, z - 1))).getBlock() == Universe3090ModBlocks.INTERFACECONSTRUCTOR.get()
					|| (world.getBlockState(BlockPos.containing(x - 1, y, z + 1))).getBlock() == Universe3090ModBlocks.INTERFACECONSTRUCTOR.get()
					|| (world.getBlockState(BlockPos.containing(x + 1, y, z - 1))).getBlock() == Universe3090ModBlocks.INTERFACECONSTRUCTOR.get()
					|| (world.getBlockState(BlockPos.containing(x, y, z + 2))).getBlock() == Universe3090ModBlocks.INTERFACECONSTRUCTOR.get()
					|| (world.getBlockState(BlockPos.containing(x, y, z - 2))).getBlock() == Universe3090ModBlocks.INTERFACECONSTRUCTOR.get()
					|| (world.getBlockState(BlockPos.containing(x - 2, y, z))).getBlock() == Universe3090ModBlocks.INTERFACECONSTRUCTOR.get()
					|| (world.getBlockState(BlockPos.containing(x + 2, y, z))).getBlock() == Universe3090ModBlocks.INTERFACECONSTRUCTOR.get()) {
				if (entity instanceof ServerPlayer _ent) {
					BlockPos _bpos = BlockPos.containing(x, y, z);
					NetworkHooks.openScreen((ServerPlayer) _ent, new MenuProvider() {
						@Override
						public Component getDisplayName() {
							return Component.literal("PortConstructora");
						}

						@Override
						public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
							return new PortConstructoraMenu(id, inventory, new FriendlyByteBuf(Unpooled.buffer()).writeBlockPos(_bpos));
						}
					}, _bpos);
				}
			}
		}
	}
}
