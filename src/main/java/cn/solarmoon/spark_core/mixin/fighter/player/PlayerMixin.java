package cn.solarmoon.spark_core.mixin.fighter.player;

import cn.solarmoon.spark_core.api.animation.anim.play.AnimController;
import cn.solarmoon.spark_core.api.animation.IAnimatable;
import cn.solarmoon.spark_core.api.animation.vanilla.PlayerAnimHelper;
import cn.solarmoon.spark_core.api.entity.ai.attack.AttackHelper;
import cn.solarmoon.spark_core.api.kotlinImpl.IEntityAnimatableJava;
import cn.solarmoon.spark_core.api.phys.collision.FreeCollisionBox;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.util.List;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity implements IEntityAnimatableJava<Player> {

    private Player player = (Player) (Object) this;
    private final AnimController<IAnimatable<Player>> animController = new AnimController<>(PlayerAnimHelper.getAnimatable(player));
    private FreeCollisionBox boxCache = null;

    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public @NotNull List<@NotNull String> getTurnBodyAnims() {
        return List.of("attack");
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tick(CallbackInfo ci) {
        var level = level();
        if (!level.isClientSide) {
            var box = createCollisionBoxBoundToBone("rightItem", new Vector3f(0.5f, 0.5f, 1f), new Vector3f(0f, 0f, -0.5f));
            AttackHelper.boxAttack(player, 1f, damageSources().mobAttack(player), box, boxCache, true, 1.0);
            box.getRenderManager("attack" + getId(), 60, Color.YELLOW).sendRenderableBoxToClient();
        }
    }

    @Override
    public Player getAnimatable() {
        return player;
    }

    @Override
    public AnimController<IAnimatable<Player>> getAnimController() {
        return animController;
    }

}
