package net.swimmingtuna.lotmc.marauder;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, LotMCMarauder.MOD_ID);

    public static final RegistryObject<CreativeModeTab> MARAUDER_TAB = CREATIVE_TABS.register("marauder_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.RESOURCE_APPRAISAL.get()))
                    .title(Component.translatable("creativetab.lotmc_marauder"))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.RESOURCE_APPRAISAL.get());
                        output.accept(ModItems.THEFT.get());
                        output.accept(ModItems.THOUGHT_MISDIRECTION.get());
                    })
                    .build());

    public static void register(IEventBus bus) {
        CREATIVE_TABS.register(bus);
    }
}
