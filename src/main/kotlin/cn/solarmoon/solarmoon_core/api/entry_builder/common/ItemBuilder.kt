package cn.solarmoon.solarmoon_core.api.entry_builder.common

import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import java.util.function.Supplier

class ItemBuilder<I: Item>(private val itemDeferredRegister: DeferredRegister<Item>) {

    private var id: String = ""
    private var item: Supplier<I>? = null

    fun id(id: String) = apply { this.id = id }
    fun bound(item: Supplier<I>) = apply { this.item = item }

    fun build(): DeferredHolder<Item, I> = itemDeferredRegister.register(id, item!!)

}