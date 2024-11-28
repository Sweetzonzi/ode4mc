package cn.solarmoon.spark_core.api.animation.anim.template

import cn.solarmoon.spark_core.api.animation.IEntityAnimatable
import cn.solarmoon.spark_core.api.animation.anim.play.MixedAnimation
import cn.solarmoon.spark_core.api.entity.state.isJumping
import cn.solarmoon.spark_core.api.entity.state.isMoving
import cn.solarmoon.spark_core.api.entity.state.isMovingBack
import cn.solarmoon.spark_core.api.entity.state.moveBackCheck
import cn.solarmoon.spark_core.api.entity.state.moveCheck
import net.minecraft.client.player.LocalPlayer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.LivingEntity

/**
 * @param animName 每种状态所使用的固定动画名
 * @param singlePlay 是否当该动画条件满足时立刻播放该动画，为false时如果该动画正在播放则不会重复播放该动画。这一项适用于类似跳跃之类的每次都需要重新播放的动画
 * @param priority 权重，决定先后播放优先级
 */
enum class EntityStateAnim(val animName: String, val singlePlay: Boolean, val priority: Int) {
    IDLE("idle", false, 4),
    WALK("walk", false, 3),
    WALK_BACK("walk_back", false, 2),
    SPRINTING("sprinting", false, 1),
    JUMP("jump", true, 0);

    fun getCondition(animatable: IEntityAnimatable<*>): Boolean {
        val entity = animatable.animatable
        return when(this) {
            IDLE -> true
            WALK -> entity.isMoving() || (entity.level().isClientSide && entity is LocalPlayer && entity.moveCheck())
            WALK_BACK -> entity.isMovingBack() || (entity.level().isClientSide && entity is LocalPlayer && entity.moveBackCheck())
            SPRINTING -> entity.isSprinting && !entity.isCrouching
            JUMP -> entity.isJumping()
        }
    }

    fun getAnimation(animatable: IEntityAnimatable<*>): MixedAnimation {
        val entity = animatable.animatable
        val modelPath = animatable.animData.modelPath
        val speed = if (entity is LivingEntity && entity.speed > 0.1025) entity.speed else 0f
        fun speedModifier(multiply: Float) = 1f + multiply * speed
        return when(this) {
            IDLE -> MixedAnimation(modelPath, animName, 1)
            WALK -> MixedAnimation(modelPath, animName, 1, speedModifier(5f))
            WALK_BACK -> MixedAnimation(modelPath, animName, 1, speedModifier(2f))
            SPRINTING -> MixedAnimation(modelPath, animName, 1, speedModifier(1f))
            JUMP -> MixedAnimation(modelPath, animName, 1, 1f, 5f)
        }
    }

    fun isPlaying(animatable: IEntityAnimatable<*>, level: Int = 0): Boolean {
        return animatable.animController.isPlaying(animName, level)
    }

    fun tryPlay(animatable: IEntityAnimatable<*>, stopFilter: (MixedAnimation) -> Boolean = {true}, animModifier: (MixedAnimation) -> Unit = {}): Boolean {
        return tryPlay(animatable, 0, stopFilter, animModifier)
    }

    fun tryPlay(animatable: IEntityAnimatable<*>, level: Int, stopFilter: (MixedAnimation) -> Boolean = {true}, animModifier: (MixedAnimation) -> Unit = {}): Boolean {
        if (getCondition(animatable)) {
            if (!isPlaying(animatable, level) || singlePlay) {
                val anim = getAnimation(animatable).apply { animModifier.invoke(this); this.level = level }
                animatable.animController.stopAndAddAnimation(anim) { stopFilter.invoke(it) }
                val player = animatable.animatable
                if (player is ServerPlayer && level != 0) animatable.syncAnimDataToClient(player)
            }
            return true
        }
        return false
    }

    companion object {
        @JvmStatic
        val ALL_STATES = enumValues<EntityStateAnim>().sortedBy { it.priority }

        @JvmStatic
        fun shouldPlayStateAnim(animatable: IEntityAnimatable<*>): Boolean {
            val playData = animatable.animData.playData
            val mixAnimations = playData.mixedAnims
            val statusAnims = animatable.statusAnims.map { it.animName }
            val allExist = ALL_STATES.all { animatable.animData.animationSet.hasAnimation(it.animName) }
            val isPlayingOtherAnim = !mixAnimations.isEmpty() && mixAnimations.any { it.name !in statusAnims && !it.isCancelled }
            val jump = playData.getMixedAnimation(JUMP.animName)
            return allExist && !isPlayingOtherAnim && (jump == null || jump.isTickIn(0.4, jump.maxTick))
        }
    }

}