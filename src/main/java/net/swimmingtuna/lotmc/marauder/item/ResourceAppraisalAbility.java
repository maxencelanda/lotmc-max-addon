package net.swimmingtuna.lotmc.marauder.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.AABB;
import net.swimmingtuna.lotm.beyonder.api.BeyonderClass;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;

import java.util.*;

public class ResourceAppraisalAbility extends SimpleAbilityItem {
    private static final int RANGE = 10;

    public ResourceAppraisalAbility(Properties properties) {
        super(properties, () -> BeyonderClassInit.MARAUDER.get(), 9, 50, 600);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity entity, InteractionHand hand) {
        if (!this.checkAll(entity)) return InteractionResult.FAIL;
        Player player = (Player) entity;
        scanResources(level, player);
        this.addCooldown(entity);
        this.useSpirituality(entity);
        return InteractionResult.SUCCESS;
    }

    private void scanResources(Level level, Player player) {
        ServerLevel serverLevel = (ServerLevel) level;
        AABB area = player.getBoundingBox().inflate(RANGE);

        Map<String, ResourceCount> totalResources = new LinkedHashMap<>();

        scanChests(serverLevel, area, totalResources);
        scanPlayers(serverLevel, area, player, totalResources);

        sendResults(player, totalResources);
    }

    private void scanPlayers(ServerLevel level, AABB area, Player source, Map<String, ResourceCount> result) {
        List<Player> nearbyPlayers = level.getEntitiesOfClass(Player.class, area, p -> p != source && p.isAlive());
        for (Player target : nearbyPlayers) {
            for (int i = 0; i < target.getInventory().getContainerSize(); i++) {
                ItemStack stack = target.getInventory().getItem(i);
                if (!stack.isEmpty()) {
                    String key = getResourceKey(stack);
                    if (key != null) {
                        result.computeIfAbsent(key, ResourceCount::new).addCount(stack.getCount());
                    }
                }
            }
        }
    }

    private void scanChests(ServerLevel level, AABB area, Map<String, ResourceCount> result) {
        BlockPos.betweenClosedStream(
                new BlockPos((int) area.minX, (int) area.minY, (int) area.minZ),
                new BlockPos((int) area.maxX, (int) area.maxY, (int) area.maxZ)
        ).forEach(pos -> {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ChestBlockEntity chest) {
                for (int i = 0; i < chest.getContainerSize(); i++) {
                    ItemStack stack = chest.getItem(i);
                    if (!stack.isEmpty()) {
                        String key = getResourceKey(stack);
                        if (key != null) {
                            result.computeIfAbsent(key, ResourceCount::new).addCount(stack.getCount());
                        }
                    }
                }
            }
        });
    }

    private String getResourceKey(ItemStack stack) {
        if (stack.is(Items.IRON_INGOT)) return "Iron Ingot";
        if (stack.is(Items.IRON_BLOCK)) return "Iron Block";
        if (stack.is(Items.RAW_IRON)) return "Raw Iron";
        if (stack.is(Items.GOLD_INGOT)) return "Gold Ingot";
        if (stack.is(Items.GOLD_BLOCK)) return "Gold Block";
        if (stack.is(Items.RAW_GOLD)) return "Raw Gold";
        if (stack.is(Items.DIAMOND)) return "Diamond";
        if (stack.is(Items.DIAMOND_BLOCK)) return "Diamond Block";
        if (stack.is(Items.EMERALD)) return "Emerald";
        if (stack.is(Items.EMERALD_BLOCK)) return "Emerald Block";
        if (stack.is(Items.NETHERITE_INGOT)) return "Netherite Ingot";
        if (stack.is(Items.NETHERITE_BLOCK)) return "Netherite Block";
        if (stack.is(Items.COPPER_INGOT)) return "Copper Ingot";
        if (stack.is(Items.RAW_COPPER)) return "Raw Copper";
        return null;
    }

    private void sendResults(Player player, Map<String, ResourceCount> resources) {
        if (resources.isEmpty()) {
            player.displayClientMessage(
                    Component.literal("No valuable resources detected nearby.").withStyle(ChatFormatting.GRAY),
                    false
            );
            return;
        }

        player.displayClientMessage(
                Component.literal("=== Resource Appraisal ===").withStyle(ChatFormatting.DARK_PURPLE),
                false
        );
        long totalValue = 0;
        for (Map.Entry<String, ResourceCount> entry : resources.entrySet()) {
            ResourceCount rc = entry.getValue();
            totalValue += rc.total;
            player.displayClientMessage(
                    Component.literal("  " + entry.getKey() + ": " + rc.count + " stacks (" + rc.total + " total)")
                            .withStyle(ChatFormatting.LIGHT_PURPLE),
                    false
            );
        }
        player.displayClientMessage(
                Component.literal("Total items: " + totalValue).withStyle(ChatFormatting.DARK_PURPLE),
                false
        );
    }

    private static class ResourceCount {
        String name;
        int count = 0;
        int total = 0;

        ResourceCount(String name) {
            this.name = name;
        }

        void addCount(int amount) {
            this.count++;
            this.total += amount;
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.literal("Scans nearby players & chests for valuable resources").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Range: " + RANGE + " blocks").withStyle(ChatFormatting.GRAY));
    }
}
