package cn.solarmoon.spirit_of_fight.feature.fight_skill.spirit

import cn.solarmoon.spark_core.SparkCore
import cn.solarmoon.spark_core.api.util.ColorUtil
import cn.solarmoon.spark_core.api.util.RenderTypeUtil
import cn.solarmoon.spark_core.api.util.blitTransparent
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.BufferBuilder
import com.mojang.blaze3d.vertex.BufferUploader
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.LayeredDraw
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Items
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent
import net.neoforged.neoforge.client.extensions.IGuiGraphicsExtension
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions
import org.joml.Vector3f
import java.awt.Color

class FightSpiritGui: LayeredDraw.Layer {
    
    companion object {
        val EMPTY = ResourceLocation.fromNamespaceAndPath(SparkCore.MOD_ID, "textures/gui/fight_spirit_empty.png")
        val FULL = ResourceLocation.fromNamespaceAndPath(SparkCore.MOD_ID, "textures/gui/fight_spirit_full.png")
    }

    override fun render(
        guiGraphics: GuiGraphics,
        deltaTracker: DeltaTracker
    ) {
        val partialTicks = deltaTracker.getGameTimeDeltaPartialTick(true)
        val player = Minecraft.getInstance().player ?: return
        val fs = player.getFightSpirit()

        val x = guiGraphics.guiWidth() / 2 - 8f
        val y = guiGraphics.guiHeight() - 48f
        val progress = fs.getProgress(partialTicks)
        val turn = player.tickCount % 20 / 4 // 从0-4反复循环的数
        guiGraphics.blitTransparent(EMPTY, x, y, 1f, 1f, 16f, 10f, Color.WHITE.rgb)
        guiGraphics.blitTransparent(FULL, x, y + 10f, 0f, 1f, 0f + turn/5f, -progress * 1f/5 + turn/5f, 16f, -progress * 10f, Color.WHITE.rgb)
    }

}