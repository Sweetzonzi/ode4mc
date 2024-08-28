package cn.solarmoon.solarmoon_core.api.entry_builder.common

import com.mojang.serialization.Codec
import net.neoforged.neoforge.attachment.AttachmentType
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import java.util.function.Supplier

class AttachmentBuilder<A>(private val attachmentDeferredRegister: DeferredRegister<AttachmentType<*>>) {

    private var id: String = ""
    private var defaultValue: Supplier<A>? = null
    private var serializer: Codec<A>? = null

    fun id(id: String) = apply { this.id = id }

    /**
     * 为空时返回的默认值，必填
     */
    fun defaultValue(value: Supplier<A>) = apply { this.defaultValue = value }

    /**
     * 编码器，如果需要存储则需填入，选填
     */
    fun serializer(codec: Codec<A>) = apply { this.serializer = codec }

    fun build(): DeferredHolder<AttachmentType<*>, AttachmentType<A>> = attachmentDeferredRegister.register(id,
        Supplier {
            val builder = AttachmentType.builder(defaultValue!!)
            serializer?.let { builder.serialize(it) }
            builder.build()
        })

}