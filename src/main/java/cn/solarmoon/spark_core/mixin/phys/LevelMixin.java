package cn.solarmoon.spark_core.mixin.phys;

import cn.solarmoon.spark_core.api.phys.IPhysWorldHolder;
import cn.solarmoon.spark_core.api.phys.PhysWorld;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Level.class)
public class LevelMixin implements IPhysWorldHolder {

    private final PhysWorld physWorld = new PhysWorld(50);

    @Override
    public @NotNull PhysWorld getPhysWorld() {
        return physWorld;
    }

}
