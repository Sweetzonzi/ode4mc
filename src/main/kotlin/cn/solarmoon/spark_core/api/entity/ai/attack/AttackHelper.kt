package cn.solarmoon.spark_core.api.entity.ai.attack

import cn.solarmoon.spark_core.SparkCore
import cn.solarmoon.spark_core.api.animation.IEntityAnimatable
import cn.solarmoon.spark_core.api.phys.collision.FreeCollisionBox
import cn.solarmoon.spark_core.api.phys.collision.toOBB
import cn.solarmoon.spark_core.registry.common.SparkAttachments
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import org.joml.Matrix4f
import java.awt.Color
import java.util.UUID

object AttackHelper {

    /**
     * 使用给定的碰撞箱进行攻击，此方法会尽量整合大部分攻击时的信息，包括攻击瞬间的方向/攻击到的生物骨骼等数据，并将攻击数据发送给受到攻击的实体，
     * 此时可通过调用实体的受击信息来完成想要的精细指令
     * @param entity 发动攻击的实体
     * @param damageValue 攻击伤害
     * @param damageSource 攻击源（这里有很大操作空间，有时您并不是想伤害，只是想看攻击有没有碰撞到某个部位，没关系，随便输入一个值，因为这个方法将会发送一个受击数据到被攻击的实体上，你可以在合适的地方处理这些数据）
     * @param box 用于判断该box是否和目标生物的某一部位相交
     * @param boxCache 由于碰撞箱的逻辑是每一tick触发的，如果运动过快碰撞箱可能丢帧，此时强烈建议您传入一个缓存的box，将会在box和boxCache之间自动产生均匀分布的过渡碰撞箱，其中只要任意一个碰撞箱接触到骨骼，则视为接触
     * @param doHurtInstant 是否立刻造成指定的伤害，否则需要自行对那个生物进行处理
     * @param boxTickDensity 如果boxCache不为null时有效，决定了过渡碰撞箱的分布密集度，这个值将会乘以当前密集度产生新的密集度
     * @return 在box范围内的所有实体
     */
    @JvmStatic
    fun boxAttack(
        entity: Entity,
        damageValue: Float,
        damageSource: DamageSource,
        box: FreeCollisionBox,
        boxCache: FreeCollisionBox?,
        doHurtInstant: Boolean,
        boxTickDensity: Double = 1.0
    ): List<Entity> {
        val level = entity.level()
        val list = mutableListOf<Entity>()
        if (level !is ServerLevel) return list
        level.getEntities(entity, entity.boundingBox.inflate(100.0)).forEach { target ->
            var attackFlag = false
            var hitBone: String? = null
            if (target is IEntityAnimatable<*>) {
                getDamageBone(target, box, boxCache, boxTickDensity)?.let { bone ->
                    hitBone = bone
                    attackFlag = true
                }
            } else {
                box.connectionIntersects(target.boundingBox.toOBB(), boxCache, boxTickDensity)?.let {
                    attackFlag = true
                }
            }
            if (attackFlag) {
                list.add(target)
                target.getData(SparkAttachments.ATTACKED_DATA).add(AttackedData(damageValue, damageSource, box, hitBone))
                if (doHurtInstant) target.hurt(damageSource, damageValue)
            }
        }
        return list
    }

    /**
     * 判断传入的box和cacheBox是否击中输入目标的某个骨骼，并返回该骨骼
     */
    @JvmStatic
    fun getDamageBone(target: IEntityAnimatable<*>, box: FreeCollisionBox, cacheBox: FreeCollisionBox?, boxTickDensity: Double = 1.0): String? {
        target.animData.model.bones.forEach { bone ->
            if (bone.name !in target.passableBones) {
                bone.cubes.forEach { cube0 ->
                    val cube = cube0.toOBB(target.getBoneMatrix(bone.name))
                    box.connectionIntersects(cube, cacheBox, boxTickDensity)?.let { hit ->
                        hit.getRenderManager(UUID.randomUUID().toString(), color = Color.RED).sendRenderableBoxToClient()
                        cube.getRenderManager(UUID.randomUUID().toString(), color = Color.GREEN).sendRenderableBoxToClient()
                        return bone.name
                    }
                }
            }
        }
        return null
    }

}