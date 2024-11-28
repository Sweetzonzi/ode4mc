package cn.solarmoon.spark_core.api.entity.skill

interface IAnimSkillHolder<T: AnimSkillController> {

    /**
     * 找到第一个可用的技能管理器
     */
    val skillController get() = getAllSkills().find { it.isAvailable }

    /**
     * 相当于注册技能，填入该生物有的所有技能
     */
    fun getAllSkills(): List<T>

}