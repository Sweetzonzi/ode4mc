package cn.solarmoon.spark_core.api.phys.thread

import net.neoforged.bus.api.Event

abstract class PhysLevelTickEvent: Event() {

    class Entity(val entity: net.minecraft.world.entity.Entity): PhysLevelTickEvent() {

    }

}