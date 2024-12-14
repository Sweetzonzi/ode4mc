package cn.solarmoon.spark_core.mixin.phys;

import cn.solarmoon.spark_core.api.phys.thread.IActionConsumer;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

@Mixin(Level.class)
public class LevelMixin implements IActionConsumer {

    private final ConcurrentLinkedQueue<@NotNull Function0<@NotNull Unit>> actions = new ConcurrentLinkedQueue<>();

    @Override
    @NotNull
    public ConcurrentLinkedQueue<@NotNull Function0<@NotNull Unit>> getActions() {
        return actions;
    }
}
