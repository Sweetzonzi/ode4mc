package cn.solarmoon.spark_core.api.compat.jei.element;

import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.client.gui.GuiGraphics;

public class EmptyBackground implements IDrawable {

    private final int width;
    private final int height;

    public EmptyBackground(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void draw(GuiGraphics guiGraphics, int xOffset, int yOffset) {

    }

}
