package cn.solarmoon.spark_core.mixin.animation;

import cn.solarmoon.spark_core.api.animation.anim.play.AnimData;
import cn.solarmoon.spark_core.api.animation.IEntityAnimatable;
import cn.solarmoon.spark_core.api.entity.ai.attack.AttackedData;
import cn.solarmoon.spark_core.registry.common.SparkAttachments;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import net.neoforged.neoforge.attachment.AttachmentType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(Entity.class)
public abstract class EntityMixin extends AttachmentHolder {

    @Shadow public abstract Level level();

    @Shadow private Level level;

    @Shadow public abstract int getId();

    @Shadow public abstract EntityType<?> getType();

    @Shadow @Nullable public abstract <T> T setData(AttachmentType<T> type, T data);

    private Entity entity = (Entity) (Object) this;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(EntityType entityType, Level level, CallbackInfo ci) {
        if (entity instanceof IEntityAnimatable<?>) {
            setData(SparkAttachments.getANIM_DATA(), AnimData.of(entity));
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tickH(CallbackInfo ci) {
        // 删除无用受击数据
        getData(SparkAttachments.getATTACKED_DATA()).removeIf(AttackedData::getCancelled);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tickT(CallbackInfo ci) {
        // 动画
        if (entity instanceof IEntityAnimatable<?> animatable) {
            animatable.getAnimController().animTick();
        }
    }

}
