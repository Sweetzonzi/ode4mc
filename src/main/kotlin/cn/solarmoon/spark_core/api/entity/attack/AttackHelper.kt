package cn.solarmoon.spark_core.api.entity.attack

import cn.solarmoon.spark_core.api.animation.IEntityAnimatable
import cn.solarmoon.spark_core.api.util.Side
import cn.solarmoon.spark_core.api.phys.obb.MountableOBB
import cn.solarmoon.spark_core.api.phys.obb.OrientedBoundingBox
import cn.solarmoon.spark_core.api.phys.obb.pushBox
import cn.solarmoon.spark_core.api.phys.obb.toOBB
import cn.solarmoon.spark_core.registry.common.SparkVisualEffects
import cn.solarmoon.spark_core.registry.common.SparkAttachments
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import thedarkcolour.kotlinforforge.neoforge.forge.vectorutil.v3d.toVec3
import java.awt.Color
import java.util.Optional
import kotlin.math.PI

object AttackHelper {

    /**
     * 判断传入的box和cacheBox是否击中输入目标的某个骨骼，并返回该骨骼和击中该骨骼的box信息
     *
     * 如果生物存在[MountableOBB]信息，则会尝试击中它所绑定的box
     */
    @JvmStatic
    fun getDamageBone(target: IEntityAnimatable<*>, box: OrientedBoundingBox, cacheBox: OrientedBoundingBox?, boxTickDensity: Double = 1.0): Pair<OrientedBoundingBox, String>? {
        target.animatable.getData(SparkAttachments.MOUNTABLE_OBB).entries.forEach { (name, mOBB) ->
            if (mOBB.type == MountableOBB.Type.STRIKABLE_BONE) {
                box.connectionIntersects(mOBB.box, cacheBox, boxTickDensity)?.let {
                    SparkVisualEffects.OBB.syncBoxToClient(name, Color.RED, mOBB.box)
                    return Pair(it, name)
                }
            }
        }
        target.animData.model.bones.forEach { bone ->
            if (bone.name !in target.passableBones) {
                bone.cubes.forEach { cube0 ->
                    val cube = cube0.toOBB(target.getBoneMatrix(bone.name))
                    box.connectionIntersects(cube, cacheBox, boxTickDensity)?.let { hit ->
                        SparkVisualEffects.OBB.syncBoxToClient("${target.animatable.id}:${bone.name}", Color.RED, cube)
                        return Pair(hit, bone.name)
                    }
                }
            }
        }
        return null
    }

    /**
     * 获取输入box相对于目标实体朝向的左右方向
     */
    @JvmStatic
    fun getBoxSide(target: Entity, box: OrientedBoundingBox): Side {
        // 水平朝向向量的垂直向量
        val viewVector = Vec3.directionFromRotation(0f, target.yRot).toVector3f().rotateY(PI.toFloat() / 2).toVec3()
        val boxCenter = box.center.toVec3()
        // 计算目标到 box 的向量
        val targetPosition = target.position()
        val boxToTarget = boxCenter.subtract(targetPosition).normalize()
        // 点乘计算夹角的余弦值
        val dotProduct = viewVector.dot(boxToTarget)
        return if (dotProduct >= 0) Side.LEFT else Side.RIGHT
    }

}

/**
 * 使用给定的碰撞箱进行攻击，此方法会尽量整合大部分攻击时的信息，包括攻击瞬间的方向/攻击到的生物骨骼等数据，并将攻击数据发送给受到攻击的实体，
 * 此时可通过调用实体的受击信息来完成想要的精细指令。（说是攻击实际上是碰撞检测）
 *
 * 但需要注意的是，本方法并不直接对实体造成伤害，而是检测输入的碰撞箱碰撞到的实体并给上碰撞的实体一个受击信息[AttackedData]。但是可以通过返回的击中生物表来进行具体操作。
 *
 * 以及此方法如果击中对象是实现了动画接口的对象，那么就不会通过原版的AABB进行受击与否的判断，而是根据该实体精准的每个骨骼位置进行检测，并会在受击数据里设定这些生物首先被击中的骨骼名。
 * 当然，如果检测对象没有实现本模组的动画接口，则还是按AABB进行检测
 * @param box 用于判断该box是否和目标生物的某一部位相交
 * @param boxId 其不为null时有两个用途：
 * - 通过给定的id生成该box在客户端的debug渲染（在击中到生物骨骼时会标红）
 * - 通过给定的id获取该box在该生物身上的缓存，以生成连贯的box碰撞（见[OrientedBoundingBox.connectionIntersects]）
 * @param setAttacked 每个生物在被加入到返回的列表前可以用此方法进行过滤，并且最重要的是，此方法返回false将不会给予该生物[AttackedData]
 * @return 被输入box接触到的所有实体
 */
fun Entity.boxAttack(
    box: OrientedBoundingBox,
    boxId: String?,
    setAttacked: (Entity) -> Boolean = { true },
): List<Entity> {
    val level = level()
    val list = mutableListOf<Entity>()
    var hitBone: String? = null
    var hitBox = box
    val boxCache = if (boxId != null) pushBox(boxId) else null
    if (level is ServerLevel) {
        level.getEntities(this, boundingBox.inflate(100.0)).forEach { target ->
            var attackFlag = false
            if (target is IEntityAnimatable<*>) {
                AttackHelper.getDamageBone(target, box, boxCache)?.let { (hitBox2, bone) ->
                    hitBox = hitBox2
                    hitBone = bone
                    attackFlag = true
                    SparkVisualEffects.OBB.syncBoxToClient(boxId, Color.RED, null)
                }
            } else {
                box.connectionIntersects(target.boundingBox.toOBB(), boxCache)?.let { hitBox2 ->
                    hitBox = hitBox2
                    attackFlag = true
                    SparkVisualEffects.OBB.syncBoxToClient(boxId, Color.RED, null)
                }
            }
            if (attackFlag) {
                if (setAttacked.invoke(target) && target.id != vehicle?.id) {
                    target.setAttackedData(AttackedData(id, hitBox, hitBone))
                    list.add(target)
                }
            }
        }
    }
    boxId?.let { SparkVisualEffects.OBB.getRenderableBox(it) }?.refresh(box)
    return list
}

fun Entity.setAttackedData(data: AttackedData) {
    setData(SparkAttachments.ATTACKED_DATA, Optional.of(data))
}

fun Entity.getAttackedData(): AttackedData? {
    return getData(SparkAttachments.ATTACKED_DATA).orElse(null)
}

fun Entity.clearAttackedData() {
    setData(SparkAttachments.ATTACKED_DATA, Optional.empty())
}