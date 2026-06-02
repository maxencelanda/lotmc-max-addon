package net.swimmingtuna.lotmc.marauder.networking.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.swimmingtuna.lotmc.marauder.screen.PrometheusTheftScreen;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class OpenPrometheusTheftScreenS2C {
    private final List<String> abilityRegistryNames;
    private final int victimEntityId;

    public OpenPrometheusTheftScreenS2C(List<String> abilityRegistryNames, int victimEntityId) {
        this.abilityRegistryNames = abilityRegistryNames;
        this.victimEntityId = victimEntityId;
    }

    public OpenPrometheusTheftScreenS2C(FriendlyByteBuf buf) {
        int size = buf.readInt();
        this.abilityRegistryNames = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            this.abilityRegistryNames.add(buf.readUtf());
        }
        this.victimEntityId = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.abilityRegistryNames.size());
        for (String name : this.abilityRegistryNames) {
            buf.writeUtf(name);
        }
        buf.writeInt(this.victimEntityId);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            mc.setScreen(new PrometheusTheftScreen(this.abilityRegistryNames, this.victimEntityId));
        });
        return true;
    }
}
