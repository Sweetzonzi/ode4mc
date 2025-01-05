package cn.solarmoon.spirit_of_fight.mixin.fighter;

import cn.solarmoon.spirit_of_fight.fighter.EntityPatch;
import cn.solarmoon.spirit_of_fight.fighter.IEntityPatchHolder;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Entity.class)
public class EntityMixin implements IEntityPatchHolder {

    private final Entity entity = (Entity) (Object) this;
    private final EntityPatch patch = new EntityPatch(entity);

    @Override
    public EntityPatch getEntityPatch() {
        return patch;
    }

}
