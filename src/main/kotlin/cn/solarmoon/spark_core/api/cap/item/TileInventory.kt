package cn.solarmoon.spark_core.api.cap.item

import net.minecraft.world.level.block.entity.BlockEntity
import net.neoforged.neoforge.items.ItemStackHandler

class TileInventory(
    val be: BlockEntity,
    size: Int,
    private val slotLimit: Int = 64
): ItemStackHandler(size) {

    override fun getSlotLimit(slot: Int): Int {
        return slotLimit
    }

    override fun onContentsChanged(slot: Int) {
        super.onContentsChanged(slot)
        be.invalidateCapabilities()
        be.setChanged()
    }

}