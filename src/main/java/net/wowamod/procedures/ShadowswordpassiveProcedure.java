package net.wowamod.procedures;

import net.wowamod.init.Universe3090ModItems;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.server.level.ServerLevel;

@Mod.EventBusSubscriber
public class ShadowswordpassiveProcedure {
    
    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        LivingEntity killedEntity = event.getEntity();
        Entity killer = event.getSource().getEntity();
        
        // Двойная проверка на клиентсайд
        if (killedEntity.level().isClientSide()) return;
        
        // Проверяем необходимые условия
        if (killer == null) return;
        if (!(killer instanceof LivingEntity)) return;
        
        execute(killedEntity.level(), killedEntity.getX(), killedEntity.getY(), killedEntity.getZ(), killedEntity, killer);
    }

    private static void execute(LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity) {
        // Дополнительная проверка на серверную сторону
        if (world.isClientSide()) return;
        
        LivingEntity killer = (LivingEntity) sourceentity;
        ItemStack mainHandItem = killer.getMainHandItem();
        
        // Проверяем правильность предмета (добавим логирование)
        if (mainHandItem.isEmpty() || mainHandItem.getItem() != Universe3090ModItems.SHADOWBLADE.get()) {
            return;
        }

        // Проверяем тип убитой сущности (используем EntityType)
        if (entity.getType() == EntityType.SKELETON) {
            spawnSkull(world, x, y + 0.5, z, new ItemStack(Items.SKELETON_SKULL));
        } 
        else if (entity.getType() == EntityType.WITHER_SKELETON) {
            spawnSkull(world, x, y + 0.5, z, new ItemStack(Blocks.WITHER_SKELETON_SKULL));
        }
    }
    
    private static void spawnSkull(LevelAccessor world, double x, double y, double z, ItemStack skull) {
        if (!(world instanceof ServerLevel serverWorld)) return;
        
        ItemEntity skullEntity = new ItemEntity(serverWorld, x, y, z, skull);
        skullEntity.setPickUpDelay(20); // Увеличим задержку
        skullEntity.setDeltaMovement(0, 0.1, 0); // Добавим небольшое движение
        
        serverWorld.addFreshEntity(skullEntity);
    }
}