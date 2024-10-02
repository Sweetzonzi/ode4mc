package cn.solarmoon.spark_core.api.compat.jei;

import cn.solarmoon.spark_core.SparkCore;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.text.DecimalFormat;

public class JEIHelper {//

    public static Component chanceText(float chance) {
        DecimalFormat df = new DecimalFormat("0.##");
        String result = df.format(chance * 100);
        return SparkCore.TRANSLATOR.set("jei", "chance", ChatFormatting.GOLD, result);
    }

}
