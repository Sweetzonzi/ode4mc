package cn.solarmoon.spark_core.api.cap.fluid

import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.player.Player
import net.neoforged.neoforge.fluids.FluidStack
import net.neoforged.neoforge.fluids.FluidUtil
import net.neoforged.neoforge.fluids.capability.IFluidHandler
import net.neoforged.neoforge.fluids.capability.templates.FluidTank

object FluidHandlerHelper {

    const val FLUID = "FluidHandler"

    /**
     * 从手中物品放入液体
     * @return 成功会返回true
     */
    @JvmStatic
    fun putFluid(tank: IFluidHandler, player: Player, hand: InteractionHand, playSound: Boolean): Boolean {
        val heldItem = player.getItemInHand(hand)
        val result = FluidUtil.tryEmptyContainer(heldItem, tank, Int.MAX_VALUE, if (playSound) player else null, true)
        if (result.isSuccess) {
            if (!player.isCreative) player.setItemInHand(hand, result.getResult())
            return true
        }
        return false
    }

    /**
     * 从手中物品拿取液体
     * @return 成功会返回true
     */
    @JvmStatic
    fun takeFluid(tank: IFluidHandler, player: Player, hand: InteractionHand, playSound: Boolean): Boolean {
        val heldItem = player.getItemInHand(hand)
        val result = FluidUtil.tryFillContainer(heldItem, tank, Int.MAX_VALUE, if (playSound) player else null, true)
        if (result.isSuccess) {
            if (!player.isCreative) player.setItemInHand(hand, result.getResult())
            return true
        }
        return false
    }

    @JvmStatic
    fun clearTank(tank: IFluidHandler) {
        if (tank is FluidTank) tank.fluid = FluidStack.EMPTY
        else setTank(tank, FluidStack.EMPTY)
    }

    /**
     * 根据液体容量获取大小百分比
     */
    @JvmStatic
    fun getScale(tank: IFluidHandler): Float {
        val stored = tank.getFluidInTank(0).amount
        val capacity = tank.getTankCapacity(0)
        return stored.toFloat() / capacity
    }

    /**
     * 用于强制设置物品里的液体（前者是被设置的，后者是设置的内容）
     */
    @JvmStatic
    fun setTank(tank: IFluidHandler, fluidStack: FluidStack) {
        if (tank is FluidTank) tank.fluid = fluidStack
        else {
            tank.drain(Int.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE)
            tank.fill(fluidStack, IFluidHandler.FluidAction.EXECUTE)
        }
    }

    /**
     * 检查两个流体栈是否完全匹配（包括数量）
     */
    @JvmStatic
    fun isMatch(fluid1: FluidStack, fluid2: FluidStack, compareAmount: Boolean, compareComponents: Boolean): Boolean {
        val amountMatch = !compareAmount || fluid1.amount == fluid2.amount
        val NBTMatch = !compareComponents || fluid1.components == fluid2.components
        val typeMatch = fluid1.fluid === fluid2.fluid
        return typeMatch && amountMatch && NBTMatch
    }

    /**
     * 检查是否还能放入液体
     *
     * 规则为已有液体必须相匹配，且剩余空间大于等于要放入的液体（或者为空）
     *
     * 相反的检查可以用contains
     */
    @JvmStatic
    fun canStillPut(tankL: IFluidHandler, fluidStack: FluidStack): Boolean {
        val remain = tankL.getTankCapacity(0) - tankL.getFluidInTank(0).amount
        val put = fluidStack.amount
        val match = tankL.getFluidInTank(0) == fluidStack
        return remain >= put && (match || tankL.getFluidInTank(0).isEmpty)
    }

}