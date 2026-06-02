package net.swimmingtuna.lotmc.marauder.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotmc.marauder.networking.ModNetworking;
import net.swimmingtuna.lotmc.marauder.networking.packet.RotateCameraPacketS2C;

import java.util.List;
import java.util.Random;

public class ThoughtMisdirectionAbility extends SimpleAbilityItem {
    private static final int RANGE = 6;

    public ThoughtMisdirectionAbility(Properties properties) {
        super(properties, () -> BeyonderClassInit.MARAUDER.get(), 8, 80, 400);
    }

    @Override
    public InteractionResult interactLivingEntityLivingEntity(ItemStack stack, LivingEntity user, LivingEntity target, InteractionHand hand) {
        if (!(target instanceof Player victim) || !(user instanceof Player thief)) return InteractionResult.FAIL;
        if (user.level().isClientSide()) return InteractionResult.FAIL;

        if (!this.checkAll(user)) return InteractionResult.FAIL;

        if (user.distanceToSqr(target) >= RANGE * RANGE) {
            thief.displayClientMessage(
                    Component.translatable("message.lotmc_marauder.target_too_far").withStyle(ChatFormatting.RED),
                    false
            );
            return InteractionResult.FAIL;
        }

        Random random = new Random();
        float angle = 120.0f + random.nextFloat() * 120.0f;
        if (random.nextBoolean()) angle = -angle;

        ModNetworking.INSTANCE.send(
                PacketDistributor.PLAYER.with(() -> (ServerPlayer) victim),
                new RotateCameraPacketS2C(angle)
        );

        this.addCooldown(thief);
        this.useSpirituality(thief);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.lotmc_marauder.thought_misdirection.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.lotmc_marauder.thought_misdirection.range", RANGE).withStyle(ChatFormatting.GRAY));
    }
}
