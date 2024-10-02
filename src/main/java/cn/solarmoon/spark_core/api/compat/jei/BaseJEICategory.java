package cn.solarmoon.spark_core.api.compat.jei;

import cn.solarmoon.spark_core.registry.client.SparkResources;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

/**
 * 完全简化后的jei界面渲染
 * @param <T> 配方类型
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class BaseJEICategory<T> implements IRecipeCategory<T> {//

    protected BaseJEI.JEIBuilder jeiBuilder;
    protected IGuiHelper helper;

    public IDrawableStatic DEFAULT_SLOT;
    public IDrawableStatic DEFAULT_CHANCE_SLOT;
    public IDrawableStatic EMPTY_ARROW;
    public IDrawableStatic ARROW;
    public IDrawableAnimated ANIMATED_ARROW;
    public IDrawableStatic PLUS;
    public IDrawableStatic EXP;
    public IDrawableStatic HAND_POINT;

    public BaseJEICategory(IGuiHelper helper) {
        this.helper = helper;
        this.DEFAULT_SLOT = helper.createDrawable(SparkResources.getJEI_SLOT(), 0, 0, 18, 18);
        this.DEFAULT_CHANCE_SLOT = helper.createDrawable(SparkResources.getJEI_CHANCE_SLOT(), 0, 0, 18, 18);
        this.EMPTY_ARROW = helper.createDrawable(SparkResources.getJEI_EMPTY_ARROW(), 0, 0, 22, 16);
        this.ARROW = helper.createDrawable(SparkResources.getJEI_ARROW(), 0, 0, 22, 16);
        this.ANIMATED_ARROW = helper.createAnimatedDrawable(ARROW, 200, IDrawableAnimated.StartDirection.LEFT, false);
        this.PLUS = helper.createDrawable(SparkResources.getJEI_PLUS(), 0, 0, 13, 13);
        this.EXP = helper.createDrawable(SparkResources.getJEI_EXP(), 0, 0, 8, 8);
        this.HAND_POINT = helper.createDrawable(SparkResources.getJEI_HAND_POINT(), 0, 0, 14, 11);
    }

    public IGuiHelper getGuiHelper() {
        return helper;
    }

    @Override
    public RecipeType<T> getRecipeType() {
        return Objects.requireNonNull(jeiBuilder.getRecipeType());
    }

    @Override
    public Component getTitle() {
        return jeiBuilder.getTitle();
    }

    @Override
    public IDrawable getBackground() {
        return jeiBuilder.getBackground();
    }

    @Override
    public IDrawable getIcon() {
        return jeiBuilder.getIcon();
    }

    public BaseJEICategory<T> setJeiBuilder(BaseJEI.JEIBuilder jeiBuilder) {
        this.jeiBuilder = jeiBuilder;
        return this;
    }

}
