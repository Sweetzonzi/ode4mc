package test

import cn.solarmoon.solarmoon_core.SolarMoonCore
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.state.BlockBehaviour
import net.neoforged.neoforge.capabilities.Capabilities
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent
import net.neoforged.neoforge.client.event.EntityRenderersEvent

object Cap {
    @JvmStatic
    fun register() {}

    @JvmStatic
    fun r(event: RegisterCapabilitiesEvent) {
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, testbe.get()) { a, _ -> a.tank }
    }

    @JvmStatic
    fun r2(event: EntityRenderersEvent.RegisterRenderers) {
        event.registerBlockEntityRenderer(testbe.get(), ::br)
    }

    @JvmStatic
    val testi = SolarMoonCore.REGISTER.item<BlockItem>()
        .id("test")
        .bound{ BlockItem(test.get(), Item.Properties()) }
        .build()

    @JvmStatic
    val test = SolarMoonCore.REGISTER.block<b>()
        .id("test")
        .bound{ b(BlockBehaviour.Properties.of()) }
        .build()

    @JvmStatic
    val testbe = SolarMoonCore.REGISTER.blockentity<be>()
        .id("testbe")
        .bound(::be)
        .validBlocks { arrayOf(test.get()) }
        .capability(Capabilities.FluidHandler.BLOCK) { a, _ -> a.tank }
        .build()

}