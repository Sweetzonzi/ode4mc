package cn.solarmoon.spirit_of_fight.data

import cn.solarmoon.spark_core.data.SkillTagProvider
import cn.solarmoon.spirit_of_fight.SpiritOfFight
import cn.solarmoon.spirit_of_fight.registry.common.SOFSkills
import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.minecraft.resources.ResourceLocation
import net.neoforged.neoforge.common.data.ExistingFileHelper
import java.util.concurrent.CompletableFuture

class SOFSkillTags(
    output: PackOutput,
    lookupProvider: CompletableFuture<HolderLookup.Provider>,
    existingFileHelper: ExistingFileHelper
): SkillTagProvider(output, lookupProvider, SpiritOfFight.MOD_ID, existingFileHelper) {

    companion object {
        @JvmStatic
        val FORGE_ATTACK = forgeTag("attack")

        private fun modTag(path: String) = createTag(ResourceLocation.fromNamespaceAndPath(SpiritOfFight.MOD_ID, path))

        private fun forgeTag(path: String) = createTag(ResourceLocation.fromNamespaceAndPath("c", path))
    }

    override fun addTags(p0: HolderLookup.Provider) {
        tag(FORGE_ATTACK).add(
            SOFSkills.SWORD_COMBO_0.get(),
            SOFSkills.SWORD_COMBO_1.get(),
            SOFSkills.SWORD_COMBO_2.get()
        ).replace(false)
    }

}