package cn.solarmoon.spark_core.mixin.fighter.player;

import cn.solarmoon.spark_core.api.animation.IEntityAnimatable;
import cn.solarmoon.spark_core.api.animation.anim.play.AnimController;
import cn.solarmoon.spark_core.api.animation.IAnimatable;
import cn.solarmoon.spark_core.api.animation.anim.template.EntityStateAnim;
import cn.solarmoon.spark_core.api.animation.vanilla.PlayerAnimHelper;
import cn.solarmoon.spark_core.api.event.EntityGetWeaponEvent;
import cn.solarmoon.spark_core.api.kotlinImpl.IEntityAnimatableJava;
import cn.solarmoon.spirit_of_fight.feature.fight_skill.controller.FightSkillController;
import cn.solarmoon.spark_core.api.kotlinImpl.IFightSkillHolderJava;
import cn.solarmoon.spirit_of_fight.feature.fight_skill.controller.SwordFightSkillController;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity implements IEntityAnimatableJava<Player>, IFightSkillHolderJava {

    @Shadow public abstract void aiStep();

    @Shadow public abstract float getSpeed();

    private Player player = (Player) (Object) this;
    private final AnimController<IAnimatable<Player>> animController = new AnimController<>(PlayerAnimHelper.getAnimatable(player));
    private SwordFightSkillController swordFightSkill;

    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(Level level, BlockPos pos, float yRot, GameProfile gameProfile, CallbackInfo ci) {
        swordFightSkill = new SwordFightSkillController((IEntityAnimatable<?>) player);
    }

    @Override
    public @NotNull List<@NotNull String> getTurnBodyAnims() {
        var list = new ArrayList<String>();
        getAllSkills().forEach(i -> list.addAll(i.getAllSkillAnims()));
        return list;
    }

    @Override
    public Player getAnimatable() {
        return player;
    }

    @Override
    public AnimController<IAnimatable<Player>> getAnimController() {
        return animController;
    }

    @Override
    public @NotNull List<@NotNull EntityStateAnim> getStatusAnims() {
        return EntityStateAnim.getALL_STATES();
    }

    @Override
    public @NotNull List<@NotNull FightSkillController> getAllSkills() {
        return List.of(swordFightSkill);
    }

    @Inject(method = "getWeaponItem", at = @At("RETURN"), cancellable = true)
    private void getWeapon(CallbackInfoReturnable<ItemStack> cir) {
        var origin = cir.getReturnValue();
        var event = new EntityGetWeaponEvent(player, origin);
        NeoForge.EVENT_BUS.post(event);
        cir.setReturnValue(event.getWeapon());
    }

}