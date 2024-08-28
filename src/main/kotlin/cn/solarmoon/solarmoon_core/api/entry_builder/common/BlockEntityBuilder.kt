package cn.solarmoon.solarmoon_core.api.entry_builder.common

import com.mojang.datafixers.types.Type
import net.minecraft.core.Direction
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.capabilities.BlockCapability
import net.neoforged.neoforge.capabilities.ICapabilityProvider
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import java.util.function.Supplier

class BlockEntityBuilder<B : BlockEntity>(
    private val blockEntityDeferredRegister: DeferredRegister<BlockEntityType<*>>,
    private val modBus: IEventBus
) {

    private var id: String = ""
    private var be: BlockEntitySupplier<B>? = null
    private var validBlocks: Supplier<Array<Block>>? = null
    private var cap: BlockCapability<*, *>? = null
    private var capP: ICapabilityProvider<B, *, *>? = null

    fun id(id: String) = apply { this.id = id }
    fun bound(blockentity: BlockEntitySupplier<B>) = apply { this.be = blockentity }
    fun validBlocks(blocks: Supplier<Array<Block>>) = apply { this.validBlocks = blocks }

    fun <T, C: Any?> capability(
        cap: BlockCapability<T, C>,
        provider: ICapabilityProvider<B, C, T>
    ) = apply {
        this.cap = cap
        this.capP = provider
    }

    fun build(): DeferredHolder<BlockEntityType<*>, BlockEntityType<B>> {
        val reg = blockEntityDeferredRegister.register(id, Supplier {
            BlockEntityType.Builder.of(be!!, *validBlocks!!.get()).build(null)
        })
        cap?.let { capP?.let { modBus.addListener { e: RegisterCapabilitiesEvent -> registerCap(reg, e) } } }
        return reg
    }

    @Suppress("UNCHECKED_CAST")
    private fun registerCap(reg: DeferredHolder<BlockEntityType<*>, BlockEntityType<B>>, event: RegisterCapabilitiesEvent) {
        event.registerBlockEntity(cap as BlockCapability<Any, Any?>, reg.get(), capP as ICapabilityProvider<B, Any?, Any>)
    }

}


