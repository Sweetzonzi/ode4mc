package cn.solarmoon.spirit_of_fight.feature.fight_skill.attack

import cn.solarmoon.spark_core.api.animation.IEntityAnimatable
import cn.solarmoon.spark_core.api.animation.anim.auto_anim.isPlayingHitAnim
import cn.solarmoon.spark_core.api.entity.preinput.getPreInput
import cn.solarmoon.spark_core.api.entity.state.getInputVector
import cn.solarmoon.spark_core.api.event.KeyboardInputTickEvent
import cn.solarmoon.spark_core.api.event.OnPreInputExecuteEvent
import cn.solarmoon.spark_core.api.util.MoveDirection
import cn.solarmoon.spirit_of_fight.feature.fight_skill.IFightSkillHolder
import cn.solarmoon.spirit_of_fight.feature.fight_skill.controller.CommonFightSkillController
import cn.solarmoon.spirit_of_fight.feature.fight_skill.skill.ConcentrationAttackAnimSkill
import cn.solarmoon.spirit_of_fight.feature.fight_skill.skill.ParryAnimSkill
import cn.solarmoon.spirit_of_fight.feature.fight_skill.sync.ClientOperationPayload
import cn.solarmoon.spirit_of_fight.registry.client.SOFKeyMappings
import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
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
                if (skill !is ConcentrationAttackAnimSkill && skill.canRelease) {
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
        }
    }

    @SubscribeEvent
    private fun specialAttack(event: KeyboardInputTickEvent.Post) {
        val player = Minecraft.getInstance().player ?: return
        if (player !is IFightSkillHolder) return
        val skillController = player.skillController ?: return
        while (SOFKeyMappings.SPECIAL_ATTACK.consumeClick()) {
            for ((index, skill) in skillController.specialAttackSkillGroup.withIndex()) {
                if (skill is ConcentrationAttackAnimSkill && skill.canRelease) {
                    skill.start()
                    ClientOperationPayload.sendOperationToServer(index.toString())
                    break
                }
            }
        }
    }

    @SubscribeEvent
    private fun dodge(event: KeyboardInputTickEvent.Post) {
        run {
            val player = Minecraft.getInstance().player ?: return@run
            val input = player.input
            val direction = MoveDirection.getByInput(input)
            if (direction != null && player is IFightSkillHolder && player.onGround()) {
                if (SOFKeyMappings.DODGE.key.value == event.options.keyShift.key.value) {
                    input.shiftKeyDown = false
                    if (player.isCrouching) return@run
                } // 如果和蹲键重合，则会取消蹲姿
                if (!isDodgeKeyInstantPress) return@run
                val skill = player.skillController ?: return@run
                val v = (player as LocalPlayer).getInputVector()
                skill.dodge.start(direction, v)
                ClientOperationPayload.sendOperationToServer("dodge", v, direction.id)
                event.isCanceled = true
            }
        }
        isDodgeKeyInstantPress = false
    }

    @SubscribeEvent
    private fun guard(event: KeyboardInputTickEvent.Post) {
        val player = Minecraft.getInstance().player ?: return
        if (player.swinging) return // 优先级比使用物品/方块低
        if (player is IFightSkillHolder) {
            val preInput = (player as Entity).getPreInput()
            val skill = player.skillController ?: return
            if (SOFKeyMappings.GUARD.isDown && !player.isUsingItem) {
                if (skill is CommonFightSkillController && skill.parry.isPlaying()) return
                if (!skill.guard.isPlaying()) {
                    skill.guard.start {
                        // 防守检测上不是很敏感，因此需要确保客户端已经开始防守了以后再给服务端同步指令
                        ClientOperationPayload.sendOperationToServer("guard")
                    }
                }
            } else {
                preInput.clearIfPresent("guard") {
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
    private fun parry(event: KeyboardInputTickEvent.Post) {
        val player = Minecraft.getInstance().player ?: return
        if (player !is IFightSkillHolder) return
        val skillController = player.skillController ?: return
        if (skillController !is CommonFightSkillController) return

        while (SOFKeyMappings.PARRY.consumeClick()) {
            if (skillController.guard.isPlaying() && !skillController.parry.isPlaying()) {
                skillController.parry.start {
                    ClientOperationPayload.sendOperationToServer("parry")
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
                player.swinging = false
            } else if ((skill != null && skill.isPlayingSkill { !it.isCancelled }) || (player as IEntityAnimatable<*>).shouldOperateFreezing()) {
                // 在普通连招过程中可以按住s阻止前移
                if (input.forwardImpulse < 0 && skill?.combo?.isPlaying() == true) {
                    player.deltaMovement = Vec3(0.0, player.deltaMovement.y, 0.0)
                }
                input.forwardImpulse = 0f
                input.leftImpulse = 0f
                input.up = false
                input.down = false
                input.left = false
                input.right = false
                input.jumping = false
                input.shiftKeyDown = false
                player.sprintTriggerTime = -1
                player.swinging = false
            }
        }
    }

    /**
     * 在释放技能/受击时禁用除了攻击以外的交互
     */
    @SubscribeEvent
    private fun interactStop(event: InputEvent.InteractionKeyMappingTriggered) {
        val player = Minecraft.getInstance().player ?: return
        if ((player is IFightSkillHolder && player.skillController?.isPlayingSkill() == true) || (player as IEntityAnimatable<*>).shouldOperateFreezing()) {
            if (event.isAttack) return
            player.stopUsingItem()
            event.setSwingHand(false) // 很重要，防抖动
            event.isCanceled = true
        }
    }

    @SubscribeEvent
    private fun preventInput(event: OnPreInputExecuteEvent.Pre) {
        val animatable = event.holder as? IEntityAnimatable<*> ?: return
        val isHitting = animatable.isPlayingHitAnim { !it.isCancelled }
        val isParried = ParryAnimSkill.PARRY_SYNCED_ANIM.any { it.value.isPlaying(animatable) { !it.isCancelled } }
        event.isCanceled = isHitting || isParried
    }

}