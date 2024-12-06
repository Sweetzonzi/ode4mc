package cn.solarmoon.spark_core.api.kotlinImpl;

import cn.solarmoon.spirit_of_fight.feature.fight_skill.IFightSkillHolder;
import cn.solarmoon.spirit_of_fight.feature.fight_skill.controller.FightSkillController;
import org.jetbrains.annotations.Nullable;

public interface IFightSkillHolderJava extends IFightSkillHolder {

    @Nullable
    default FightSkillController getSkillController() {
        return getAllSkills().stream().filter(FightSkillController::isAvailable).findFirst().orElse(null);
    }

}
