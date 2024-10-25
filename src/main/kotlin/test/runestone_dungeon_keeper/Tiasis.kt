package test.runestone_dungeon_keeper

import cn.solarmoon.spark_core.api.animation.anim.play.AnimController
import cn.solarmoon.spark_core.api.animation.IAnimatable
import cn.solarmoon.spark_core.api.animation.IEntityAnimatable
import cn.solarmoon.spark_core.api.animation.anim.play.MixedAnimation
import cn.solarmoon.spark_core.api.entity.ai.attack.AttackHelper
import cn.solarmoon.spark_core.api.phys.collision.FreeCollisionBox
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
import net.minecraft.world.entity.monster.Monster
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import cn.solarmoon.spark_core.api.entity.ai.goal.DirectAttackGoal
import cn.solarmoon.spark_core.api.entity.ai.pathfinding.NaturalNavigateGround
import net.minecraft.nbt.NbtOps
import net.minecraft.world.entity.ai.navigation.PathNavigation
import org.joml.Vector3f
import test.EES
import java.awt.Color

class Tiasis(type: EntityType<Tiasis>, level: Level): PathfinderMob(type, level), IEntityAnimatable<Tiasis> {

    companion object {
        @JvmStatic
        fun createAttributes(): AttributeSupplier.Builder {
            return Monster.createMonsterAttributes().add(Attributes.MOVEMENT_SPEED, 0.25)
        }
    }

    override val animatable: Tiasis = this
    override val animController: AnimController<IAnimatable<Tiasis>> = AnimController(this)

    override val turnBodyAnims: List<String> get() = animData.animationSet.animations.map { it.name }

    override val passableBones: List<String> = listOf("bone20")

    override fun registerGoals() {
        super.registerGoals()
        goalSelector.addGoal(1, DirectAttackGoal(this, 1.0, 5.0))
        goalSelector.addGoal(2, LookAtPlayerGoal(this, Player::class.java, 8.0F))
        targetSelector.addGoal(1, HurtByTargetGoal(this))
        targetSelector.addGoal(2, NearestAttackableTargetGoal(this, Player::class.java, true))
    }

    override fun createNavigation(level: Level): PathNavigation {
        return NaturalNavigateGround(this, level)
    }

    var boxCache: FreeCollisionBox? = null
    var boxCache2: FreeCollisionBox? = null
    var boxCache3: FreeCollisionBox? = null
    var boxCache4: FreeCollisionBox? = null

    // 在 tick 方法中更新 cache
    override fun tick() {
        super.tick()

        if (animData.modelPath == EES.BOSS2.id && !level().isClientSide) {
            val box = createCollisionBoxBoundToBone("bone20", Vector3f(1f, 1f, 6f), Vector3f(0f, 0f, -3f))
            var cache = box.copy()
            FreeCollisionBox.CODEC.decode(level().registryAccess().createSerializationContext(NbtOps.INSTANCE), persistentData.get("box")).ifSuccess {
                cache = it.first
            }
            cache.getRenderManager("c", 60, Color.GREEN).sendRenderableBoxToClient()
            AttackHelper.boxAttack(this, 1f, damageSources().mobAttack(this), box, cache, true)
            box.getRenderManager("233${id}", color = Color.YELLOW).sendRenderableBoxToClient()
            persistentData.put("box", FreeCollisionBox.CODEC.encodeStart(NbtOps.INSTANCE, box).orThrow)
        }

    }

    var index = 0
    override fun interactAt(player: Player, vec: Vec3, hand: InteractionHand): InteractionResult {
        index++
        val size = animData.animationSet.animations.size
        val anim = animData.animationSet.animations[index % size]
        animController.stopAndAddAnimation(MixedAnimation(animData.modelPath, anim.name, 1))
        syncAnimDataToClient()
        player.displayClientMessage(Component.literal("已切换动作到${anim.name}"), true)
        return InteractionResult.SUCCESS
    }

}