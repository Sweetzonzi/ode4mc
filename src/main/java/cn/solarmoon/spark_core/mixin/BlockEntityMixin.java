package cn.solarmoon.spark_core.mixin;

import cn.solarmoon.spark_core.api.recipe.processor.IRecipeProcessorProvider;
import cn.solarmoon.spark_core.api.recipe.processor.RecipeProcessor;
import cn.solarmoon.spark_core.api.recipe.processor.RecipeProcessorHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import net.neoforged.neoforge.attachment.AttachmentType;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

@Mixin(BlockEntity.class)
public abstract class BlockEntityMixin extends AttachmentHolder implements IRecipeProcessorProvider {

    @Shadow @Nullable public abstract <T> T setData(AttachmentType<T> type, T data);

    private BlockEntity be = (BlockEntity) (Object) this;
    private final Map<RecipeType<?>, RecipeProcessor<?, ?>> recipeProcessorMap = new HashMap<>();

    @NotNull
    @Override
    public Map<RecipeType<?>, RecipeProcessor<?, ?>> getRecipeProcessors() {
        return recipeProcessorMap;
    }

    @Inject(method = "saveAdditional", at = @At("HEAD"))
    public void save(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        RecipeProcessorHelper.getMap(be).forEach((n, p) -> p.saveAll(tag, registries));
    }

    @Inject(method = "loadAdditional", at = @At("HEAD"))
    public void load(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        RecipeProcessorHelper.getMap(be).forEach((n, p) -> p.loadAll(tag, registries));
    }

}
