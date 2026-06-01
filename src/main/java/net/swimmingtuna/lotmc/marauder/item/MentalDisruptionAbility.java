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
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;

import java.util.List;
import java.util.Random;

public class MentalDisruptionAbility extends SimpleAbilityItem {
    private static final int RANGE = 10;
    private static final SoundEvent[] HALLUCINATION_SOUNDS = {
            SoundEvents.CREEPER_PRIMED,
            SoundEvents.WARDEN_HEARTBEAT,
            SoundEvents.ENDERMAN_STARE,
            SoundEvents.PLAYER_HURT
    };

    public MentalDisruptionAbility(Properties properties) {
        super(properties, () -> BeyonderClassInit.MARAUDER.get(), 8, 60, 300);
    }

    @Override
    public InteractionResult interactLivingEntityLivingEntity(ItemStack stack, LivingEntity user, LivingEntity target, InteractionHand hand) {
        if (!(target instanceof Player victim) || !(user instanceof Player thief)) return InteractionResult.FAIL;
        if (user.level().isClientSide()) return InteractionResult.FAIL;

        if (!this.checkAll(user)) return InteractionResult.FAIL;

        if (user.distanceToSqr(target) >= RANGE * RANGE) {
            thief.displayClientMessage(
                    Component.literal("Target is too far!").withStyle(ChatFormatting.RED),
                    false
            );
            return InteractionResult.FAIL;
        }

        ServerPlayer serverVictim = (ServerPlayer) victim;
        Random random = new Random();
        int soundCount = 1 + random.nextInt(3);

        for (int i = 0; i < soundCount; i++) {
            SoundEvent sound = HALLUCINATION_SOUNDS[random.nextInt(HALLUCINATION_SOUNDS.length)];
            long delayMs = (long) i * (1000L + random.nextInt(1000));

            if (delayMs == 0) {
                sendHallucination(serverVictim, sound);
            } else {
                scheduleHallucination(serverVictim, sound, delayMs);
            }
        }

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
        new Thread(() -> {
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            victim.connection.send(new ClientboundSoundPacket(
                    Holder.direct(sound),
                    SoundSource.AMBIENT,
                    victim.getX(), victim.getY(), victim.getZ(),
                    1.0f, 1.0f,
                    victim.getRandom().nextLong()
            ));
        }).start();
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.literal("Plays terrifying hallucination sounds in the target's ears").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Range: " + RANGE + " blocks").withStyle(ChatFormatting.GRAY));
    }
}
