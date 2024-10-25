package test

import cn.solarmoon.spark_core.SparkCore
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent
import test.runestone_dungeon_keeper.Tiasis

object EES {

    @JvmStatic
    val BOSS = SparkCore.REGISTER.entity<Tiasis>()
        .id("wukong")
        .builder(EntityType.Builder.of(::Tiasis, MobCategory.MONSTER).sized(0.5f, 1.8f).eyeHeight(1.6f).clientTrackingRange(8))
        .build()

    @JvmStatic
    val BOSS2 = SparkCore.REGISTER.entity<Tiasis>()
        .id("tiasis")
        .builder(EntityType.Builder.of(::Tiasis, MobCategory.MONSTER).sized(2.99f, 5.99f).eyeHeight(6f).clientTrackingRange(10))
        .build()

    @JvmStatic
    val BOSS3 = SparkCore.REGISTER.entity<Tiasis>()
        .id("runestone_dungeon_keeper")
        .builder(EntityType.Builder.of(::Tiasis, MobCategory.MONSTER).sized(0.8f, 2.6f).eyeHeight(2.4f).clientTrackingRange(8))
        .build()

    @JvmStatic
    val BOSS4 = SparkCore.REGISTER.entity<Tiasis>()
        .id("quan")
        .builder(EntityType.Builder.of(::Tiasis, MobCategory.MONSTER).sized(0.8f, 2.6f).eyeHeight(2.4f).clientTrackingRange(8))
        .build()

    @JvmStatic
    val BOSS5 = SparkCore.REGISTER.entity<Tiasis>()
        .id("wendigo")
        .builder(EntityType.Builder.of(::Tiasis, MobCategory.MONSTER).sized(0.8f, 2.6f).eyeHeight(2.4f).clientTrackingRange(8))
        .build()

    @JvmStatic
    val BOSS6 = SparkCore.REGISTER.entity<Tiasis>()
        .id("pirate")
        .builder(EntityType.Builder.of(::Tiasis, MobCategory.MONSTER).sized(0.8f, 2.6f).eyeHeight(2.4f).clientTrackingRange(8))
        .build()

    @JvmStatic
    val BOSS7 = SparkCore.REGISTER.entity<Tiasis>()
        .id("tuna")
        .builder(EntityType.Builder.of(::Tiasis, MobCategory.MONSTER).sized(0.8f, 2.6f).eyeHeight(2.4f).clientTrackingRange(8))
        .build()

    @JvmStatic
    val BOSS8 = SparkCore.REGISTER.entity<Tiasis>()
        .id("crab")
        .builder(EntityType.Builder.of(::Tiasis, MobCategory.MONSTER).sized(0.8f, 2.6f).eyeHeight(2.4f).clientTrackingRange(8))
        .build()

    fun at(event: EntityAttributeCreationEvent) {
        event.put(BOSS.get(), Tiasis.createAttributes().build())
        event.put(BOSS2.get(), Tiasis.createAttributes().build())
        event.put(BOSS3.get(), Tiasis.createAttributes().build())
        event.put(BOSS4.get(), Tiasis.createAttributes().build())
        event.put(BOSS5.get(), Tiasis.createAttributes().build())
        event.put(BOSS6.get(), Tiasis.createAttributes().build())
        event.put(BOSS7.get(), Tiasis.createAttributes().build())
        event.put(BOSS8.get(), Tiasis.createAttributes().build())
    }

    @JvmStatic
    fun register(bus: IEventBus) {
        bus.addListener(::at)
    }

}