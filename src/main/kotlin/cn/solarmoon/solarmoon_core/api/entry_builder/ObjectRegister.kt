package cn.solarmoon.solarmoon_core.api.entry_builder

import cn.solarmoon.solarmoon_core.api.entry_builder.common.AttachmentBuilder
import cn.solarmoon.solarmoon_core.api.entry_builder.common.BlockBuilder
import cn.solarmoon.solarmoon_core.api.entry_builder.common.BlockEntityBuilder
import cn.solarmoon.solarmoon_core.api.entry_builder.common.ItemBuilder
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.common.EventBusSubscriber.Bus
import net.neoforged.fml.javafmlmod.FMLModContainer
import net.neoforged.neoforge.attachment.AttachmentType
import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.neoforge.registries.NeoForgeRegistries
import thedarkcolour.kotlinforforge.neoforge.forge.FORGE_BUS
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS

class ObjectRegister(modId: String) {

    var modBus: IEventBus? = null

    val itemDeferredRegister: DeferredRegister<Item> = DeferredRegister.create(Registries.ITEM, modId)
    val blockDeferredRegister: DeferredRegister<Block> = DeferredRegister.create(Registries.BLOCK, modId)
    val blockEntityDeferredRegister: DeferredRegister<BlockEntityType<*>> = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, modId)
    val attachmentDeferredRegister: DeferredRegister<AttachmentType<*>> = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, modId)

    fun register(bus: IEventBus) {
        modBus = bus
        itemDeferredRegister.register(bus)
        blockDeferredRegister.register(bus)
        blockEntityDeferredRegister.register(bus)
        attachmentDeferredRegister.register(bus)
    }

    fun <I: Item> item() = ItemBuilder<I>(itemDeferredRegister)
    fun <B: Block> block() = BlockBuilder<B>(blockDeferredRegister)
    fun <B: BlockEntity> blockentity() = BlockEntityBuilder<B>(blockEntityDeferredRegister, modBus!!)
    fun <A> attachment() = AttachmentBuilder<A>(attachmentDeferredRegister)

}