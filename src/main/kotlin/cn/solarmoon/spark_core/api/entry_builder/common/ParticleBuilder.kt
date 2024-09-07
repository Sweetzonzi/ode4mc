package cn.solarmoon.spark_core.api.entry_builder.common

import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.particles.ParticleType
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import java.util.function.Supplier

class ParticleBuilder<P: ParticleOptions>(private val particleDeferredRegister: DeferredRegister<ParticleType<*>>) {

    private var id = ""
    private var particleSupplier: Supplier<ParticleType<P>>? = null

    fun id(id: String) = apply { this.id = id }

    fun bound(particleSupplier: Supplier<ParticleType<P>>) = apply { this.particleSupplier = particleSupplier }

    fun build(): DeferredHolder<ParticleType<*>, ParticleType<P>> {
        return particleDeferredRegister.register(id, particleSupplier!!)
    }

}