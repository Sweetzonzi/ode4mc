package cn.solarmoon.spark_core.api.animation.vanilla

import cn.solarmoon.spark_core.api.animation.IEntityAnimatable
import net.minecraft.client.Minecraft
import net.minecraft.client.player.AbstractClientPlayer
import net.minecraft.world.entity.HumanoidArm
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
     * 用于判断是否应当在第一人称下播放动画时渲染手部的动作
     */
    @OnlyIn(Dist.CLIENT)
    @JvmStatic
    fun shouldRenderArmAnimInFirstPerson(player: AbstractClientPlayer): Boolean {
        val isInFirstPerson = Minecraft.getInstance().options.cameraType.isFirstPerson
        val isMainCamera = Minecraft.getInstance().cameraEntity?.id == player.id
        val isPlayingAnimWithAnyArm = isPlayingAnimOnArm(player, HumanoidArm.RIGHT) || isPlayingAnimOnArm(player, HumanoidArm.LEFT)
        return isInFirstPerson && isMainCamera && isPlayingAnimWithAnyArm
    }

    /**
     * 是否在指定手部有动画正在播放
     */
    @JvmStatic
    fun isPlayingAnimOnArm(player: Player, arm: HumanoidArm): Boolean {
        return getAnimatable(player).animData.playData.mixedAnimations.any { it.animation.getBoneAnim(if (arm == HumanoidArm.LEFT) "leftArm" else "rightArm") != null }
    }

}