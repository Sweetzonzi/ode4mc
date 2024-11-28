package cn.solarmoon.spark_core.mixin.animation;

import cn.solarmoon.spark_core.api.animation.IEntityAnimatable;
import cn.solarmoon.spirit_of_fight.feature.fight_skill.IFightSkillHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    public LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow protected abstract float tickHeadTurn(float yRot, float animStep);

    private LivingEntity entity = (LivingEntity) (Object) this;

    @Inject(method = "tick", at = @At("TAIL"))
    private void tick(CallbackInfo ci) {
        if (entity instanceof IEntityAnimatable<?> animatable) {
            // 播放指定动画时将身体转到目视方向
            if (animatable.getTurnBodyAnims().stream().anyMatch(i -> animatable.getAnimController().isPlaying(i, in -> true))) {
                tickHeadTurn(getYRot(), 100);
            }
        }
    }

    @Redirect(method = "pushEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getBoundingBox()Lnet/minecraft/world/phys/AABB;", ordinal = 0))
    private AABB push(LivingEntity instance) {
        var box = instance.getBoundingBox();
        if (instance instanceof IFightSkillHolder fighter) {
            if (fighter.getSkillController() != null) {
                return box.inflate(0.5);
            }
        }
        return box;
    }

}