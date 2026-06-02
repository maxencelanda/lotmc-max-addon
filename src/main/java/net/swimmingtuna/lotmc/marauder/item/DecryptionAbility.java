package net.swimmingtuna.lotmc.marauder.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.caps.BeyonderHolderAttacher;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator.PsychologicalInvisibility;

import java.util.List;

public class DecryptionAbility extends SimpleAbilityItem {
    private static final int RANGE = 15;

    public DecryptionAbility(Properties properties) {
        super(properties, () -> BeyonderClassInit.MARAUDER.get(), 7, 150, 1200);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity entity, InteractionHand hand) {
        if (!this.checkAll(entity)) return InteractionResult.FAIL;
        if (level.isClientSide()) return InteractionResult.FAIL;

        Player user = (Player) entity;
        ServerLevel serverLevel = (ServerLevel) level;

        BeyonderHolder userHolder = BeyonderHolderAttacher.getHolderUnwrap(user);
        if (userHolder == null) return InteractionResult.FAIL;
        int userSequence = userHolder.getSequence();

        AABB area = user.getBoundingBox().inflate(RANGE);
        List<Player> nearbyPlayers = serverLevel.getEntitiesOfClass(Player.class, area, p -> p != user && p.isAlive());

        int revealedCount = 0;
        for (Player target : nearbyPlayers) {
            boolean revealed = false;

            if (target.hasEffect(MobEffects.INVISIBILITY)) {
                target.removeEffect(MobEffects.INVISIBILITY);
                revealed = true;
            }

            BeyonderHolder targetHolder = BeyonderHolderAttacher.getHolderUnwrap(target);
            if (targetHolder != null && targetHolder.currentClassMatches(BeyonderClassInit.SPECTATOR)) {
                int targetSequence = targetHolder.getSequence();
                if (targetSequence >= userSequence - 1) {
                    if (target.getPersistentData().getBoolean("psychologicalInvisibility")) {
                        PsychologicalInvisibility.removePsychologicalInvisibilityEffect(target);
                        revealed = true;
                    }
                }
            }

            if (revealed) {
                revealedCount++;
            }
        }

        if (revealedCount == 0) {
            user.displayClientMessage(
                    Component.translatable("message.lotmc_marauder.no_hidden_players").withStyle(ChatFormatting.GRAY),
                    false
            );
            return InteractionResult.FAIL;
        }

        user.displayClientMessage(
                Component.translatable("message.lotmc_marauder.decryption_success", revealedCount)
                        .withStyle(ChatFormatting.GREEN),
                false
        );

        this.addCooldown(entity);
        this.useSpirituality(entity);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.lotmc_marauder.decryption.desc", RANGE).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.lotmc_marauder.decryption.desc2").withStyle(ChatFormatting.GRAY));
    }
}
