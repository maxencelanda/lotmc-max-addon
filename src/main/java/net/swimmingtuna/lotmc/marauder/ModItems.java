package net.swimmingtuna.lotmc.marauder;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.swimmingtuna.lotmc.marauder.item.ResourceAppraisalAbility;
import net.swimmingtuna.lotmc.marauder.item.TheftAbility;
import net.swimmingtuna.lotmc.marauder.item.ThoughtMisdirectionAbility;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, LotMCMarauder.MOD_ID);

    public static final RegistryObject<Item> RESOURCE_APPRAISAL = ITEMS.register("resource_appraisal",
            () -> new ResourceAppraisalAbility(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> THEFT = ITEMS.register("theft",
            () -> new TheftAbility(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> THOUGHT_MISDIRECTION = ITEMS.register("thought_misdirection",
            () -> new ThoughtMisdirectionAbility(new Item.Properties().stacksTo(1)));

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
