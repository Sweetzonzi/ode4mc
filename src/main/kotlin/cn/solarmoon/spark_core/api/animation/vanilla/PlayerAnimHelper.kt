package cn.solarmoon.spark_core.api.animation.vanilla

import cn.solarmoon.spark_core.api.animation.IEntityAnimatable
import cn.solarmoon.spark_core.api.animation.anim.template.EntityStateAnim
import net.minecraft.client.Minecraft
import net.minecraft.client.player.AbstractClientPlayer
import net.minecraft.world.entity.player.Player
import net.neoforged.api.distmarker.Dist
import net.neoforged.api.distmarker.OnlyIn

object PlayerAnimHelper {

    @Suppress("unchecked_cast")
    @JvmStatic
    fun getAnimatable(player: Player): IEntityAnimatable<Player> {
        return (player as IEntityAnimatable<Player>)
    }

    /**
     * 用于判断是否应当在第一人称下播放动画时渲染手部（目前只有物品）的动作
     */
    @OnlyIn(Dist.CLIENT)
    @JvmStatic
    fun shouldRenderArmAnimInFirstPerson(player: AbstractClientPlayer): Boolean {
        val isInFirstPerson = Minecraft.getInstance().options.cameraType.isFirstPerson
        val isMainCamera = Minecraft.getInstance().cameraEntity == player
        val isPlayingAnimWithAnyArm = player is IEntityAnimatable<*> && player.animData.playData.mixedAnims.any { !EntityStateAnim.entries.map { it.animName }.contains(it.name) }
        return isInFirstPerson && isMainCamera && isPlayingAnimWithAnyArm
    }

}