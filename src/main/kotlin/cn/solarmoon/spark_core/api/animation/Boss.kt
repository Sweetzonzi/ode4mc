package cn.solarmoon.spark_core.api.animation

import cn.solarmoon.spark_core.api.animation.anim.AnimController
import cn.solarmoon.spark_core.api.animation.anim.ClientAnimData
import cn.solarmoon.spark_core.api.animation.anim.IAnimatable
import cn.solarmoon.spark_core.api.phys.collision.FreeCollisionBox
import cn.solarmoon.spark_core.api.phys.collision.FreeCollisionBoxRenderManager
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.damagesource.DamageTypes
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import org.joml.Quaternionf
import java.awt.Color

class Boss(type: EntityType<Boss>, level: Level): Mob(type, level), IAnimatable<Boss> {

    override val animatable: Boss = this
    override val animController: AnimController<IAnimatable<Boss>> = AnimController(this)

    init {
        animData = ClientAnimData.create(id, type)
        animData.defaultAnim = animData.animationSet.getAnimation("idle")
    }

    override fun tick() {
        super.tick()
        animController.animTick()


        val level = level()
        if (level is ServerLevel) {
            val box = FreeCollisionBox(getBonePivot("wand2"), Vec3(0.5, 0.5, 2.0)).apply { rotation.setFromUnnormalized(getBoneMatrix("wand2")) }
            // debug
            val boxRenderer = FreeCollisionBoxRenderManager("b1", box, 500)
            boxRenderer.sendRenderableBoxToClient()
            val bBox = boundingBox
            level.getEntities(this, bBox.inflate(10.0)).forEach { entity ->
                if (box.intersects(FreeCollisionBox.of(entity.boundingBox))) {
                    entity.hurt(damageSources().source(DamageTypes.ARROW), 5f)
                    entity.invulnerableTime = 5
                    // debug
                    boxRenderer.setHit(true)
                }
            }
        }

    }

    var index = 0
    override fun interactAt(player: Player, vec: Vec3, hand: InteractionHand): InteractionResult {
        if (level().isClientSide) return InteractionResult.FAIL
        index++
        val size = animData.animationSet.animations.size
        val anim = animData.animationSet.animations[index % size]
        animController.start(anim)
        player.displayClientMessage(Component.literal("已切换动作到${anim.name}"), true)
        return InteractionResult.SUCCESS
    }

}