package cn.solarmoon.spirit_of_fight.fighter.player

import cn.solarmoon.spark_core.SparkCore
import cn.solarmoon.spark_core.animation.vanilla.asAnimatable
import cn.solarmoon.spark_core.entity.preinput.PreInput
import cn.solarmoon.spark_core.entity.preinput.getPreInput
import cn.solarmoon.spark_core.local_control.LocalInputController
import cn.solarmoon.spark_core.skill.getTypedSkillController
import cn.solarmoon.spirit_of_fight.SpiritOfFight
import cn.solarmoon.spirit_of_fight.feature.fight_skill.controller.FightSkillController
import cn.solarmoon.spirit_of_fight.feature.fight_skill.controller.SwordFightSkillController
import cn.solarmoon.spirit_of_fight.feature.fight_skill.sync.ClientOperationPayload
import cn.solarmoon.spirit_of_fight.registry.client.SOFKeyMappings
import net.minecraft.client.Minecraft
import net.minecraft.client.player.Input
import net.minecraft.client.player.LocalPlayer
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import net.neoforged.neoforge.client.event.InputEvent
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent

object PlayerLocalController: LocalInputController() {

    val attackKey get() = Minecraft.getInstance().options.keyAttack

    override fun laterInit() {
        addTickingKey(attackKey)
        addTickingKey(SOFKeyMappings.GUARD)
        addTickingKey(SOFKeyMappings.PARRY)
    }

    fun attack(player: LocalPlayer, skillController: FightSkillController) {
        val preInput = player.getPreInput()
        val index = skillController.comboIndex.get()
        preInput.setInput("combo", 5) {
            skillController.getComboSkill().activate()
            addPackage(ClientOperationPayload(player.id, preInput.id, Vec3.ZERO, index))
            skillController.comboIndex.increment()
        }
    }

    fun guard(player: LocalPlayer, skillController: FightSkillController) {
        if (skillController.getGuardSkill().isActive()) return
        val preInput = player.getPreInput()

        preInput.setInput("guard", 5) {
            skillController.getGuardSkill().activate()
            addPackage(ClientOperationPayload(player.id, preInput.id, Vec3.ZERO, 0))
        }
    }

    fun guardStop(player: LocalPlayer, skillController: FightSkillController) {
        if (skillController.getGuardSkill().isActive()) {
            skillController.getGuardSkill().end()
            addPackage(ClientOperationPayload(player.id, "guard_stop", Vec3.ZERO, 0))
        }
    }

    fun parry(player: LocalPlayer, skillController: SwordFightSkillController) {
        SparkCore.LOGGER.info("wowo")
        if (player.asAnimatable().animController.isPlaying(skillController.getGuardSkill().animName)) {
            SparkCore.LOGGER.info("eee")
            skillController.getParrySkill().activate()
            addPackage(ClientOperationPayload(player.id, "parry", Vec3.ZERO, 0))
        }
    }

    override fun tick(player: LocalPlayer, input: Input) {
        val skillController = player.getTypedSkillController<FightSkillController>() ?: return

        onRelease(attackKey) {
            if (shouldAttack(player)) {
                attack(player, skillController)
            }
        }

        onPress(SOFKeyMappings.GUARD) {
            guard(player, skillController)
        }
        onRelease(SOFKeyMappings.GUARD) {
            guardStop(player, skillController)
        }

        onPressOnce(SOFKeyMappings.PARRY) {
            if (skillController is SwordFightSkillController) {
                parry(player, skillController)
            }
        }

        preInputControl(player, player.getPreInput())
    }

    fun preInputControl(player: LocalPlayer, preInput: PreInput) {
        val skillController = player.getTypedSkillController<FightSkillController>() ?: return

        // 不在进行任何技能时可释放预输入
        if (!skillController.isPlaying()) {
            skillController.comboIndex.set(0)
            preInput.executeIfPresent()
        }

        // 连招1-2阶段可以变招
        player.asAnimatable().animData.playData.getMixedAnimation(skillController.getComboSkill(0).animName)?.let {
            if (preInput.hasInput("combo") && it.isTickIn(0.05, 0.15)) preInput.executeIfPresent()
        }

        // 格挡时可预输入闪避
    }

    override fun onInteract(player: LocalPlayer, event: InputEvent.InteractionKeyMappingTriggered) {
        if (event.isAttack && shouldAttack(player)) {
            event.setSwingHand(false)
            event.isCanceled = true
        }
    }

    override fun updateMovement(player: LocalPlayer, event: MovementInputUpdateEvent) {

    }

    fun shouldAttack(player: LocalPlayer): Boolean {
        player.getTypedSkillController<FightSkillController>() ?: return false
        val hit = Minecraft.getInstance().hitResult ?: return false
        // 如果目标是空气或实体，则无论如何默认进行攻击
        return if (hit.type in listOf(HitResult.Type.ENTITY, HitResult.Type.MISS)) true
        // 如果是方块，则看是否按压时间短于0.25秒，超出则正常挖掘
        else getPressTick(attackKey) < 5
    }

//    private var isDodgeKeyInstantPress = false

//    @SubscribeEvent
//    private fun localInput(event: KeyboardInputTickEvent.Post) {
//        val player = Minecraft.getInstance().player ?: return
//        player.getTypedSkillController<FightSkillController>()?.localPlayerInput(player)
//    }
//
//    @SubscribeEvent
//    private fun attack(event: InputEvent.InteractionKeyMappingTriggered) {
//        val hit = Minecraft.getInstance().hitResult ?: return
//        val player = Minecraft.getInstance().player ?: return
//        player.getTypedSkillController<FightSkillController>()?.localPlayerInteractInput(player, event)
//        if (player !is IFightSkillHolder) return
//        val skillController = player.skillController ?: return
//        if (event.isAttack && hit.type in listOf(HitResult.Type.ENTITY, HitResult.Type.MISS)) {
//            SparkSkills.PLAYER_SWORD_COMBO_0.value().activate(player as IEntityAnimatable<*>)
//            ClientOperationPayload.sendOperationToServer("combo")
////            var shouldCombo = true
////            for ((index, skill) in skillController.specialAttackSkillGroup.withIndex()) {
////                if (skill !is ConcentrationAttackAnimSkill && skill.canRelease) {
////                    skill.start {
////                        ClientOperationPayload.sendOperationToServer(index.toString())
////                    }
////                    shouldCombo = false
////                    break
////                }
////            }
////            if (shouldCombo) {
////                skillController.combo.start(false) {
////                    ClientOperationPayload.sendOperationToServer("combo")
////                }
////                skillController.combo.getPlayingAnim()?.let {
////                    // 这一段使得连招在50-150ms之间可以变招
////                    val changeNode = skillController.combo.attackChangeNode[skillController.combo.index]
////                    if (player.getPreInput().hasInput("combo") && changeNode != null && !it.isInTransition && it.isTickIn(0.05, 0.15)) {
////                        skillController.combo.start(true) {
////                            ClientOperationPayload.sendOperationToServer("combo_switch")
////                        }
////                        player.getPreInput().executeIfPresent("combo")
////                    }
////                }
////            }
//            event.setSwingHand(false)
//            event.isCanceled = true
//        }
//    }
//
//    @SubscribeEvent
//    private fun specialAttack(event: KeyboardInputTickEvent.Post) {
//        val player = Minecraft.getInstance().player ?: return
//        if (player !is IFightSkillHolder) return
//        val skillController = player.skillController ?: return
//        while (SOFKeyMappings.SPECIAL_ATTACK.consumeClick()) {
//            for ((index, skill) in skillController.specialAttackSkillGroup.withIndex()) {
//                if (skill is ConcentrationAttackAnimSkill && skill.canRelease) {
//                    skill.start {
//                        ClientOperationPayload.sendOperationToServer(index.toString())
//                    }
//                    break
//                }
//            }
//        }
//    }
//
//    @SubscribeEvent
//    private fun dodge(event: KeyboardInputTickEvent.Post) {
//        run {
//            val player = Minecraft.getInstance().player ?: return@run
//            val input = player.input
//            val direction = MoveDirection.getByInput(input)
//            if (direction != null && player is IFightSkillHolder && player.onGround()) {
//                if (SOFKeyMappings.DODGE.key.value == event.options.keyShift.key.value) {
//                    input.shiftKeyDown = false
//                    if (player.isCrouching) return@run
//                } // 如果和蹲键重合，则会取消蹲姿
//                if (!isDodgeKeyInstantPress) return@run
//                val skill = player.skillController ?: return@run
//                val v = (player as LocalPlayer).getInputVector()
//                skill.dodge.start(direction, v) {
//                    ClientOperationPayload.sendOperationToServer("dodge", v, direction.id)
//                }
//            }
//        }
//        isDodgeKeyInstantPress = false
//    }
//
//    @SubscribeEvent
//    private fun guard(event: KeyboardInputTickEvent.Post) {
//        val player = Minecraft.getInstance().player ?: return
//        if (player.swinging) return // 优先级比使用物品/方块低
//        if (player is IFightSkillHolder) {
//            val preInput = (player as Entity).getPreInput()
//            val skill = player.skillController ?: return
//            if (SOFKeyMappings.GUARD.isDown && !player.isUsingItem) {
//                if (skill is CommonFightSkillController && skill.parry.isPlaying()) return
//                if (!skill.guard.isPlaying()) {
//                    skill.guard.start {
//                        // 防守检测上不是很敏感，因此需要确保客户端已经开始防守了以后再给服务端同步指令
//                        ClientOperationPayload.sendOperationToServer("guard")
//                    }
//                }
//            } else {
//                preInput.clearIfPresent("guard") {
//                    ClientOperationPayload.sendOperationToServer("guard_clear")
//                } // 这里多加一条清除确保客户端一定取消操作
//
//                if (skill.guard.isPlaying { !it.isInTransition }) {
//                    skill.guard.stop {
//                        ClientOperationPayload.sendOperationToServer("guard_stop")
//                    }
//                }
//            }
//        }
//    }
//
//    @SubscribeEvent
//    private fun parry(event: KeyboardInputTickEvent.Post) {
//        val player = Minecraft.getInstance().player ?: return
//        if (player !is IFightSkillHolder) return
//        val skillController = player.skillController ?: return
//        if (skillController !is CommonFightSkillController) return
//
//        while (SOFKeyMappings.PARRY.consumeClick()) {
//            if (skillController.guard.isPlaying() && !skillController.parry.isPlaying()) {
//                skillController.parry.start {
//                    ClientOperationPayload.sendOperationToServer("parry")
//                }
//            }
//        }
//    }
//
//    @SubscribeEvent
//    private fun stop(event: MovementInputUpdateEvent) {
//        val player = event.entity as LocalPlayer
//        val input = player.input
//        if (player is IFightSkillHolder) {
//            val skill = player.skillController
//            if (skill != null && skill.guard.isStanding { !it.isInTransition }) {
//                input.forwardImpulse /= 4f
//                input.leftImpulse /= 4f
//                input.jumping = false
//                input.shiftKeyDown = false
//                player.sprintTriggerTime = -1
//                player.swinging = false
//            } else if ((skill != null && skill.isPlayingSkill { !it.isCancelled }) || (player as IEntityAnimatable<*>).shouldOperateFreezing()) {
//                // 在普通连招过程中可以按住s阻止前移
//                if (input.forwardImpulse < 0 && skill?.combo?.isPlaying() == true) {
//                    player.deltaMovement = Vec3(0.0, player.deltaMovement.y, 0.0)
//                }
//                input.forwardImpulse = 0f
//                input.leftImpulse = 0f
//                input.up = false
//                input.down = false
//                input.left = false
//                input.right = false
//                input.jumping = false
//                input.shiftKeyDown = false
//                player.sprintTriggerTime = -1
//                player.swinging = false
//            }
//        }
//    }
//
//    /**
//     * 在释放技能/受击时禁用除了攻击以外的交互
//     */
//    @SubscribeEvent
//    private fun interactStop(event: InputEvent.InteractionKeyMappingTriggered) {
//        val player = Minecraft.getInstance().player ?: return
//        if ((player is IFightSkillHolder && player.skillController?.isPlayingSkill() == true) || (player as IEntityAnimatable<*>).shouldOperateFreezing()) {
//            if (event.isAttack) return
//            player.stopUsingItem()
//            event.setSwingHand(false) // 很重要，防抖动
//            event.isCanceled = true
//        }
//    }
//
//    @SubscribeEvent
//    private fun preventInput(event: OnPreInputExecuteEvent.Pre) {
//        val animatable = event.holder as? IEntityAnimatable<*> ?: return
//        val isHitting = animatable.isPlayingHitAnim { !it.isCancelled }
//        val isParried = ParryAnimSkill.PARRY_SYNCED_ANIM.any { it.value.isPlaying(animatable) { !it.isCancelled } }
//        event.isCanceled = isHitting || isParried
//    }

}