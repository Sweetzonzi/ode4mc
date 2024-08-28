package test

import cn.solarmoon.solarmoon_core.SolarMoonCore
import cn.solarmoon.solarmoon_core.api.blockentity.SyncedEntityBlock
import cn.solarmoon.solarmoon_core.api.blockstate.IBedPartState
import cn.solarmoon.solarmoon_core.api.blockstate.IHorizontalFacingState
import cn.solarmoon.solarmoon_core.api.blockstate.IWaterLoggedState
import cn.solarmoon.solarmoon_core.registry.common.CommonAttachments
import net.minecraft.core.BlockPos
import net.minecraft.world.InteractionHand
import net.minecraft.world.ItemInteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape
import net.neoforged.neoforge.capabilities.Capabilities
import net.neoforged.neoforge.fluids.FluidUtil

class b(properties: Properties) : SyncedEntityBlock(properties), IWaterLoggedState, IHorizontalFacingState, IBedPartState {

    override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape {
        return Shapes.block()
    }

    override fun getBlockEntityType(): BlockEntityType<*> {
        return Cap.testbe.get()
    }

    override fun useItemOn(
        stack: ItemStack,
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hand: InteractionHand,
        hitResult: BlockHitResult
    ): ItemInteractionResult {
        level.getCapability(Capabilities.FluidHandler.BLOCK, pos, hitResult.direction)?.let {
            if (FluidUtil.interactWithFluidHandler(player, hand, it)) {
                return ItemInteractionResult.SUCCESS
            }
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult)
    }

}