package cn.solarmoon.spark_core.api.event

import net.minecraft.client.Options
import net.neoforged.bus.api.Event
import net.neoforged.bus.api.ICancellableEvent

abstract class KeyboardInputTickEvent(
    val options: Options,
    val isSneaking: Boolean,
    val sneakingSpeedMultiplier: Float
): Event(), ICancellableEvent {

    class Pre(
        options: Options,
        isSneaking: Boolean,
        sneakingSpeedMultiplier: Float
    ): KeyboardInputTickEvent(options, isSneaking, sneakingSpeedMultiplier)

    class Post(
        options: Options,
        isSneaking: Boolean,
        sneakingSpeedMultiplier: Float
    ): KeyboardInputTickEvent(options, isSneaking, sneakingSpeedMultiplier)

}