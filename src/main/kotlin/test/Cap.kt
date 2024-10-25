package test

import cn.solarmoon.spark_core.SparkCore
import cn.solarmoon.spark_core.api.fluid.WaterLikeFluidType
import cn.solarmoon.spark_core.api.network.CommonNetRegister
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.state.BlockBehaviour
import net.neoforged.fml.loading.FMLEnvironment
import net.neoforged.neoforge.capabilities.Capabilities
import net.neoforged.neoforge.client.event.EntityRenderersEvent
import net.neoforged.neoforge.common.NeoForge
import test.player.Attack
import test.player.AttackNet

object Cap {

    @JvmStatic
    fun register() {
        if (FMLEnvironment.dist.isClient) NeoForge.EVENT_BUS.register(Attack())
        CommonNetRegister.register(Pair(AttackNet.Else(), AttackNet()))
    }

    @JvmStatic
    fun r2(event: EntityRenderersEvent.RegisterRenderers) {
        event.registerBlockEntityRenderer(testbe.get(), ::br)
    }

    @JvmStatic
    val testi = SparkCore.REGISTER.item<BlockItem>()
        .id("test")
        .bound{ BlockItem(test.get(), Item.Properties()) }
        .build()

    @JvmStatic
    val test = SparkCore.REGISTER.block<b>()
        .id("test")
        .bound{ b(BlockBehaviour.Properties.of()) }
        .build()

    @JvmStatic
    val testbe = SparkCore.REGISTER.blockentity<be>()
        .id("testbe")
        .bound(::be)
        .validBlocks { arrayOf(test.get()) }
        .capability(Capabilities.FluidHandler.BLOCK) { a, _ -> a.tank }
        .build()

    val f = SparkCore.REGISTER.fluid()
        .id("ft")
        .waterLike(true, WaterLikeFluidType.waterLikeProperties(false))
        .simple({ sp() }, true)
        .build()

}