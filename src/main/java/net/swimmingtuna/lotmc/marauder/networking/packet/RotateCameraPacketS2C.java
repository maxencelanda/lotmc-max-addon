package net.swimmingtuna.lotmc.marauder.networking.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RotateCameraPacketS2C {
    private final float yawDelta;

    public RotateCameraPacketS2C(float yawDelta) {
        this.yawDelta = yawDelta;
    }

    public RotateCameraPacketS2C(FriendlyByteBuf buf) {
        this.yawDelta = buf.readFloat();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeFloat(this.yawDelta);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            var player = Minecraft.getInstance().player;
            if (player != null) {
                player.setYRot(player.getYRot() + this.yawDelta);
            }
        });
        return true;
    }
}
