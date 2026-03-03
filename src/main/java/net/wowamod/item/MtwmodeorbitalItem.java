package net.wowamod.item;

import software.bernie.geckolib.util.GeckoLibUtil;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.GeoItem;

import net.wowamod.item.renderer.MtwmodeorbitalItemRenderer;
import net.wowamod.OrbitalBeamEntity; // Импорт вашей сущности
import net.wowamod.ModEntityBeamOrbital;

import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.Item;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.network.chat.Component;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.player.LocalPlayer;

import java.util.function.Consumer;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

public class MtwmodeorbitalItem extends Item implements GeoItem {
  private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
  public String animationprocedure = "empty";
  public static ItemDisplayContext transformType;

  // Параметры удара (теперь public static)
  public static final int COOLDOWN_TICKS = 200; // 10 сек
  public static final float STRIKE_RADIUS = 6.0f;
  public static final double MAX_RANGE = 50.0;
  private static final float DAMAGE = 40.0f; // (Этот урон теперь наносится сущностью)

  public MtwmodeorbitalItem() {
    super(new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC));
  }

  // ПКМ: запуск удара
  @Override
  public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
    ItemStack stack = player.getItemInHand(hand);

    // Выполняем рейкаст, чтобы найти, куда смотрит игрок
    BlockHitResult hit = raycast(player, level, MAX_RANGE);

    // Проверяем, что попали в блок
    if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
      
      // Логику выполняем ТОЛЬКО на сервере
      if (!level.isClientSide) {
        double x = hit.getBlockPos().getX() + 0.5;
        double y = hit.getBlockPos().getY() + 1.0; 
        double z = hit.getBlockPos().getZ() + 0.5;

        // --- ЛОГИКА ОРБИТАЛЬНОГО УДАРА ---
        // Создаем ВАШУ сущность луча
        OrbitalBeamEntity beam = new OrbitalBeamEntity(ModEntityBeamOrbital.ORBITAL_BEAM.get(), level, x, y, z, STRIKE_RADIUS); // НОВОЕ
		level.addFreshEntity(beam);
        
        // Воспроизводим звук "активации" (звуки удара теперь в OrbitalBeamEntity)
        level.playSound(null, x, y, z, SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 1.0f, 0.8f);

        // Накладываем кулдаун
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
      }
      
      // Возвращаем "успех" на обеих сторонах
      return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    // Если никуда не попали, возвращаем "pass"
    return InteractionResultHolder.pass(stack);
  }

  // Метод сделан public static, чтобы AreaRendererMTW мог его использовать
  public static BlockHitResult raycast(Player player, Level level, double range) {
    ClipContext ctx = new ClipContext(
        player.getEyePosition(),
        player.getEyePosition().add(player.getViewVector(1.0f).scale(range)),
        ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player);
    HitResult hr = level.clip(ctx);
    return hr instanceof BlockHitResult bhr ? bhr : null;
  }

  // GeckoLib и клиентский рендер предмета в руке
  @Override
  public void initializeClient(Consumer<IClientItemExtensions> consumer) {
    super.initializeClient(consumer);
    consumer.accept(new IClientItemExtensions() {
      private final BlockEntityWithoutLevelRenderer renderer = new MtwmodeorbitalItemRenderer();

      @Override
      public BlockEntityWithoutLevelRenderer getCustomRenderer() {
        return renderer;
      }

      @Override 
      public boolean applyForgeHandTransform(PoseStack poseStack, LocalPlayer player, HumanoidArm arm, ItemStack itemInHand, float partialTick, float equipProcess, float swingProcess) {
        int i = arm == HumanoidArm.RIGHT ? 1 : -1;
        poseStack.translate(i * 0.56F, -0.52F, -0.72F);
        if (player.getUseItem() == itemInHand) {
          poseStack.translate(0.05, 0.05, 0.05);
        }
        return true;
      }
    });
  }

  public void getTransformType(ItemDisplayContext type) {
    MtwmodeorbitalItem.transformType = type;
  }

  private PlayState idlePredicate(AnimationState event) {
    if (transformType != null) {
      if (this.animationprocedure.equals("empty")) {
        event.getController().setAnimation(RawAnimation.begin().thenLoop("animation.M.T.W_animated.active"));
        return PlayState.CONTINUE;
      }
    }
    return PlayState.STOP;
  }

  private PlayState procedurePredicate(AnimationState event) {
    if (transformType != null) {
      if (!this.animationprocedure.equals("empty") && event.getController().getAnimationState() == AnimationController.State.STOPPED) {
        event.getController().setAnimation(RawAnimation.begin().thenPlay(this.animationprocedure));
        // Логика сброса анимации (если нужна)
        // if (event.getController().getAnimationState() == AnimationController.State.STOPPED) {
        //   this.animationprocedure = "empty";
        //   event.getController().forceAnimationReset();
        // }
      } else if (this.animationprocedure.equals("empty")) {
        return PlayState.STOP;
      }
    }
    return PlayState.CONTINUE;
  }

  @Override
  public void registerControllers(AnimatableManager.ControllerRegistrar data) {
    AnimationController procedureController = new AnimationController(this, "procedureController", 0, this::procedurePredicate);
    data.add(procedureController);
    AnimationController idleController = new AnimationController(this, "idleController", 0, this::idlePredicate);
    data.add(idleController);
  }

  @Override
  public AnimatableInstanceCache getAnimatableInstanceCache() {
    return this.cache;
  }

  @Override
  public void appendHoverText(ItemStack itemstack, Level world, List<Component> list, TooltipFlag flag) {
    super.appendHoverText(itemstack, world, list, flag);
  }
}
