package net.swimmingtuna.lotmc.marauder;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.swimmingtuna.lotmc.marauder.events.ModEvents;
import net.swimmingtuna.lotmc.marauder.events.VillagerEvents;
import net.swimmingtuna.lotmc.marauder.networking.ModNetworking;
import org.slf4j.Logger;

@Mod(LotMCMarauder.MOD_ID)
public class LotMCMarauder {
    public static final String MOD_ID = "lotmc_marauder";
    private static final Logger LOGGER = LogUtils.getLogger();

    public LotMCMarauder() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.register(modEventBus);
        ModCreativeTab.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(ModEvents.class);
        MinecraftForge.EVENT_BUS.register(VillagerEvents.class);

        modEventBus.addListener(this::commonSetup);

        LOGGER.info("LOTMC Marauder Addon initialized!");
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(ModNetworking::register);
    }
}
