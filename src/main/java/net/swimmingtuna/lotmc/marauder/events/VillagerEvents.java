package net.swimmingtuna.lotmc.marauder.events;

import net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.caps.BeyonderHolderAttacher;
import net.swimmingtuna.lotm.init.BeyonderClassInit;

public class VillagerEvents {
    @SubscribeEvent
    public static void onMerchantOpen(PlayerContainerEvent.Open event) {
        if (!(event.getContainer() instanceof MerchantMenu menu)) return;

        Player player = (Player) event.getEntity();
        if (player.level().isClientSide()) return;

        BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
        if (!holder.currentClassMatches(BeyonderClassInit.MARAUDER) || holder.getSequence() > 8) return;

        MerchantOffers offers = menu.getOffers();
        for (MerchantOffer offer : offers) {
            int basePrice = offer.getBaseCostA().getCount();
            int discount = (int) Math.floor(basePrice * 0.3);
            if (discount > 0) {
                offer.addToSpecialPriceDiff(-discount);
            }
        }

        if (player instanceof ServerPlayer sp) {
            sp.connection.send(new ClientboundMerchantOffersPacket(
                    menu.containerId,
                    offers,
                    menu.getTraderLevel(),
                    menu.getTraderXp(),
                    menu.showProgressBar(),
                    menu.canRestock()
            ));
        }
    }
}
