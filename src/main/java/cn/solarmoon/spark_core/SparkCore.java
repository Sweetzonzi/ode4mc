package cn.solarmoon.spark_core;

import cn.solarmoon.spark_core.api.entry_builder.ObjectRegister;
import cn.solarmoon.spark_core.api.kit.Translator;
import cn.solarmoon.spark_core.data.DataGenerater;
import cn.solarmoon.spark_core.registry.client.SparkTooltips;
import cn.solarmoon.spark_core.registry.common.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.Cap;
import test.cep;
import test.ees;

@Mod(SparkCore.MOD_ID)
public class SparkCore {

    public static final String MOD_ID = "spark_core";
    public static final Logger LOGGER = LoggerFactory.getLogger("星火核心");
    public static final Translator TRANSLATOR = new Translator(MOD_ID);
    public static final ObjectRegister REGISTER = new ObjectRegister(MOD_ID, true);

    public SparkCore(IEventBus modEventBus, ModContainer modContainer) {
        REGISTER.register(modEventBus);

        if (FMLEnvironment.dist.isClient()) {
            SparkTooltips.register(modEventBus);
            cep.register(modEventBus);
        }

        SparkAttachments.register();
        SparkAttributes.register();
        SparkRecipes.register();
        SparkDamageTypes.register();
        SparkDataComponents.register();
        SparkCommonEvents.register();
        SparkCommands.register();
        SparkNetDatas.register(modEventBus);
        SparkDatas.register();
        SparkEntityDatas.register();
        ees.register(modEventBus);

        DataGenerater.register(modEventBus);

        Cap.register();
        modEventBus.addListener(Cap::r2);
    }

}
