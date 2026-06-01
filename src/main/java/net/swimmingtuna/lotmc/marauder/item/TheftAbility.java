package net.swimmingtuna.lotmc.marauder.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.Tags;
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.caps.BeyonderHolderAttacher;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;

import java.util.ArrayList;
import java.util.List;

public class TheftAbility extends SimpleAbilityItem {
    public TheftAbility(Properties properties) {
        super(properties, () -> BeyonderClassInit.MARAUDER.get(), 9, 100, 2400);
    }

    @Override
    public InteractionResult interactLivingEntityLivingEntity(ItemStack pStack, LivingEntity pPlayer, LivingEntity pInteractionTarget, InteractionHand pUsedHand) {
        if (!(pPlayer instanceof Player thief) || !(pInteractionTarget instanceof Player victim))
            return InteractionResult.FAIL;

        Level level = thief.level();
        if (level.isClientSide()) return InteractionResult.FAIL;

        // Distance check: strictly less than 2.0 blocks
        if (thief.distanceToSqr(victim) >= 4.0) {
            thief.displayClientMessage(
                    Component.literal("Target is too far!").withStyle(ChatFormatting.RED),
                    false
            );
            return InteractionResult.FAIL;
        }

        // Sequence check
        BeyonderHolder thiefHolder = BeyonderHolderAttacher.getHolderUnwrap(thief);
        BeyonderHolder victimHolder = BeyonderHolderAttacher.getHolderUnwrap(victim);

        if (!thiefHolder.currentClassMatches(BeyonderClassInit.MARAUDER)) {
            thief.displayClientMessage(
                    Component.literal("You are not a Marauder!").withStyle(ChatFormatting.RED),
                    false
            );
            return InteractionResult.FAIL;
        }

        int thiefSequence = thiefHolder.getSequence();
        int victimSequence = victimHolder.getSequence();

        if (victimSequence != -1 && victimSequence <= thiefSequence - 2) {
            thief.displayClientMessage(
                    Component.literal("Target is too powerful to steal from!").withStyle(ChatFormatting.RED),
                    false
            );
            return InteractionResult.FAIL;
        }

        // Find valid items in victim's inventory
        List<Integer> validSlots = findValidItems(victim);

        if (validSlots.isEmpty()) {
            thief.displayClientMessage(
                    Component.literal("Nothing valuable to steal!").withStyle(ChatFormatting.GRAY),
                    false
            );
            return InteractionResult.FAIL;
        }

        // Consume spirituality
        if (!thiefHolder.useSpirituality(100)) {
            thief.displayClientMessage(
                    Component.literal("Not enough spirituality!").withStyle(ChatFormatting.RED),
                    false
            );
            return InteractionResult.FAIL;
        }

        // Pick a random valid slot
        int slotIndex = validSlots.get(thief.getRandom().nextInt(validSlots.size()));
        ItemStack targetStack = victim.getInventory().getItem(slotIndex);

        // Steal exactly 1 unit
        ItemStack stolen = targetStack.copy();
        stolen.setCount(1);
        targetStack.shrink(1);

        // Give to thief or drop at feet
        if (!thief.getInventory().add(stolen)) {
            ItemEntity itemEntity = new ItemEntity(level, thief.getX(), thief.getY(), thief.getZ(), stolen);
            itemEntity.setPickUpDelay(0);
            level.addFreshEntity(itemEntity);
        }

        this.addCooldown(thief);
        return InteractionResult.SUCCESS;
    }

    private static boolean isMaterialItem(ItemStack stack) {
        return stack.is(Tags.Items.INGOTS)
                || stack.is(Tags.Items.GEMS)
                || stack.is(Tags.Items.DUSTS)
                || stack.is(Tags.Items.RAW_MATERIALS);
    }

    private List<Integer> findValidItems(Player victim) {
        List<Integer> slots = new ArrayList<>();
        var inv = victim.getInventory();
        int mainHandSlot = inv.selected;
        int offhandSlot = inv.items.size() + inv.armor.size();

        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (i == mainHandSlot || i == offhandSlot) continue;

            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.getMaxStackSize() <= 1) continue;
            if (!isMaterialItem(stack)) continue;

            slots.add(i);
        }

        return slots;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.literal("Steals one material/ingredient from a nearby player's inventory").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Range: 2 blocks").withStyle(ChatFormatting.GRAY));
    }
}
