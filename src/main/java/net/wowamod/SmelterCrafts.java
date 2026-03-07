package net.wowamod;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

// Импорт твоих предметов
import net.wowamod.init.Universe3090ModItems;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = "universe3090", bus = Mod.EventBusSubscriber.Bus.MOD)
public class SmelterCrafts {

    public static MenuType<SmelterMenu> SMELTER_MENU;

    @SubscribeEvent
    public static void onRegister(RegisterEvent event) {
        if (event.getRegistryKey().equals(ForgeRegistries.Keys.MENU_TYPES)) {
            SMELTER_MENU = IForgeMenuType.create(SmelterMenu::new);
            event.register(ForgeRegistries.Keys.MENU_TYPES, new ResourceLocation("universe3090", "smelter_menu"), () -> SMELTER_MENU);
        }
    }

    @Mod.EventBusSubscriber(modid = "universe3090", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientSetup {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                MenuScreens.register(SMELTER_MENU, SmelterScreen::new);
            });
        }
    }

    public static final List<SmelterRecipe> RECIPES = new ArrayList<>();

    // БЕЗОПАСНАЯ ЗАГРУЗКА РЕЦЕПТОВ (ПОСЛЕ ТОГО, КАК ПРЕДМЕТЫ ПОЯВИЛИСЬ В ИГРЕ)
    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // Твой кастомный рецепт:
            // Aluminium + Dark Iron + Copper Plate + Ring = Combined Alloy
            addRecipe(
                Ingredient.of(Universe3090ModItems.ALUMINIUM_PLATE.get()), 
                Ingredient.of(Universe3090ModItems.DARKIRONPLASTINE.get()), 
                Ingredient.of(Universe3090ModItems.COPPERPLATE.get()), 
                Ingredient.of(Universe3090ModItems.RING.get()), 
                new ItemStack(Universe3090ModItems.COMBINED_ALLOY.get(), 1), // Здесь можно указать количество получаемых сплавов (например 1)
                15000, 
                300
            );
            
            // Если захочешь добавить еще рецепты, просто копируй функцию addRecipe сюда
        });
    }

    public static void addRecipe(Ingredient x, Ingredient y, Ingredient z, Ingredient catalyst, ItemStack output, int energyCost, int processTime) {
        RECIPES.add(new SmelterRecipe(x, y, z, catalyst, output, energyCost, processTime));
    }

    public static SmelterRecipe getRecipe(ItemStack x, ItemStack y, ItemStack z, ItemStack catalyst) {
        for (SmelterRecipe recipe : RECIPES) {
            if (recipe.matches(x, y, z, catalyst)) return recipe;
        }
        return null;
    }

    public static class SmelterRecipe {
        public final Ingredient x, y, z, catalyst;
        public final ItemStack output;
        public final int energyCost, processTime;

        public SmelterRecipe(Ingredient x, Ingredient y, Ingredient z, Ingredient catalyst, ItemStack output, int energyCost, int processTime) {
            this.x = x; this.y = y; this.z = z; this.catalyst = catalyst;
            this.output = output; this.energyCost = energyCost; this.processTime = processTime;
        }

        public boolean matches(ItemStack inX, ItemStack inY, ItemStack inZ, ItemStack inCat) {
            return x.test(inX) && y.test(inY) && z.test(inZ) && catalyst.test(inCat);
        }
    }
}