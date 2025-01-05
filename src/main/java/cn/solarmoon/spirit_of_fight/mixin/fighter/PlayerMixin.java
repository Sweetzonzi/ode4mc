package cn.solarmoon.spirit_of_fight.mixin.fighter;

import cn.solarmoon.spirit_of_fight.fighter.player.IPlayerPatchHolder;
import cn.solarmoon.spirit_of_fight.fighter.player.PlayerPatch;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Player.class)
public class PlayerMixin implements IPlayerPatchHolder {

    private final Player player = (Player) (Object) this;
    private final PlayerPatch patch = new PlayerPatch(player);

    @Override
    public PlayerPatch getPatch() {
        return patch;
    }

}
