package cn.solarmoon.solarmoon_core.api.util

import net.minecraft.core.BlockPos
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import java.util.*

object DropUtil {

    /**
     * 生成基础掉落物
     * 坐标中心位置
     * 固定概率掉落
     */
    @JvmStatic
    fun summonDrop(item: Item, level: Level, pos: BlockPos, min: Int, max: Int) {
        val rand = Random()
        val drop = ItemEntity(level, pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5, ItemStack(item, rand.nextInt(min, max)))
        level.addFreshEntity(drop)
    }

    /**
     * 生成基础掉落物
     * 更为精确的位置
     * 固定概率掉落
     */
    @JvmStatic
    fun summonDrop(item: Item, level: Level, vec3: Vec3, origin: Int, bound: Int) {
        val rand = Random()
        val drop = ItemEntity(level, vec3.x, vec3.y, vec3.z, ItemStack(item, rand.nextInt(origin, bound)))
        level.addFreshEntity(drop)
    }

    /**
     * 生成基础掉落物
     * 坐标中心位置（不包括y）
     */
    @JvmStatic
    fun summonDrop(item: Item, level: Level, pos: BlockPos, amount: Int) {
        val drop = ItemEntity(level, pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5, ItemStack(item, amount))
        level.addFreshEntity(drop)
    }

    /**
     * 生成基础掉落物
     * 坐标中心位置（不包括y）
     * 附带一个自定义初速度
     */
    @JvmStatic
    fun summonDrop(item: ItemStack, level: Level, pos: BlockPos, movement: Vec3) {
        val drop = ItemEntity(level, pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5, item)
        drop.deltaMovement = movement
        level.addFreshEntity(drop)
    }

    /**
     * 生成基础掉落物
     * 坐标中心位置（不包括y）
     * 附带一个自定义初速度
     */
    @JvmStatic
    fun summonDrop(item: Item, level: Level, pos: BlockPos, movement: Vec3, amount: Int) {
        val drop = ItemEntity(level, pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5, ItemStack(item, amount))
        drop.deltaMovement = movement
        level.addFreshEntity(drop)
    }

    /**
     * 生成基础掉落物
     * 坐标中心位置（不包括y）
     * 附带一个自定义初速度
     * 直接生成一整个列表的掉落物
     */
    @JvmStatic
    fun summonDrop(items: List<Item>, level: Level, pos: BlockPos, movement: Vec3, amount: Int) {
        for (item in items) {
            val drop = ItemEntity(level, pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5, ItemStack(item, amount))
            drop.deltaMovement = movement
            level.addFreshEntity(drop)
        }
    }

    /**
     * 生成基础掉落物
     */
    @JvmStatic
    fun summonDrop(item: Item, level: Level, vec3: Vec3, amount: Int) {
        val drop = ItemEntity(level, vec3.x, vec3.y, vec3.z, ItemStack(item, amount))
        level.addFreshEntity(drop)
    }

    /**
     * 生成基础掉落物
     * 以坐标中心为生成点
     */
    @JvmStatic
    fun summonDrop(stack: ItemStack, level: Level, pos: BlockPos) {
        val drop = ItemEntity(level, pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5, stack)
        level.addFreshEntity(drop)
    }

    /**
     * 生成基础掉落物
     * 以坐标中心（不包括y）为生成点
     * 附带一个初速度
     */
    @JvmStatic
    fun summonDrop(stacks: List<ItemStack>, level: Level, pos: BlockPos, movement: Vec3) {
        for (stack in stacks) {
            val drop = ItemEntity(level, pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5, stack)
            drop.deltaMovement = movement
            level.addFreshEntity(drop)
        }
    }

    /**
     * 生成基础掉落物
     * 以三维坐标为生成点（更为精细）
     */
    @JvmStatic
    fun summonDrop(item: ItemStack, level: Level, vec3: Vec3) {
        val drop = ItemEntity(level, vec3.x, vec3.y, vec3.z, item)
        level.addFreshEntity(drop)
    }

    /**
     * 生成基础掉落物
     * 以三维坐标为生成点（更为精细）
     * 直接生成一整个列表的掉落物
     */
    @JvmStatic
    fun summonDrop(stacks: List<ItemStack>, level: Level, vec3: Vec3) {
        for (stack in stacks) {
            summonDrop(stack, level, vec3)
        }
    }

    /**
     * 像give一样给玩家添加物品，如果没能成功，则将物品以掉落物的形式给到玩家身边
     * Forge：ItemHandlerHelper方法一致
     */
    @JvmStatic
    fun addItemToInventory(player: Player, stack: ItemStack) {
        val result = player.addItem(stack)
        if (!result) {
            player.drop(stack, false)
        }
    }

    /**
     * 像give一样给玩家添加物品，如果没能成功，则将物品以掉落物的形式给到指定坐标（坐标中心）
     */
    @JvmStatic
    fun addItemToInventory(player: Player, stack: ItemStack, pos: BlockPos) {
        val result = player.addItem(stack)
        if (!result) {
            val vec3 = Vec3(pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5)
            summonDrop(stack, player.level(), vec3)
        }
    }

    /**
     * 像give一样给玩家添加物品，如果没能成功，则将物品以掉落物的形式给到指定坐标（坐标中心）
     * 完美模式：当还剩1个物品时能够把返还的物品准确回到点击的手上（仅限于物品交互，空手交互不可用此方法）
     */
    @JvmStatic
    fun addItemToInventoryPerfectly(
        player: Player,
        add: ItemStack,
        pos: BlockPos,
        heldItem: ItemStack,
        hand: InteractionHand
    ) {
        if (heldItem.count == 1) player.setItemInHand(hand, add)
        else addItemToInventory(player, add, pos)
    }

    /**
     * 像give一样给玩家添加物品，如果没能成功，则将物品以掉落物的形式给到玩家身边
     * 完美模式：当还剩1个物品时能够把返还的物品准确回到点击的手上（仅限于物品交互，空手交互不可用此方法）
     */
    @JvmStatic
    fun addItemToInventoryPerfectly(player: Player, add: ItemStack, heldItem: ItemStack, hand: InteractionHand) {
        if (heldItem.count == 1) player.setItemInHand(hand, add)
        else addItemToInventory(player, add)
    }

}