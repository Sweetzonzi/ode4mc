package cn.solarmoon.spark_core.mixin.event;

import cn.solarmoon.spark_core.api.event.PlayerGetAttackStrengthEvent;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerMixin {

    private Player player = (Player) (Object) this;

    @Inject(method = "getAttackStrengthScale", at = @At("RETURN"), cancellable = true)
    private void getAttackStrengthScale(float adjustTicks, CallbackInfoReturnable<Float> cir) {
        var origin = cir.getReturnValue();
        var event = new PlayerGetAttackStrengthEvent(player, adjustTicks, origin);
        NeoForge.EVENT_BUS.post(event);
        cir.setReturnValue(event.getAttackStrengthScale());
    }

}
