package cn.solarmoon.solarmoon_core.api.cap.item

import cn.solarmoon.solarmoon_core.api.util.DropUtil.addItemToInventory
import cn.solarmoon.solarmoon_core.api.util.DropUtil.summonDrop
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.phys.Vec3
import net.neoforged.neoforge.common.util.INBTSerializable
import net.neoforged.neoforge.items.IItemHandler
import java.util.function.Consumer

object ItemHandlerHelper {

    const val ITEM = "ItemHandler"

    /**
     * 插入容纳的物品（按物品栈插入）
     *
     * 逻辑为从第一格开始尝试插入直到插入成功
     *
     * 会返回计算消耗后的物品栈，并不会消耗物品，因此不要再用shrink！用setItem！
     */
    @JvmStatic
    fun insertItem(inv: IItemHandler, itemStack: ItemStack): ItemStack {
        return insertItem(inv, itemStack, 0, inv.slots - 1)
    }

    /**
     * 插入容纳的物品（按物品栈插入）
     *
     * 逻辑为从第一格开始尝试插入直到插入成功
     *
     * 会返回计算消耗后的物品栈，并不会消耗物品，因此不要再用shrink！用setItem！
     */
    @JvmStatic
    fun insertItem(inv: IItemHandler, itemStack: ItemStack, slotMin: Int, slotMax: Int): ItemStack {
        var result = itemStack
        for (i in slotMin..slotMax) {
            result = inv.insertItem(i, itemStack, false)
            if (!ItemStack.matches(result, itemStack)) break
        }
        return result
    }

    /**
     * 从中提取物品
     *
     * 默认逻辑从最后一栏开始提取，按物品栈提取，没提取会返回空栈<br></br>
     */
    @JvmStatic
    fun extractItem(inv: IItemHandler, count: Int, slotMin: Int, slotMax: Int): ItemStack {
        var stack = ItemStack.EMPTY
        for (i in slotMax downTo slotMin) {
            stack = inv.extractItem(i, count, false)
            if (!stack.isEmpty) {
                break
            }
        }
        return stack
    }

    /**
     * 从中提取物品
     *
     * 默认逻辑从最后一栏开始提取，按物品栈提取，没提取会返回空栈
     */
    @JvmStatic
    fun extractItem(inv: IItemHandler, count: Int): ItemStack {
        return extractItem(inv, count, 0, inv.slots - 1)
    }

    /**
     * 从tag中读取inventory信息
     */
    @JvmStatic
    fun setInventory(invToBeSet: INBTSerializable<CompoundTag>, set: CompoundTag, provider: HolderLookup.Provider) {
        invToBeSet.deserializeNBT(provider, set.getCompound(ITEM))
    }

    /**
     * 获取容器内的所有物品
     */
    @JvmStatic
    fun getStacks(inv: IItemHandler): List<ItemStack> {
        val stacks: MutableList<ItemStack> = ArrayList()
        val maxSlots: Int = inv.slots
        for (i in 0 until maxSlots) {
            val stack: ItemStack = inv.getStackInSlot(i)
            //这里不能让stack为空，因为会插入EMPTY的stack，这样会妨碍List.isEmpty的检查
            if (!stack.isEmpty) {
                stacks.add(stack)
            }
        }
        return stacks
    }

    /**
     * 获取容器内物品总数
     */
    @JvmStatic
    fun getStacksAmount(inv: IItemHandler): Int {
        var amount = 0
        val maxSlots: Int = inv.slots
        for (i in 0 until maxSlots) {
            amount += inv.getStackInSlot(i).count
        }
        return amount
    }

    /**
     * 获取容器的最大可容纳物品量
     */
    @JvmStatic
    fun getItemCapability(inv: IItemHandler): Int {
        var total = 0
        for (i in 0 until inv.slots) {
            total += inv.getSlotLimit(i)
        }
        return total
    }

    /**
     * @return 获取容器当前物品和最大容积之比
     */
    @JvmStatic
    fun getScale(inv: IItemHandler): Float {
        return getStacksAmount(inv).toFloat() / getItemCapability(inv)
    }

    /**
     * 单独放入玩家手中物品
     * @return 成功返回true
     */
    @JvmStatic
    fun putItem(inv: IItemHandler, player: Player, hand: InteractionHand, count: Int, slotMin: Int, slotMax: Int): Boolean {
        val heldItem = player.getItemInHand(hand)
        if (!heldItem.isEmpty) {
            val simulativeItem = heldItem.copyWithCount(count)
            val result = insertItem(inv, simulativeItem, slotMin, slotMax)
            val countToShrink = count - result.count
            if (!player.isCreative) heldItem.shrink(countToShrink)
            return result != simulativeItem
        }
        return false
    }

    /**
     * 单独放入玩家手中物品
     * @return 成功返回true
     */
    @JvmStatic
    fun putItem(inv: IItemHandler, player: Player, hand: InteractionHand, count: Int): Boolean {
        return putItem(inv, player, hand, count, 0, inv.slots - 1)
    }

    /**
     * 玩家用手单独拿取物品（只适用空手拿取），超过的部分不会提取，放心使用
     * @return 成功返回true
     */
    @JvmStatic
    fun takeItem(inv: IItemHandler, player: Player, hand: InteractionHand, count: Int, slotMin: Int, slotMax: Int): Boolean {
        val heldItem = player.getItemInHand(hand)
        if (heldItem.isEmpty && getStacks(inv).isNotEmpty()) {
            val result = extractItem(inv, count, slotMin, slotMax)
            if (!result.isEmpty) {
                if (!player.isCreative) addItemToInventory(player, result)
                return true
            }
        }
        return false
    }

    /**
     * 玩家用手单独拿取物品（只适用空手拿取），超过的部分不会提取，放心使用
     * @return 成功返回true
     */
    @JvmStatic
    fun takeItem(inv: IItemHandler, player: Player, hand: InteractionHand, count: Int): Boolean {
        return takeItem(inv, player, hand, count, 0, inv.slots - 1)
    }

    /**
     * 基本的存物逻辑，似乎可通用
     *
     * 手不为空时存入，为空时疯狂取出
     * @return 无所谓装取，只要容器交互成功就返回true
     */
    @JvmStatic
    fun storage(inv: IItemHandler, player: Player, hand: InteractionHand, putCount: Int, takeCount: Int, slotMin: Int, slotMax: Int): Boolean {
        if (putItem(inv, player, hand, putCount, slotMin, slotMax)) return true
        return takeItem(inv, player, hand, takeCount, slotMin, slotMax)
    }

    /**
     * 基本的存物逻辑，似乎可通用
     *
     * 手不为空时存入，为空时疯狂取出
     * @return 无所谓装取，只要容器交互成功就返回true
     */
    @JvmStatic
    fun storage(inv: IItemHandler, player: Player, hand: InteractionHand, putCount: Int, takeCount: Int): Boolean {
        if (putItem(inv, player, hand, putCount, 0, inv.slots - 1)) return true
        return takeItem(inv, player, hand, takeCount, 0, inv.slots - 1)
    }

    /**
     * 特殊的取物逻辑，玩家蹲下时存入手中全部物品，站立时存入一个，取出同理
     *
     * 但要注意必须实现IBlockUseCaller接口，否则默认情况下将不调用蹲下后的逻辑
     * @return 成功返回true
     */
    @JvmStatic
    fun specialStorage(inv: IItemHandler, player: Player, hand: InteractionHand, slotMin: Int, slotMax: Int): Boolean {
        val heldItem = player.getItemInHand(hand)
        return if (!heldItem.isEmpty) {
            if (player.isCrouching) {
                putItem(inv, player, hand, heldItem.count, slotMin, slotMax)
            } else {
                putItem(inv, player, hand, 1, slotMin, slotMax)
            }
        } else {
            if (player.isCrouching) takeItem(inv, player, hand, 64, slotMin, slotMax)
            else takeItem(inv, player, hand, 1, slotMin, slotMax)
        }
    }

    /**
     * 特殊的取物逻辑，玩家蹲下时存入手中全部物品，站立时存入一个，取出同理
     *
     * 但要注意必须实现IBlockUseCaller接口，否则默认情况下将不调用蹲下后的逻辑
     * @return 成功返回true
     */
    @JvmStatic
    fun specialStorage(inv: IItemHandler, player: Player, hand: InteractionHand): Boolean {
        return specialStorage(inv, player, hand, 0, inv.slots - 1)
    }

    @JvmStatic
    fun clearInv(inv: IItemHandler) {
        getStacks(inv).forEach(Consumer { stack: ItemStack -> stack.count = 0 })
    }

    /**
     * 泵出所有物品
     */
    @JvmStatic
    fun pumpOutAllItems(inv: IItemHandler, blockEntity: BlockEntity, positionAddon: Vec3) {
        val level = blockEntity.level
        val pos = blockEntity.blockPos
        level?.let { summonDrop(getStacks(inv), it, pos.center.add(positionAddon)) }
        clearInv(inv)
    }

}