package test

import cn.solarmoon.spark_core.SparkCore
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.entity.monster.Monster
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent

object ees {

    @JvmStatic
    val BOSS = SparkCore.REGISTER.entity<Boss>()
        .id("runestone_dungeon_keeper")
        .builder(EntityType.Builder.of(::Boss, MobCategory.MONSTER).sized(1f, 2.3f).eyeHeight(1.8f).clientTrackingRange(8))
        .build()

    fun at(event: EntityAttributeCreationEvent) {
        event.put(BOSS.get(), Monster.createMonsterAttributes().build())
    }

    @JvmStatic
    fun register(bus: IEventBus) {
        bus.addListener(::at)
    }

}