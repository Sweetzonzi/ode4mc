package cn.solarmoon.spark_core.api.renderer

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.world.entity.HumanoidArm
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions

/**
 * 可以转换物品的第三人称下的显示
 */
interface IItemExtensionsWith3rdPTrans: IClientItemExtensions {//

    fun applyTransformTo3rdPerson(entity: LivingEntity, stack: ItemStack, context: ItemDisplayContext, arm: HumanoidArm, poseStack: PoseStack, buffer: MultiBufferSource, light: Int)

}