package cn.solarmoon.spirit_of_fight;

import cn.solarmoon.spark_core.entry_builder.ObjectRegister;
import cn.solarmoon.spirit_of_fight.registry.client.SOFClientEvents;
import cn.solarmoon.spirit_of_fight.registry.client.SOFGuis;
import cn.solarmoon.spirit_of_fight.registry.client.SOFKeyMappings;
import cn.solarmoon.spirit_of_fight.registry.client.SOFLocalControllerRegister;
import cn.solarmoon.spirit_of_fight.registry.common.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(SpiritOfFight.MOD_ID)
public class SpiritOfFight {

    public static final String MOD_ID = "spirit_of_fight";
    public static final Logger LOGGER = LoggerFactory.getLogger("战魂");
    public static final ObjectRegister REGISTER = new ObjectRegister(MOD_ID, true);

    public SpiritOfFight(IEventBus modEventBus, ModContainer modContainer) {
        REGISTER.register(modEventBus);

        if (FMLEnvironment.dist.isClient()) {
            SOFClientEvents.register();
            SOFKeyMappings.register();
            SOFGuis.register(modEventBus);
            SOFLocalControllerRegister.register(modEventBus);
        }

        SOFAttachments.register();
        SOFCommonEvents.register();
        SOFVisualEffects.register();
        SOFAnimRegister.register();
        SOFSkills.register();
        SOFDataGenerater.register(modEventBus);
        SOFNetworks.register(modEventBus);
    }


}
