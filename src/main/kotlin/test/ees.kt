package test

import cn.solarmoon.spark_core.SparkCore
import cn.solarmoon.spark_core.api.animation.Boss
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.MobCategory
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent

object ees {

    @JvmStatic
    val BOSS = SparkCore.REGISTER.entity<Boss>()
        .id("runestone_dungeon_keeper")
        .builder(EntityType.Builder.of(::Boss, MobCategory.MISC).sized(1f, 2f).eyeHeight(1.8f).clientTrackingRange(8))
        .build()

    fun at(event: EntityAttributeCreationEvent) {
        event.put(BOSS.get(), Mob.createMobAttributes().build())
    }

    @JvmStatic
    fun register(bus: IEventBus) {
        bus.addListener(::at)
    }

}