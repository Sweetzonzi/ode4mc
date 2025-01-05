package cn.solarmoon.spirit_of_fight.fighter

import cn.solarmoon.spark_core.phys.attached_body.AttachedBody
import net.minecraft.world.entity.Entity

class EntityPatch(
    val entity: Entity
) {

    var weaponAttackBody: AttachedBody? = null
    var weaponGuardBody: AttachedBody? = null

}