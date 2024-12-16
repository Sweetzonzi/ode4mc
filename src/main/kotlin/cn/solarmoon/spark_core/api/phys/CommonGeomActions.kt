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
    val attacker = data().owner
    val target = o2.data().owner
    if (target is Entity) {
        if (data().attackedEntities.contains(target.id)) return null
        data().attackedEntities.add(target.id)
        target.setAttackedData(AttackedData((attacker as? Entity)?.id, this, body.data().name))
        return target
    }
    return null
}

/**
 * 对碰撞到的实体设置受击数据并进行常规攻击（玩家调用[Player.attack]，生物则调用[LivingEntity.doHurtTarget]）
 * @param immediateDamage 是否无视伤害的无敌时间立刻造成伤害
 * @return 碰撞到的实体
 */
fun DGeom.livingCommonAttack(o2: DGeom, immediateDamage: Boolean): Entity? {
    val target = setAttacked(o2)
    if (target != null) {
        val attacker = data().owner
        if (attacker is Player) {
            attacker.attack(target)
        } else if (attacker is LivingEntity) {
            attacker.doHurtTarget(target)
        }
        if (immediateDamage) target.invulnerableTime = 0
    }
    return target
}