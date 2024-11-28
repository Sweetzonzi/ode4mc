package cn.solarmoon.spark_core.api.entry_builder

import cn.solarmoon.spark_core.api.entry_builder.client.KeyMappingBuilder
import cn.solarmoon.spark_core.api.entry_builder.client.LayerBuilder
import cn.solarmoon.spark_core.api.entry_builder.common.*
import cn.solarmoon.spark_core.api.entry_builder.common.fluid.FluidBuilder
import cn.solarmoon.spark_core.api.util.RegisterUtil
import net.minecraft.core.RegistrySetBuilder
import net.minecraft.core.particles.ParticleType
import net.minecraft.core.registries.Registries
import net.minecraft.network.syncher.EntityDataSerializer
import net.minecraft.sounds.SoundEvent
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.ai.attributes.Attribute
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration
import net.minecraft.world.level.material.Fluid
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.attachment.AttachmentType
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider
import net.neoforged.neoforge.data.event.GatherDataEvent
import net.neoforged.neoforge.fluids.FluidType
import net.neoforged.neoforge.registries.DeferredRegister
import net.neoforged.neoforge.registries.NeoForgeRegistries


class ObjectRegister(val modId: String, val gatherData: Boolean = true) {

    var modBus: IEventBus? = null

    val itemDeferredRegister: DeferredRegister<Item> = DeferredRegister.create(Registries.ITEM, modId)
    val blockDeferredRegister: DeferredRegister<Block> = DeferredRegister.create(Registries.BLOCK, modId)
    val blockEntityDeferredRegister: DeferredRegister<BlockEntityType<*>> = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, modId)
    val fluidDeferredRegister: DeferredRegister<Fluid> = DeferredRegister.create(Registries.FLUID, modId)
    val fluidTypeDeferredRegister: DeferredRegister<FluidType> = DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, modId)
    val attachmentDeferredRegister: DeferredRegister<AttachmentType<*>> = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, modId)
    val featureDeferredRegister: DeferredRegister<Feature<*>> = DeferredRegister.create(Registries.FEATURE, modId)
    val attributeDeferredRegister: DeferredRegister<Attribute> = DeferredRegister.create(Registries.ATTRIBUTE, modId)
    val creativeTabDeferredRegister: DeferredRegister<CreativeModeTab> = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, modId)
    val effectDeferredRegister: DeferredRegister<MobEffect> = DeferredRegister.create(Registries.MOB_EFFECT, modId)
    val entityDeferredRegister: DeferredRegister<EntityType<*>> = DeferredRegister.create(Registries.ENTITY_TYPE, modId)
    val particleDeferredRegister: DeferredRegister<ParticleType<*>> = DeferredRegister.create(Registries.PARTICLE_TYPE, modId)
    val recipeDeferredRegister: DeferredRegister<RecipeType<*>> = DeferredRegister.create(Registries.RECIPE_TYPE, modId)
    val recipeSerializerDeferredRegister: DeferredRegister<RecipeSerializer<*>> = DeferredRegister.create(Registries.RECIPE_SERIALIZER, modId)
    val soundDeferredRegister: DeferredRegister<SoundEvent> = DeferredRegister.create(Registries.SOUND_EVENT, modId)
    val dataComponentDeferredRegister: DeferredRegister.DataComponents = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, modId)
    val entityDataDeferredRegister: DeferredRegister<EntityDataSerializer<*>> = DeferredRegister.create(NeoForgeRegistries.ENTITY_DATA_SERIALIZERS, modId)

    fun register(bus: IEventBus) {
        modBus = bus
        itemDeferredRegister.register(bus)
        blockDeferredRegister.register(bus)
        blockEntityDeferredRegister.register(bus)
        fluidDeferredRegister.register(bus)
        fluidTypeDeferredRegister.register(bus)
        attachmentDeferredRegister.register(bus)
        featureDeferredRegister.register(bus)
        attributeDeferredRegister.register(bus)
        creativeTabDeferredRegister.register(bus)
        effectDeferredRegister.register(bus)
        entityDeferredRegister.register(bus)
        particleDeferredRegister.register(bus)
        recipeDeferredRegister.register(bus)
        recipeSerializerDeferredRegister.register(bus)
        soundDeferredRegister.register(bus)
        dataComponentDeferredRegister.register(bus)
        entityDataDeferredRegister.register(bus)
        modBus!!.takeIf { gatherData }?.addListener(this::gather)
    }

    private fun gather(event: GatherDataEvent) {
        val generator = event.generator
        val output = generator.packOutput
        val lookupProvider = event.lookupProvider
        val builder = RegistrySetBuilder()
        RegisterUtil.gatherBuilderFix(builder)
        builder.add(Registries.PLACED_FEATURE, FeatureBuilder.Gather::placedBootStrap)
        generator.addProvider(
            event.includeServer(),
            DatapackBuiltinEntriesProvider(output, lookupProvider, builder, mutableSetOf(modId))
        )
    }

    fun <I: Item> item() = ItemBuilder<I>(itemDeferredRegister, modBus!!)
    fun <B: Block> block() = BlockBuilder<B>(blockDeferredRegister)
    fun <B: BlockEntity> blockentity() = BlockEntityBuilder<B>(blockEntityDeferredRegister, modBus!!)
    fun <E: Entity> entity() = EntityBuilder<E>(entityDeferredRegister)
    fun fluid() = FluidBuilder(
        modId,
        fluidDeferredRegister,
        fluidTypeDeferredRegister,
        blockDeferredRegister,
        itemDeferredRegister,
        modBus!!
    )
    fun <A> attachment() = AttachmentBuilder<A>(attachmentDeferredRegister)
    fun <D> dataComponent() = DataComponentBuilder<D>(dataComponentDeferredRegister)
    fun <C: FeatureConfiguration, F: Feature<C>> feature() = FeatureBuilder<C, F>(modId, featureDeferredRegister)
    fun attribute() = AttributeBuilder(modId, attributeDeferredRegister, modBus!!)
    fun creativeTab() = CreativeTabBuilder(creativeTabDeferredRegister)
    fun damageType() = DamageTypeBuilder(modId)
    fun effect() = EffectBuilder(effectDeferredRegister)
    fun <P: ParticleType<*>> particle() = ParticleBuilder<P>(particleDeferredRegister)
    fun <R: Recipe<*>> recipe() = RecipeBuilder<R>(modId, recipeSerializerDeferredRegister, recipeDeferredRegister)
    fun sound() = SoundBuilder(modId, soundDeferredRegister)
    fun <D> entityData() = EntityDataBuilder<D>(entityDataDeferredRegister)

    fun layer() = LayerBuilder(modId, modBus!!)
    fun keyMapping() = KeyMappingBuilder(modId, modBus!!)

}