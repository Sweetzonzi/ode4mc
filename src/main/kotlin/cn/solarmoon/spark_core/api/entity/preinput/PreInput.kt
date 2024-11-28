package cn.solarmoon.spark_core.api.entity.preinput

/**
 * ### 通过Attachment装载的预输入，使用[getPreInput]来获取并修改
 * - 预输入通过id进行标识，每次设置预输入都会覆盖上一个预输入内容
 * - 预输入在生物tick中进行计时，默认存在半秒后会自动清除留存的指令
 * - 预输入不会在游戏中保存，游戏重启后会丢失
 */
class PreInput {

    var id = ""
        private set
    private var hasInput = false
    private var input = {}
    private var maxRemainTime = 10
    private var remain = 0

    fun hasInput(id: String = ""): Boolean {
        return if (id.isEmpty()) hasInput
        else hasInput && this.id == id
    }

    fun setInput(id: String = "", maxRemainTime: Int = 10, input: () -> Unit) {
        this.input = input
        this.id = id
        this.maxRemainTime = maxRemainTime
        hasInput = true
        remain = 0
    }

    fun invokeInput() {
        input.invoke()
        clear()
    }

    fun clear() {
        id = ""
        input = {}
        hasInput = false
        remain = 0
    }

    /**
     * 保证预输入只能存留半秒
     */
    fun tick() {
        if (hasInput && remain < maxRemainTime) remain++
        else {
            clear()
        }
    }

}
