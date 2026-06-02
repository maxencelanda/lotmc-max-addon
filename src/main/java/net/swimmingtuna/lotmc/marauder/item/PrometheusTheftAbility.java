package net.swimmingtuna.lotmc.marauder.item;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotmc.marauder.networking.ModNetworking;
import net.swimmingtuna.lotmc.marauder.networking.packet.OpenPrometheusTheftScreenS2C;
import net.swimmingtuna.lotm.capabilities.scribed_abilities.ScribedUtils;
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.caps.BeyonderHolderAttacher;

import java.util.List;
import java.util.stream.Collectors;

public class PrometheusTheftAbility extends SimpleAbilityItem {
    private static final int RANGE = 50;
    private static final int FAIL_COOLDOWN = 100;
    private static final int SUCCESS_COOLDOWN = 2400;
    private static final int THEFT_DURATION = 2400;

    public PrometheusTheftAbility(Item.Properties properties) {
        super(properties, () -> BeyonderClassInit.MARAUDER.get(), 6, 150, SUCCESS_COOLDOWN);
    }

    @Override
    public InteractionResult interactLivingEntityLivingEntity(ItemStack stack, LivingEntity user, LivingEntity target, InteractionHand hand) {
        if (!(user instanceof Player thief) || !(target instanceof ServerPlayer victim)) return InteractionResult.FAIL;
        if (user.level().isClientSide()) return InteractionResult.FAIL;
        if (!this.checkAll(user)) return InteractionResult.FAIL;

        if (user.distanceToSqr(target) >= RANGE * RANGE) {
            thief.displayClientMessage(
                    Component.literal("Target is too far!").withStyle(ChatFormatting.RED), false);
            return InteractionResult.FAIL;
        }

        if (hasBorrowedAbility(thief)) {
            thief.displayClientMessage(
                    Component.literal("You already have a borrowed ability! Wait for it to expire.").withStyle(ChatFormatting.RED), false);
            return InteractionResult.FAIL;
        }

        BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(victim);
        if (holder == null || holder.getCurrentClass() == null) {
            thief.displayClientMessage(
                    Component.literal("Target is not a Beyonder!").withStyle(ChatFormatting.RED), false);
            return InteractionResult.FAIL;
        }

        List<Item> abilities = BeyonderUtil.getAbilitiesInPossession(victim).stream()
                .filter(item -> item instanceof SimpleAbilityItem)
                .collect(Collectors.toList());

        if (abilities.isEmpty()) {
            thief.displayClientMessage(
                    Component.literal("Target has no abilities to steal!").withStyle(ChatFormatting.RED), false);
            return InteractionResult.FAIL;
        }

        List<String> abilityNames = abilities.stream()
                .map(item -> ForgeRegistries.ITEMS.getKey(item))
                .filter(key -> key != null)
                .map(ResourceLocation::toString)
                .collect(Collectors.toList());

        ModNetworking.INSTANCE.send(
                net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> (ServerPlayer) thief),
                new OpenPrometheusTheftScreenS2C(abilityNames, victim.getId())
        );

        return InteractionResult.SUCCESS;
    }

    public static void processChoice(ServerPlayer thief, ServerPlayer victim, String abilityRegistryName) {
        Level level = thief.level();
        if (thief.distanceToSqr(victim) >= RANGE * RANGE) {
            thief.sendSystemMessage(Component.literal("Target moved too far away!").withStyle(ChatFormatting.RED));
            return;
        }

        if (hasBorrowedAbility(thief)) {
            thief.sendSystemMessage(Component.literal("You already have a borrowed ability!").withStyle(ChatFormatting.RED));
            return;
        }

        ResourceLocation abilityKey = new ResourceLocation(abilityRegistryName);
        Item abilityItem = ForgeRegistries.ITEMS.getValue(abilityKey);
        if (abilityItem == null || !(abilityItem instanceof SimpleAbilityItem)) {
            thief.sendSystemMessage(Component.literal("Invalid ability!").withStyle(ChatFormatting.RED));
            return;
        }

        List<Item> victimAbilities = BeyonderUtil.getAbilitiesInPossession(victim);
        if (!victimAbilities.contains(abilityItem)) {
            thief.sendSystemMessage(Component.literal("Target no longer has that ability!").withStyle(ChatFormatting.RED));
            return;
        }

        int targetSeq = ((SimpleAbilityItem) abilityItem).getRequiredSequence();
        float luck = (float) thief.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.LUCK);

        float successRate = calculateSuccessRate(targetSeq, luck);

        boolean success = thief.getRandom().nextFloat() < Math.max(0.0f, Math.min(1.0f, successRate));

        if (success) {
            applyTheft(thief, victim, abilityItem, abilityRegistryName);
            SimpleAbilityItem.addCooldown(thief, abilityItem, THEFT_DURATION);
        } else {
            BeyonderUtil.useSpirituality(thief, 150);
            SimpleAbilityItem.addCooldown(thief, abilityItem, FAIL_COOLDOWN);
            thief.sendSystemMessage(
                    Component.literal("The theft failed! The target's power was too stable.").withStyle(ChatFormatting.RED));
        }
    }

    private static float calculateSuccessRate(int targetSeq, float luck) {
        if (targetSeq >= 6) {
            float rate = 0.85f;
            if (luck < 0) {
                rate += luck * 0.10f;
            } else {
                rate += luck * 0.05f;
            }
            return rate;
        } else {
            if (luck < 0) return 0.0f;
            int tiersAbove6 = 6 - targetSeq;
            float rate = 0.40f - (tiersAbove6 * 0.10f);
            rate = Math.max(0.0f, rate);
            rate += luck * 0.15f;
            return rate;
        }
    }

    private static void applyTheft(ServerPlayer thief, ServerPlayer victim, Item abilityItem, String abilityRegistryName) {
        CompoundTag thiefTag = thief.getPersistentData();
        thiefTag.putString("prometheusBorrowedAbility", abilityRegistryName);
        thiefTag.putInt("prometheusBorrowedTimer", THEFT_DURATION);
        thiefTag.putUUID("prometheusBorrowedFrom", victim.getUUID());

        CompoundTag victimTag = victim.getPersistentData();
        victimTag.putString("prometheusStolenAbility", abilityRegistryName);
        victimTag.putInt("prometheusStolenTimer", THEFT_DURATION);
        victimTag.putUUID("prometheusStolenBy", thief.getUUID());

        ScribedUtils.copyAbility(thief, abilityItem);

        ScribedUtils.removeAbility(victim, abilityItem);

        ItemStack stolenStack = new ItemStack(abilityItem);
        if (!thief.getInventory().add(stolenStack)) {
            ItemEntity entity = new ItemEntity(thief.level(), thief.getX(), thief.getY(), thief.getZ(), stolenStack);
            entity.setPickUpDelay(0);
            thief.level().addFreshEntity(entity);
        }

        for (int i = 0; i < victim.getInventory().getContainerSize(); i++) {
            ItemStack stack = victim.getInventory().getItem(i);
            if (stack.is(abilityItem)) {
                stack.shrink(1);
                break;
            }
        }

        Component abilityName = abilityItem.getName(new ItemStack(abilityItem));
        thief.sendSystemMessage(Component.literal("You successfully stole ")
                .append(abilityName.copy().withStyle(ChatFormatting.YELLOW))
                .append(Component.literal(" from ").withStyle(ChatFormatting.GREEN))
                .append(victim.getName().copy().withStyle(ChatFormatting.RED)));

        victim.sendSystemMessage(Component.literal("Your ability ")
                .append(abilityName.copy().withStyle(ChatFormatting.YELLOW))
                .append(Component.literal(" was stolen by ").withStyle(ChatFormatting.RED))
                .append(thief.getName().copy().withStyle(ChatFormatting.DARK_RED)));
    }

    public static boolean hasBorrowedAbility(LivingEntity living) {
        CompoundTag tag = living.getPersistentData();
        return tag.getInt("prometheusBorrowedTimer") > 0 && !tag.getString("prometheusBorrowedAbility").isEmpty();
    }

    public static boolean isBorrowedAbility(LivingEntity living, Item item) {
        if (living == null || item == null) return false;
        CompoundTag tag = living.getPersistentData();
        int timer = tag.getInt("prometheusBorrowedTimer");
        if (timer <= 0) return false;
        String borrowed = tag.getString("prometheusBorrowedAbility");
        if (borrowed.isEmpty()) return false;
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(item);
        return itemId != null && itemId.toString().equals(borrowed);
    }

    public static boolean isStolenAbility(LivingEntity living, Item item) {
        if (living == null || item == null) return false;
        CompoundTag tag = living.getPersistentData();
        int timer = tag.getInt("prometheusStolenTimer");
        if (timer <= 0) return false;
        String stolen = tag.getString("prometheusStolenAbility");
        if (stolen.isEmpty()) return false;
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(item);
        return itemId != null && itemId.toString().equals(stolen);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.literal("Steals an active ability from a target within " + RANGE + " blocks").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Success rate depends on target's sequence and your luck").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Borrowed ability lasts 2 minutes.").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Victim loses the ability for the duration.").withStyle(ChatFormatting.GRAY));
    }
}
