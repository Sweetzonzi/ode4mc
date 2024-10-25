package cn.solarmoon.spark_core.mixin.fighter.vindicator;

import cn.solarmoon.spark_core.api.animation.anim.play.AnimController;
import cn.solarmoon.spark_core.api.animation.IAnimatable;
import cn.solarmoon.spark_core.api.animation.anim.InterpolationType;
import cn.solarmoon.spark_core.api.animation.anim.part.Animation;
import cn.solarmoon.spark_core.api.entity.EntityStateHelper;
import cn.solarmoon.spark_core.api.kotlinImpl.IEntityAnimatableJava;
import kotlin.jvm.functions.Function0;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

import java.util.LinkedHashMap;
import java.util.List;

@Mixin(Vindicator.class)
public abstract class VindicatorMixin extends AbstractIllager implements IEntityAnimatableJava<Vindicator> {

    private Vindicator vindicator = (Vindicator) (Object) this;
    private final AnimController<IAnimatable<Vindicator>> animController = new AnimController<>((IAnimatable<Vindicator>) vindicator);

    protected VindicatorMixin(EntityType<? extends AbstractIllager> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public @NotNull List<@NotNull String> getTurnBodyAnims() {
        return List.of("attack_swipe");
    }

    @Override
    public @NotNull Vindicator getAnimatable() {
        return vindicator;
    }

    @Override
    public @NotNull AnimController<@NotNull IAnimatable<@NotNull Vindicator>> getAnimController() {
        return animController;
    }

}
