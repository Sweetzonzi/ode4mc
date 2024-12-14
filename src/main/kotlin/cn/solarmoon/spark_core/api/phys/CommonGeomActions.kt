package cn.solarmoon.spark_core.api.phys

import cn.solarmoon.spark_core.SparkCore
import cn.solarmoon.spark_core.api.entity.attack.AttackedData
import cn.solarmoon.spark_core.api.entity.attack.setAttackedData
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import org.ode4j.ode.DGeom

/**
 * 只对碰撞到的实体设置受击数据
 * @return 碰撞到的实体
 */
fun DGeom.setAttacked(o2: DGeom): Entity? {
    val attacker = body.data().owner
    val target = o2.body.data().owner
    if (target is Entity) {
        SparkCore.LOGGER.info("e")
        target.setAttackedData(AttackedData((attacker as? Entity)?.id, this, body.data().name))
        return target
    }
    return null
}

/**
 * 对碰撞到的实体设置受击数据并进行常规攻击（玩家调用[Player.attack]，生物则调用[LivingEntity.doHurtTarget]）
 * @return 碰撞到的实体
 */
fun DGeom.livingCommonAttack(o2: DGeom): Entity? {
    val attacker = data().owner
    val target = o2.data().owner
    if (attacker is LivingEntity && target is Entity) {
        if (attacker is Player) {
            attacker.attack(target)
        } else {
            attacker.doHurtTarget(target)
        }
    }
    return setAttacked(o2)
}