package net.swimmingtuna.lotmc.marauder.networking.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import net.swimmingtuna.lotmc.marauder.item.PrometheusTheftAbility;

import java.util.function.Supplier;

public class PrometheusTheftChoiceC2S {
    private final String abilityRegistryName;
    private final int victimEntityId;

    public PrometheusTheftChoiceC2S(String abilityRegistryName, int victimEntityId) {
        this.abilityRegistryName = abilityRegistryName;
        this.victimEntityId = victimEntityId;
    }

    public PrometheusTheftChoiceC2S(FriendlyByteBuf buf) {
        this.abilityRegistryName = buf.readUtf();
        this.victimEntityId = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(this.abilityRegistryName);
        buf.writeInt(this.victimEntityId);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer thief = context.getSender();
            if (thief == null) return;
            Level level = thief.level();
            Entity victimEntity = level.getEntity(this.victimEntityId);
            if (!(victimEntity instanceof ServerPlayer victim)) return;
            PrometheusTheftAbility.processChoice(thief, victim, this.abilityRegistryName);
        });
        return true;
    }
}
