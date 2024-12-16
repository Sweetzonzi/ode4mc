package cn.solarmoon.spark_core;

import cn.solarmoon.spark_core.api.entry_builder.ObjectRegister;
import cn.solarmoon.spark_core.api.phys.DxHelper;
import cn.solarmoon.spark_core.registry.client.SparkClientEvents;
import cn.solarmoon.spark_core.registry.common.*;
import cn.solarmoon.spirit_of_fight.registry.client.SOFClientEvents;
import cn.solarmoon.spirit_of_fight.registry.client.SOFGuis;
import cn.solarmoon.spirit_of_fight.registry.client.SOFKeyMappings;
import cn.solarmoon.spirit_of_fight.registry.common.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import org.ode4j.ode.OdeHelper;
import org.ode4j.ode.internal.OdeInit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(SparkCore.MOD_ID)
public class SparkCore {

    public static final String MOD_ID = "spark_core";
    public static final Logger LOGGER = LoggerFactory.getLogger("星火核心");
    public static final ObjectRegister REGISTER = new ObjectRegister(MOD_ID, true);

    public SparkCore(IEventBus modEventBus, ModContainer modContainer) {
        REGISTER.register(modEventBus);

        if (FMLEnvironment.dist.isClient()) {
            SparkClientEvents.register();
            SOFClientEvents.register();
            SOFKeyMappings.register();
        }

        SparkVisualEffects.register();
        SparkAttachments.register();
        SparkCommonEvents.register();
        SparkPayloads.register(modEventBus);
        SparkDatas.register();
        SparkAnimRegister.register();

        SOFNetworks.register(modEventBus);
        SOFGuis.register(modEventBus);
        SOFCommonEvents.register();
        SOFAttachments.register();
        SOFVisualEffects.register();
        SOFAnimRegister.register();

        DxHelper.initOde();
    }

}
