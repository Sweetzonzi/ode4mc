package cn.solarmoon.spark_core.api.util

import com.google.gson.JsonParser
import java.text.DecimalFormat
import java.util.*

object TextUtil {

    @JvmStatic
    fun toRoman(num: Int): String {
        val m = arrayOf("", "M", "MM", "MMM")
        val c = arrayOf("", "C", "CC", "CCC", "CD", "D", "DC", "DCC", "DCCC", "CM")
        val x = arrayOf("", "X", "XX", "XXX", "XL", "L", "LX", "LXX", "LXXX", "XC")
        val i = arrayOf("", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX")

        val thousands = m[num / 1000]
        val hundreds = c[num % 1000 / 100]
        val tens = x[num % 100 / 10]
        val ones = i[num % 10]

        return thousands + hundreds + tens + ones
    }

    /**
     * @return 如果有小数就保留两位小数，没有就不写
     */
    @JvmStatic
    fun decimalRetentionOrNot(f: Float): String {
        val df = DecimalFormat("0.##")
        return df.format(f.toDouble())
    }

    /**
     * 提取tag中的词条（其实未必是tag中的词条）<br></br>
     * 如果 tag 是 {"name": "John"}，extractTag 是 "name"，那么这段代码将返回 "John"
     * @param tag 要提取的tag
     * @param extractTag 指定一个词条
     * @return 返回词条对应的条目
     */
    @JvmStatic
    fun extractTag(tag: String?, extractTag: String?): String {
        val jsonObject = JsonParser.parseString(tag).asJsonObject
        return jsonObject[extractTag].asString
    }

    /**
     * 以分隔符为界限提取后面的内容<br></br>
     * 如："minecraft:dirt", 分隔符输入":"，则能输出dirt
     * @param string 完整词条
     * @param separator 分隔符
     * @return 分隔符之后的内容
     */
    @JvmStatic
    fun extractString(string: String, separator: String): String {
        val parts = string.split(separator.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return if (parts.size > 1) {
            parts[1]
        } else parts[0]
    }

    /**
     * @param idLike 形似minecraft:air的字符串
     * @return 以冒号为界分割字符串并转为标题形式，比如Minecraft Air。
     */
    @JvmStatic
    fun splitFromColon(idLike: String): String {
        val parts = idLike.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return parts[0].substring(0, 1)
            .uppercase(Locale.getDefault()) + parts[0].substring(1) + " " + parts[1].substring(0, 1)
            .uppercase(Locale.getDefault()) + parts[1].substring(1)
    }

}