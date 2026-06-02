package net.swimmingtuna.lotmc.marauder.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.swimmingtuna.lotm.capabilities.sanity_data.SanityUtil;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;

import java.util.List;
import java.util.Random;

public class MentalDisruptionAbility extends SimpleAbilityItem {
    private static final int RANGE = 15;
    private static final SoundEvent[] HALLUCINATION_SOUNDS = {
            SoundEvents.CREEPER_PRIMED,
            SoundEvents.WARDEN_HEARTBEAT,
            SoundEvents.ENDERMAN_STARE,
            SoundEvents.PLAYER_HURT
    };

    public MentalDisruptionAbility(Properties properties) {
        super(properties, () -> BeyonderClassInit.MARAUDER.get(), 8, 100, 1200);
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

        ServerPlayer serverVictim = (ServerPlayer) victim;
        Random random = new Random();
        int soundCount = 1 + random.nextInt(3);

        long baseDelay = 4000L + random.nextInt(1000);
        long interval = 2000L + random.nextInt(1000);

        for (int i = 0; i < soundCount; i++) {
            SoundEvent sound = HALLUCINATION_SOUNDS[random.nextInt(HALLUCINATION_SOUNDS.length)];
            long delayMs = baseDelay + (long) i * interval;
            scheduleHallucination(serverVictim, sound, delayMs);
        }

        SanityUtil.decreaseSanity(victim, 3);

        this.addCooldown(thief);
        this.useSpirituality(thief);
        return InteractionResult.SUCCESS;
    }

    private static void sendHallucination(ServerPlayer victim, SoundEvent sound) {
        victim.connection.send(new ClientboundSoundPacket(
                Holder.direct(sound),
                SoundSource.AMBIENT,
                victim.getX(), victim.getY(), victim.getZ(),
                1.0f, 1.0f,
                victim.getRandom().nextLong()
        ));
    }

    private static void scheduleHallucination(ServerPlayer victim, SoundEvent sound, long delayMs) {
        // Use the server thread tick scheduler instead of a raw Thread
        int delayTicks = Math.max(1, (int) (delayMs / 50));
        tickDownHallucination(victim, sound, delayTicks);
    }

    private static void tickDownHallucination(ServerPlayer victim, SoundEvent sound, int ticksLeft) {
        if (ticksLeft <= 0) {
            sendHallucination(victim, sound);
        } else {
            victim.getServer().execute(() -> tickDownHallucination(victim, sound, ticksLeft - 1));
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.lotmc_marauder.mental_disruption.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.lotmc_marauder.mental_disruption.range", RANGE).withStyle(ChatFormatting.GRAY));
    }
}
