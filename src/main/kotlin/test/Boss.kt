package test

import cn.solarmoon.spark_core.api.animation.anim.AnimController
import cn.solarmoon.spark_core.api.animation.anim.AnimData
import cn.solarmoon.spark_core.api.animation.anim.IAnimatable
import cn.solarmoon.spark_core.api.phys.collision.FreeCollisionBox
import cn.solarmoon.spark_core.api.phys.collision.FreeCollisionBoxRenderManager
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.damagesource.DamageTypes
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.ai.goal.FloatGoal
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
import net.minecraft.world.entity.monster.Monster
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import java.awt.Color

class Boss(type: EntityType<Boss>, level: Level): Monster(type, level), IAnimatable<Boss> {

    override val animatable: Boss = this
    override val animController: AnimController<IAnimatable<Boss>> = AnimController(this)

    init {
        animData = AnimData.create(id, type)
        animData.defaultAnim = animData.animationSet.getAnimation("idle")
    }

    override fun registerGoals() {
        super.registerGoals()
        goalSelector.addGoal(1, FloatGoal(this))
        goalSelector.addGoal(2, MeleeAttackGoal(this, 0.2, false))
        goalSelector.addGoal(3, LookAtPlayerGoal(this, Player::class.java, 8.0F))
        targetSelector.addGoal(1, HurtByTargetGoal(this))
        targetSelector.addGoal(2, NearestAttackableTargetGoal(this, Player::class.java, true))
    }

    override fun tick() {
        super.tick()
        animController.animTick()

        val level = level()
        if (level is ServerLevel) {

            if (deltaMovement.length() > 0) animController.start(animData.animationSet.getAnimation("walk"))

            val box = FreeCollisionBox(getBonePivot("wand2"), Vec3(0.5, 0.5, 2.0)).apply { rotation.setFromUnnormalized(getBoneMatrix("wand2")) }
            // debug
            val boxRenderer = FreeCollisionBoxRenderManager("$id-b1", box, 500, Color.YELLOW)
            boxRenderer.sendRenderableBoxToClient()
            val bBox = boundingBox
            level.getEntities(this, bBox.inflate(10.0)).forEach { entity ->
                if (box.intersects(FreeCollisionBox.of(entity.boundingBox))) {
                    entity.hurt(damageSources().source(DamageTypes.ARROW), 5f)
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