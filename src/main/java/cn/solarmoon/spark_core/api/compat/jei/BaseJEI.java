package cn.solarmoon.spark_core.api.compat.jei;

import cn.solarmoon.spark_core.api.compat.jei.element.EmptyBackground;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 简便的jei创建器，需要自己添加@JeiPlugin标识以启用
 */
@SuppressWarnings("unchecked")
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class BaseJEI implements IModPlugin {

    protected IGuiHelper guiHelper;
    private final List<JEIBuilder> jeiBuilders;

    public BaseJEI() {
        this.jeiBuilders = new ArrayList<>();
    }

    public abstract void register();

    public abstract String getModId();

    public abstract ResourceLocation getPluginUid(); //这个不可预设，非常的迷呀

    /**
     * 注册新jei配方渲染类型
     */
    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        guiHelper = registry.getJeiHelpers().getGuiHelper();
        register();
        for (JEIBuilder jeiBuilder : jeiBuilders) {
            if (jeiBuilder.getRecipeCategory() != null) {
                registry.addRecipeCategories(jeiBuilder.getRecipeCategory());
            }
        }
    }

    /**
     * 将现有配方绑定jei
     */
    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        for (JEIBuilder jeiBuilder : jeiBuilders) {
            if (jeiBuilder.getRecipeType() != null && jeiBuilder.existRecipeType == null) {
                registration.addRecipes(jeiBuilder.getRecipeType(), jeiBuilder.getClientRecipes());
            }
        }
    }

    /**
     * 为配方绑定物品
     */
    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        for (JEIBuilder jeiBuilder : jeiBuilders) {
            for (ItemStack stack : jeiBuilder.getCatalystStacks()) {
                if (jeiBuilder.getRecipeType() != null) {
                    registration.addRecipeCatalyst(stack, jeiBuilder.getRecipeType());
                }
            }
        }
    }

    public void add(JEIBuilder... jeiBuilder) {
        jeiBuilders.addAll(Arrays.asList(jeiBuilder));
    }

    public JEIBuilder builder() {
        return new JEIBuilder(getModId(), guiHelper);
    }

    public static class JEIBuilder {

        private RecipeType<?> recipeType;
        private IRecipeCategory<?> recipeCategory;
        private net.minecraft.world.item.crafting.RecipeType<?> mcRecipeType;
        private Component title;
        private IDrawable background;
        private IDrawable icon;
        private final String modId;
        private final IGuiHelper guiHelper;
        private ItemStack iconItem;
        private RecipeType<?> existRecipeType;

        private final List<ItemStack> catalystStacks;

        public JEIBuilder(String modId, IGuiHelper guiHelper) {
            this.modId = modId;
            this.guiHelper = guiHelper;
            this.catalystStacks = new ArrayList<>();
        }

        /**
         * 把配方绑定在物品上
         */
        public JEIBuilder addRecipeCatalyst(ItemStack itemStack) {
            catalystStacks.add(itemStack);
            return this;
        }

        /**
         * 把配方绑定在物品上
         */
        public JEIBuilder addRecipeCatalyst(ItemLike item) {
            catalystStacks.add(new ItemStack(item));
            return this;
        }

        /**
         * 把配方绑定在物品上
         */
        public JEIBuilder addRecipeCatalyst(Ingredient ingredient) {
            catalystStacks.addAll(Arrays.asList(ingredient.getItems()));
            return this;
        }

        /**
         * 绑定jei配方独特的界面渲染（实现JEICategory）
         */
        public JEIBuilder boundCategory(IRecipeCategory<?> recipeCategory) {
            this.recipeCategory = recipeCategory;
            if (recipeCategory instanceof BaseJEICategory<?> jeiCategory) {
                this.recipeCategory = jeiCategory.setJeiBuilder(this);
            }
            return this;
        }

        /**
         * 把该配方类型的所有配方与jei绑定，这里输入的并非jei配方类型，具体的创建在build中，区别于同方法的另一个
         */
        public <T extends Recipe<?>> JEIBuilder recipeType(net.minecraft.world.item.crafting.RecipeType<T> recipeType) {
            this.mcRecipeType = recipeType;
            return this;
        }

        /**
         * 直接绑定已有配方类型
         */
        public JEIBuilder recipeType(RecipeType<?> recipeType) {
            this.existRecipeType = recipeType;
            return this;
        }

        public JEIBuilder background(IDrawable background) {
            this.background = background;
            return this;
        }

        public JEIBuilder emptyBackground(int width, int height) {
            background(new EmptyBackground(width, height));
            return this;
        }

        /**
         * 不填默认输出icon显示的物品的名称，icon也没有就显示绑定的第一个物品名称
         */
        public JEIBuilder title(Component title) {
            this.title = title;
            return this;
        }

        /**
         * 不填默认输出绑定的物品中的第一个物品的样式
         */
        public JEIBuilder icon(IDrawable icon) {
            this.icon = icon;
            return this;
        }

        /**
         * 不填默认输出绑定的物品中的第一个物品的样式
         */
        public JEIBuilder icon(ItemLike item) {
            icon(guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(item)));
            this.iconItem = new ItemStack(item);
            return this;
        }

        /**
         * 最终构建，不是添加配方类型无需构建（比如绑定已有配方）
         */
        public <T extends Recipe<?>> JEIBuilder build(String id, Class<? extends T> mcRecipeClass) {
            recipeType = RecipeType.create(modId, id, mcRecipeClass);
            if (title == null) {
                if (iconItem != null) title = iconItem.getHoverName();
                else if (!catalystStacks.isEmpty()) title = catalystStacks.get(0).getHoverName();
            }
            if (icon == null) {
                if (!catalystStacks.isEmpty()) icon(catalystStacks.get(0).getItem());
            }
            return this;
        }

        @Nullable
        public IRecipeCategory<?> getRecipeCategory() {
            return recipeCategory;
        }

        public List<ItemStack> getCatalystStacks() {
            return catalystStacks;
        }

        @Nullable
        public <T> RecipeType<T> getRecipeType() {
            if (existRecipeType != null) {
                return (RecipeType<T>) existRecipeType;
            }
            return (RecipeType<T>) recipeType;
        }

        public IDrawable getBackground() {
            return background;
        }

        public Component getTitle() {
            return title;
        }

        public IDrawable getIcon() {
            return icon;
        }

        public IGuiHelper getGuiHelper() {
            return guiHelper;
        }

        public <T extends Recipe<RecipeWrapper>> List<T> getClientRecipes() {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                return mc.level.getRecipeManager().getAllRecipesFor((net.minecraft.world.item.crafting.RecipeType<T>) mcRecipeType).stream().map(RecipeHolder::value).toList();
            }
            return new ArrayList<>();
        }

    }

}
