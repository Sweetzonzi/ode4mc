package cn.solarmoon.spark_core.mixin.fighter.vindicator;

import cn.solarmoon.spark_core.api.animation.IEntityAnimatable;
import cn.solarmoon.spark_core.api.animation.anim.InterpolationType;
import cn.solarmoon.spark_core.api.phys.collision.FreeCollisionBox;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.monster.Vindicator;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.util.UUID;

@Mixin(MeleeAttackGoal.class)
public abstract class MeleeAttackGoalMixin {

    @Shadow @Final protected PathfinderMob mob;

    @Shadow protected abstract boolean canPerformAttack(LivingEntity entity);

    private FreeCollisionBox cache = null;

    @Inject(method = "stop", at = @At("HEAD"), cancellable = true)
    private void attack(CallbackInfo ci) {
        LivingEntity livingentity = this.mob.getTarget();
        if (!EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(livingentity)) {
            this.mob.setTarget(null);
        }

        this.mob.setAggressive(true);
        this.mob.getNavigation().stop();
        ci.cancel();
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void tick(CallbackInfo ci) {

    }

}
