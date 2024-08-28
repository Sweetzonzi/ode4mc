package cn.solarmoon.solarmoon_core.api.cap.item

import net.minecraft.core.HolderLookup
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.neoforged.neoforge.items.ItemStackHandler
import java.util.*

/**
 * 用于快速修改带有blockentity信息的物品的容器内容（也就是依靠[BlockEntity.saveToItem]方法保存信息的物品）
 */
object TileItemContainerHelper {

    @JvmStatic
    fun getInventory(stack: ItemStack, provider: HolderLookup.Provider): ItemStackHandler? {
        val tag = stack.get(DataComponents.BLOCK_ENTITY_DATA)?.copyTag()
        var inv: ItemStackHandler? = null
        if (tag != null && tag.contains(ItemHandlerHelper.ITEM)) {
            inv = ItemStackHandler()
            inv.deserializeNBT(provider, tag.getCompound(ItemHandlerHelper.ITEM))
        }
        return inv
    }

    @JvmStatic
    fun setInventory(stack: ItemStack, inv: ItemStackHandler, provider: HolderLookup.Provider) {
        val tag: CompoundTag? = stack.get(DataComponents.BLOCK_ENTITY_DATA)?.copyTag()
        tag?.let {
            it.put(ItemHandlerHelper.ITEM, inv.serializeNBT(provider))
            stack.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(tag))
        }
    }

}