package net.swimmingtuna.lotmc.marauder.events;

import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.swimmingtuna.lotm.capabilities.scribed_abilities.ScribedUtils;
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.caps.BeyonderHolderAttacher;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.util.BeyonderAbilitiesItemMenu;
import net.swimmingtuna.lotmc.marauder.ModItems;
import net.swimmingtuna.lotmc.marauder.attributes.marauder.MarauderAttributes;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * If you are the original LOTMC developer reading this and want to natively integrate
 * this ability into the Marauder pathway like every other pathway ability, do this:
 *
 * 1. Add the item registry entry in your ItemInit class:
 *    public static final RegistryObject<Item> RESOURCE_APPRAISAL = ITEMS.register("resource_appraisal",
 *        () -> new ResourceAppraisalAbility(new Item.Properties().stacksTo(1)));
 *
 * 2. Add the item to MarauderClass.getItems() like all other pathway abilities:
 *    @Override
 *    public Multimap<Integer, Item> getItems(LivingEntity living) {
 *        HashMultimap<Integer, Item> items = HashMultimap.create();
 *        items.put(9, ItemInit.ALLY_MAKER.get());
 *        items.put(9, ItemInit.RESOURCE_APPRAISAL.get());  // <-- add this line
 *        return items;
 *    }
 *
 * 3. Add the language entry in en_us.json / fr_fr.json.
 *
 * That's it. The ability will then appear automatically in /abilities for any
 * sequence 9+ Marauder, with proper sorting by descriptionId, just like every
 * other pathway ability. No mixins, no reflection, no container interception needed.
 */
public class ModEvents {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Set<UUID> marauderPlayers = new HashSet<>();

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player.level().isClientSide) return;

        Player player = event.player;
        BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);

        if (holder != null && holder.getCurrentClass() != null) {
            boolean isMarauder = holder.currentClassMatches(BeyonderClassInit.MARAUDER);
            UUID uuid = player.getUUID();

            if (isMarauder) {
                marauderPlayers.add(uuid);
                MarauderAttributes.applyAll(player, holder.getSequence());
            } else if (marauderPlayers.remove(uuid)) {
                MarauderAttributes.cleanAll(player);
            }
        }

        handlePrometheusTimers(player);
    }

    private static void handlePrometheusTimers(Player player) {
        CompoundTag tag = player.getPersistentData();

        int borrowedTimer = tag.getInt("prometheusBorrowedTimer");
        if (borrowedTimer > 0) {
            borrowedTimer--;
            tag.putInt("prometheusBorrowedTimer", borrowedTimer);
            if (borrowedTimer <= 0) {
                String borrowedAbility = tag.getString("prometheusBorrowedAbility");
                tag.remove("prometheusBorrowedAbility");
                tag.remove("prometheusBorrowedTimer");
                tag.remove("prometheusBorrowedFrom");
                if (!borrowedAbility.isEmpty()) {
                    Item abilityItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(borrowedAbility));
                    if (abilityItem != null) {
                        ScribedUtils.removeAbility(player, abilityItem);
                        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                            ItemStack stack = player.getInventory().getItem(i);
                            if (stack.is(abilityItem)) {
                                stack.shrink(1);
                                break;
                            }
                        }
                    }
                }
                player.sendSystemMessage(Component.literal("Your borrowed ability has expired."));
            }
        }

        int stolenTimer = tag.getInt("prometheusStolenTimer");
        if (stolenTimer > 0) {
            stolenTimer--;
            tag.putInt("prometheusStolenTimer", stolenTimer);
            if (stolenTimer <= 0) {
                String stolenAbility = tag.getString("prometheusStolenAbility");
                tag.remove("prometheusStolenAbility");
                tag.remove("prometheusStolenTimer");
                tag.remove("prometheusStolenBy");
                if (!stolenAbility.isEmpty()) {
                    Item abilityItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(stolenAbility));
                    if (abilityItem != null) {
                        ItemStack returnedStack = new ItemStack(abilityItem);
                        if (!player.getInventory().add(returnedStack)) {
                            ItemEntity entity = new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), returnedStack);
                            entity.setPickUpDelay(0);
                            player.level().addFreshEntity(entity);
                        }
                    }
                }
                player.sendSystemMessage(Component.literal("Your stolen ability has returned!"));
            }
        }
    }

    /**
     * Workaround: since LOTMC's pathway classes hardcode their items in getItems()
     * with no extension API, we inject our RESOURCE_APPRAISAL into the abilities
     * container AFTER it has been built, by intercepting the menu open event.
     *
     * If LOTMC ever adds an event or registry for addon items, this can be removed.
     */
    @SubscribeEvent
    public static void onContainerOpen(PlayerContainerEvent.Open event) {
        if (!(event.getContainer() instanceof BeyonderAbilitiesItemMenu menu)) return;

        Player player = (Player) event.getEntity();
        BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
        if (holder == null || !holder.currentClassMatches(BeyonderClassInit.MARAUDER)) return;

        try {
            Field containerField = BeyonderAbilitiesItemMenu.class.getDeclaredField("container");
            containerField.setAccessible(true);
            Container container = (Container) containerField.get(menu);

            ItemStack appraisalStack = new ItemStack(ModItems.RESOURCE_APPRAISAL.get());
            ItemStack theftStack = new ItemStack(ModItems.THEFT.get());
            ItemStack thoughtStack = new ItemStack(ModItems.THOUGHT_MISDIRECTION.get());
            ItemStack mentalStack = new ItemStack(ModItems.MENTAL_DISRUPTION.get());
            ItemStack decryptionStack = new ItemStack(ModItems.DECRYPTION.get());
            ItemStack prometheusStack = new ItemStack(ModItems.PROMETHEUS_THEFT.get());

            for (ItemStack abilityItem : new ItemStack[]{appraisalStack, theftStack, thoughtStack, mentalStack, decryptionStack, prometheusStack}) {
                if (abilityItem.isEmpty()) continue;
                boolean alreadyPresent = false;
                for (int i = 0; i < container.getContainerSize(); i++) {
                    if (ItemStack.isSameItem(container.getItem(i), abilityItem)) {
                        alreadyPresent = true;
                        break;
                    }
                }
                if (alreadyPresent) continue;

                for (int i = 0; i < container.getContainerSize(); i++) {
                    if (container.getItem(i).isEmpty()) {
                        container.setItem(i, abilityItem);
                        break;
                    }
                }
            }

            if (player instanceof ServerPlayer) {
                menu.broadcastChanges();
            }

            LOGGER.info("Injected abilities into /abilities for player {}", player.getName().getString());
        } catch (Exception e) {
            LOGGER.error("Failed to inject RESOURCE_APPRAISAL into abilities menu", e);
        }
    }
}
