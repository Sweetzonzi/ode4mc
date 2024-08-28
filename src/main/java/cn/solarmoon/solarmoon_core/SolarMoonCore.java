package cn.solarmoon.solarmoon_core;

import cn.solarmoon.solarmoon_core.api.ability.AbilityComponents;
import cn.solarmoon.solarmoon_core.api.ability.placeable.CustomPlaceableItem;
import cn.solarmoon.solarmoon_core.api.entry_builder.ObjectRegister;
import cn.solarmoon.solarmoon_core.api.kit.Translator;
import cn.solarmoon.solarmoon_core.registry.common.CommonAttachments;
import cn.solarmoon.solarmoon_core.registry.common.SolarCommonEvents;
import cn.solarmoon.solarmoon_core.registry.common.SolarNetDatas;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.Cap;

@Mod(SolarMoonCore.MOD_ID)
public class SolarMoonCore {

    public static final String MOD_ID = "solarmoon_core";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Translator TRANSLATOR = new Translator(MOD_ID);
    public static final ObjectRegister REGISTER = new ObjectRegister(MOD_ID);

    public SolarMoonCore(IEventBus modEventBus, ModContainer modContainer) {
        REGISTER.register(modEventBus);
        CommonAttachments.register();
        SolarCommonEvents.register();
        SolarNetDatas.register(modEventBus);

        Cap.register();
        modEventBus.addListener(Cap::r2);
    }

}
