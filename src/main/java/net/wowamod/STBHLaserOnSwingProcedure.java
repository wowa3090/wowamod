package net.wowamod.procedures;

import net.wowamod.procedures.STBHAbilities;

import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.Entity;

public class STBHLaserOnSwingProcedure {
    public static void execute(LevelAccessor world, Entity entity, ItemStack itemstack) {
        if (entity == null)
            return;
        
        // ИСПРАВЛЕНО: Добавляем проверку и получаем объект Level
        if (!(world instanceof Level level))
            return;

        if (entity instanceof Player player) {
            EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(
                level, // Используем исправленную переменную 'level'
                player, 
                player.getEyePosition(1.0F), 
                player.getEyePosition(1.0F).add(player.getLookAngle().scale(4.5)),
                new AABB(player.getEyePosition(1.0F), player.getEyePosition(1.0F).add(player.getLookAngle().scale(4.5))).inflate(1.0D),
                (e) -> !e.isSpectator() && e.isPickable()
            );

            if (entityHitResult == null) {
                STBHAbilities.fireLaserBeam(world, entity, itemstack);
            }
        }
    }
}
