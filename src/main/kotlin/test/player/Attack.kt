package test.player

import cn.solarmoon.spark_core.api.network.CommonNetData
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.entity.player.PlayerRenderer
import net.minecraft.world.phys.HitResult
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.client.event.InputEvent
import net.neoforged.neoforge.network.PacketDistributor

class Attack {

    @SubscribeEvent
    private fun onClick(event: InputEvent.InteractionKeyMappingTriggered) {
        val hit = Minecraft.getInstance().hitResult ?: return
        val player = Minecraft.getInstance().player ?: return
        val hand = event.hand
        if (event.isAttack && hit.type in listOf(HitResult.Type.ENTITY, HitResult.Type.MISS)) {
            PacketDistributor.sendToServer(CommonNetData(floatValue = player.yRot, message = "attack"))
            event.setSwingHand(false)
            event.isCanceled = true
            // 这里默认会挥手，挥手的同时会将模型旋转过渡到当前视角，这里保留挥手以便过渡，不过可在渲染内直接切断挥手的渲染即可
        }
    }

}