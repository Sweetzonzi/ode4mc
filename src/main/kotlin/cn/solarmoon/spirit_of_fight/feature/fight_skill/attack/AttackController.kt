package cn.solarmoon.spirit_of_fight.feature.fight_skill.attack

import cn.solarmoon.spark_core.SparkCore
import cn.solarmoon.spark_core.api.animation.IEntityAnimatable
import cn.solarmoon.spark_core.api.entity.state.getInputVector
import cn.solarmoon.spark_core.api.event.KeyboardInputTickEvent
import cn.solarmoon.spark_core.api.util.MoveDirection
import cn.solarmoon.spirit_of_fight.feature.fight_skill.IFightSkillHolder
import cn.solarmoon.spirit_of_fight.feature.fight_skill.skill.ConcentrationAttackAnimSkill
import cn.solarmoon.spirit_of_fight.feature.fight_skill.sync.ClientOperationPayload
import cn.solarmoon.spirit_of_fight.feature.hit.HitType
import cn.solarmoon.spirit_of_fight.registry.client.SOFKeyMappings
import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import net.minecraft.world.phys.HitResult
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.client.event.InputEvent
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent

class AttackController {

    private var isDodgeKeyInstantPress = false

    @SubscribeEvent
    private fun pressOnce(event: InputEvent.Key) {
        if (event.key == SOFKeyMappings.DODGE.key.value && event.action == InputConstants.PRESS) {
            isDodgeKeyInstantPress = true
        }
    }

    @SubscribeEvent
    private fun attack(event: InputEvent.InteractionKeyMappingTriggered) {
        val hit = Minecraft.getInstance().hitResult ?: return
        val player = Minecraft.getInstance().player ?: return
        if (player !is IFightSkillHolder) return
        val skillController = player.skillController ?: return
        if (event.isAttack && hit.type in listOf(HitResult.Type.ENTITY, HitResult.Type.MISS)) {
            var shouldCombo = true
            for ((index, skill) in skillController.specialAttackSkillGroup.withIndex()) {
                if (skill !is ConcentrationAttackAnimSkill && skill.isMetCondition) {
                    skill.start()
                    ClientOperationPayload.sendOperationToServer(index.toString())
                    shouldCombo = false
                    break
                }
            }
            if (shouldCombo) {
                skillController.combo.start(false)
                ClientOperationPayload.sendOperationToServer("combo")
            }
            event.setSwingHand(false)
            event.isCanceled = true
        } else { // 使用技能时阻止和方块挖掘/交互
            if (skillController.getPlayingSkillAnim() != null) event.isCanceled = true
        }
    }

    @SubscribeEvent
    private fun specialAttack(event: InputEvent.Key) {
        val player = Minecraft.getInstance().player ?: return
        if (player !is IFightSkillHolder) return
        val skillController = player.skillController ?: return
        if (event.key == SOFKeyMappings.SPECIAL_ATTACK.key.value && event.action == InputConstants.PRESS) {
            for ((index, skill) in skillController.specialAttackSkillGroup.withIndex()) {
                if (skill is ConcentrationAttackAnimSkill && skill.isMetCondition) {
                    skill.start()
                    ClientOperationPayload.sendOperationToServer(index.toString())
                    break
                }
            }
        }
    }

    @SubscribeEvent
    private fun dodge(event: KeyboardInputTickEvent.Post) {
        val player = Minecraft.getInstance().player ?: return
        val input = player.input
        val direction = MoveDirection.getByInput(input)
        if (direction != null && player is IFightSkillHolder && player.onGround()) {
            if (SOFKeyMappings.DODGE.key.value == event.options.keyShift.key.value) {
                input.shiftKeyDown = false
                if (player.isCrouching) return
            } // 如果和蹲键重合，则会取消蹲姿
            if (!isDodgeKeyInstantPress) return
            val skill = player.skillController ?: return
            val v = (player as LocalPlayer).getInputVector()
            skill.dodge.start(direction, v)
            ClientOperationPayload.sendOperationToServer("dodge", v, direction.id)
            event.isCanceled = true
        }
        isDodgeKeyInstantPress = false
    }

    @SubscribeEvent
    private fun guard(event: KeyboardInputTickEvent.Post) {
        val player = Minecraft.getInstance().player ?: return
        if (player is IFightSkillHolder) {
            val skill = player.skillController ?: return
            if (SOFKeyMappings.GUARD.isDown) {
                if (skill.preInput.id != "guard" && !skill.guard.isPlaying()) {
                    skill.guard.start {
                        // 防守检测上不是很敏感，因此需要确保客户端已经开始防守了以后再给服务端同步指令
                        ClientOperationPayload.sendOperationToServer("guard")
                    }
                }
            } else {
                if (skill.preInput.id == "guard") {
                    skill.preInput.clear()
                    ClientOperationPayload.sendOperationToServer("guard_clear")
                } // 这里多加一条清除确保客户端一定取消操作
                if (skill.guard.isPlaying { !it.isInTransition }) {
                    skill.guard.stop {
                        ClientOperationPayload.sendOperationToServer("guard_stop")
                    }
                }
            }
        }
    }

    @SubscribeEvent
    private fun stop(event: MovementInputUpdateEvent) {
        val player = event.entity as LocalPlayer
        val input = player.input
        if (player is IFightSkillHolder) {
            val skill = player.skillController
            if (skill != null && skill.guard.isStanding { !it.isInTransition }) {
                input.forwardImpulse /= 4f
                input.leftImpulse /= 4f
                input.jumping = false
                input.shiftKeyDown = false
                player.sprintTriggerTime = -1
                player.setSprinting(false)
                player.swinging = false
            } else if ((skill != null && skill.isPlayingSkill { !it.isCancelled }) || HitType.isPlayingHitAnim(player as IEntityAnimatable<*>) { !it.isCancelled }) {
                input.forwardImpulse = 0f
                input.leftImpulse = 0f
                input.up = false
                input.down = false
                input.left = false
                input.right = false
                input.jumping = false
                input.shiftKeyDown = false
                player.sprintTriggerTime = -1
                player.setSprinting(false)
                player.swinging = false
            }
        }
    }

}