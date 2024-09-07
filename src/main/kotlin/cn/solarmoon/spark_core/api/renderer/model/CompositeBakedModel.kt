package cn.solarmoon.spark_core.api.renderer.model

import net.minecraft.client.renderer.block.model.BakedQuad
import net.minecraft.client.renderer.block.model.ItemOverrides
import net.minecraft.client.renderer.block.model.ItemTransforms
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.resources.model.BakedModel
import net.minecraft.core.Direction
import net.minecraft.util.RandomSource
import net.minecraft.world.level.block.state.BlockState
import java.util.*

/**
 * 物品贴图的叠加模型
 * @param main 上层贴图
 * @param overlay 下层贴图
 */
class CompositeBakedModel(private val main: BakedModel, private val overlay: BakedModel): BakedModel {

    override fun getQuads(state: BlockState?, direction: Direction?, random: RandomSource): MutableList<BakedQuad> {
        val m = ArrayList(overlay.getQuads(state, direction, random)) // 必须新建一个不然会往原有的无限累加
        val o = main.getQuads(state, direction, random)
        m.addAll(o)
        return Collections.unmodifiableList(m)
    }

    override fun useAmbientOcclusion(): Boolean {
        return main.useAmbientOcclusion()
    }

    override fun isGui3d(): Boolean {
        return main.isGui3d
    }

    override fun usesBlockLight(): Boolean {
        return main.usesBlockLight()
    }

    override fun isCustomRenderer(): Boolean {
        return false
    }

    override fun getParticleIcon(): TextureAtlasSprite {
        return main.particleIcon
    }

    override fun getOverrides(): ItemOverrides {
        return ItemOverrides.EMPTY
    }

    override fun getTransforms(): ItemTransforms {
        return main.transforms
    }

}