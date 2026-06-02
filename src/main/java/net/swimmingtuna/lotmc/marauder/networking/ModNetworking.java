package net.swimmingtuna.lotmc.marauder.networking;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.swimmingtuna.lotmc.marauder.networking.packet.OpenPrometheusTheftScreenS2C;
import net.swimmingtuna.lotmc.marauder.networking.packet.PrometheusTheftChoiceC2S;
import net.swimmingtuna.lotmc.marauder.networking.packet.RotateCameraPacketS2C;

public class ModNetworking {
    private static final String PROTOCOL_VERSION = "1.0";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("lotmc_marauder", "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    private static int nextId = 0;

    public static void register() {
        INSTANCE.messageBuilder(RotateCameraPacketS2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(RotateCameraPacketS2C::new)
                .encoder(RotateCameraPacketS2C::toBytes)
                .consumerMainThread(RotateCameraPacketS2C::handle)
                .add();
        INSTANCE.messageBuilder(OpenPrometheusTheftScreenS2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(OpenPrometheusTheftScreenS2C::new)
                .encoder(OpenPrometheusTheftScreenS2C::toBytes)
                .consumerMainThread(OpenPrometheusTheftScreenS2C::handle)
                .add();
        INSTANCE.messageBuilder(PrometheusTheftChoiceC2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(PrometheusTheftChoiceC2S::new)
                .encoder(PrometheusTheftChoiceC2S::toBytes)
                .consumerMainThread(PrometheusTheftChoiceC2S::handle)
                .add();
    }

    private static int id() {
        return nextId++;
    }
}
