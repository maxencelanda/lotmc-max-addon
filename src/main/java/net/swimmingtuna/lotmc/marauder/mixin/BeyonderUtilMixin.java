package net.swimmingtuna.lotmc.marauder.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotmc.marauder.item.PrometheusTheftAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BeyonderUtil.class)
public class BeyonderUtilMixin {

    @Inject(method = "canUseAbility", at = @At("RETURN"), cancellable = true, remap = false)
    private static void onCanUseAbility(LivingEntity living, Item item, CallbackInfoReturnable<Boolean> cir) {
        if (PrometheusTheftAbility.isStolenAbility(living, item)) {
            cir.setReturnValue(false);
            return;
        }
        if (!cir.getReturnValue() && PrometheusTheftAbility.isBorrowedAbility(living, item)) {
            cir.setReturnValue(true);
        }
    }
}
