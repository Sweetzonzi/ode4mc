package cn.solarmoon.spark_core.api.entity.skill

import cn.solarmoon.spark_core.SparkCore
import cn.solarmoon.spark_core.api.animation.IEntityAnimatable
import cn.solarmoon.spark_core.api.animation.anim.play.MixedAnimation
import cn.solarmoon.spark_core.api.phys.IBoundingBone
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.Vec3
import org.ode4j.ode.DGeom

/**
 * @param animBounds 该技能绑定的动画组
 */
abstract class AnimSkill(
    val animatable: IEntityAnimatable<*>,
    val animBounds: Set<String>
) {

    val entity = animatable.animatable
    val boundingBones = mutableListOf<IBoundingBone>()

    /**
     * 根据该生物的攻速以及输入的基础攻速进行差值，以此获取一个基于攻速的动画速度
     */
    fun getAttackAnimSpeed(baseSpeedValue: Float): Float {
        val entity = entity
        if (entity is LivingEntity) {
            val sp = entity.getAttribute(Attributes.ATTACK_SPEED) ?: return 1f
            return ((sp.value.toFloat() - baseSpeedValue) / 2 + 1f).coerceAtLeast(0.05f)
        } else return 1f
    }

    /**
     * 获取以生物水平朝向为内容的移动值
     * @param mul 对水平速度进行乘积以调整大小
     */
    fun getForwardMoveVector(mul: Float): Vec3 {
        val forward = entity.forward
        return Vec3(forward.x * mul, entity.deltaMovement.y, forward.z * mul)
    }

    /**
     * 是否正在播放组内的任意一个动画
     */
    fun isPlaying(filter: (MixedAnimation) -> Boolean = { true }): Boolean {
        return animBounds.any { animatable.animController.isPlaying(it, filter) }
    }

    /**
     * 获取第一个正在播放的动画（间接保证每个技能只能同时播放一个组内动画）
     */
    fun getPlayingAnim(filter: (MixedAnimation) -> Boolean = {true}): MixedAnimation? {
        for (anim in animBounds) {
            val a = animatable.animData.playData.getMixedAnimation(anim, filter)
            if (a != null) return animatable.animController.animsCache.firstOrNull { it.name == a.name }
        }
        return null
    }

    open fun tick() {
        getPlayingAnim()?.let {
            whenInAnim(it)
        } ?: run {
            whenNotInAnim()
        }

        boundingBones.forEach {
            it.tick()
            if (it.body.isEnabled) onGeomEnabled(it)
            else onGeomDisabled(it)
        }
    }

    open fun shouldEnableGeom(geom: DGeom, anim: MixedAnimation): Boolean = false

    open fun onGeomEnabled(bone: IBoundingBone) {}

    open fun onGeomDisabled(bone: IBoundingBone) {}

    /**
     * 当正在播放当前技能绑定的动画时的自定义内容
     */
    open fun whenInAnim(anim: MixedAnimation) {
        boundingBones.forEach { bone ->
            bone.tick()
            bone.boundingGeoms.forEach { geom ->
                if (shouldEnableGeom(geom, anim)) {
                    bone.body.enable()
                } else {
                    bone.body.disable()
                }
            }
        }
        move(anim)
    }

    open fun whenNotInAnim() {
        boundingBones.forEach { it.body.disable() }
    }

    /**
     * 当在动画播放中受到攻击时会调用此方法
     * @return false将免疫此次攻击，true将正常执行[net.minecraft.world.entity.Entity.hurt]操作
     */
    open fun whenAttackedInAnim(damageSource: DamageSource, value: Float, anim: MixedAnimation): Boolean = true

    /**
     * [getBox]方法中每个box的唯一标识，默认用于生成对应的debug渲染箱，但也可用于外置骨骼等需要box唯一标识的方法
     */
    open fun getBoxId(index: Int = 0): String {
        return "${entity.id}:common_box_$index"
    }

    /**
     * 正在播放绑定动画时进行移动
     */
    open fun move(anim: MixedAnimation) {
        getMove(anim)?.let {
            entity.deltaMovement = it
        }
    }

    /**
     * 获取各个条件下的位移，会在不返回null时进行给定的位移
     */
    open fun getMove(anim: MixedAnimation): Vec3? = null

    /**
     * 伤害乘数，此乘数将应用在伤害最终结算时
     */
    open fun getAttackDamageMultiplier(anim: MixedAnimation): Float? = null

    /**
     * 获取该次攻击所使用的物品，这个方法的用意在于有时一些武器是左手或双持类型，每个动作的攻击武器未必一致，故有此方法为每个动作单独指定
     * @param originWeapon 此方法修改的是[net.minecraft.world.entity.Entity.getWeaponItem]，因此origin代表了该方法所返回的默认武器
     */
    open fun getAttackItem(originWeapon: ItemStack?, anim: MixedAnimation): ItemStack? = originWeapon

}