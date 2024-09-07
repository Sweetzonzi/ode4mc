package cn.solarmoon.spark_core.feature.use

import cn.solarmoon.spark_core.api.recipe.ChanceResult
import cn.solarmoon.spark_core.api.util.BlockUtil
import cn.solarmoon.spark_core.api.util.DropUtil
import cn.solarmoon.spark_core.api.util.DropUtil.addItemToInventory
import cn.solarmoon.spark_core.registry.common.SparkRecipes
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent


class UseImpl {

    @SubscribeEvent
    fun whenUse(event: PlayerInteractEvent.RightClickBlock) {
        val player = event.entity
        val heldItem = event.itemStack
        val pos = event.pos
        val level = event.level
        val state = level.getBlockState(pos)
        val block = state.block
        val hand = event.hand

        val recipes = level.recipeManager.getAllRecipesFor(SparkRecipes.USE.type.get())
        val recipeOptional = recipes.stream()
            .filter { block == it.value.inputBlock && it.value.ingredient.test(heldItem) }
            .map { it.value }
            .findFirst()
        if (recipeOptional.isPresent) {
            val recipe: UseRecipe = recipeOptional.get()
            if (block !== recipe.outputBlock) BlockUtil.replaceBlockWithAllState(state, recipe.outputBlock.defaultBlockState(), level, pos)
            if (!player.isCreative) {
                addItemToInventory(player, heldItem.craftingRemainingItem)
                heldItem.shrink(1)
            }
            ChanceResult.getRolledResults(player, recipe.chanceResults).forEach { c -> DropUtil.summonDrop(c, level, pos) }
            val randomSource = player.random
            for (i in 0 until randomSource.nextInt(2, 5)) {
                level.addParticle(
                    ParticleTypes.END_ROD,
                    pos.x + randomSource.nextDouble(), pos.y.toDouble(), pos.z + randomSource.nextDouble(),
                    0.0, 0.1, 0.0
                )
            }
            level.playSound(null, pos, SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.BLOCKS, 1f, 2f)
            player.swing(hand)
            event.isCanceled = true
        }
    }

}