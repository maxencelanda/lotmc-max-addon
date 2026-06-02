package net.swimmingtuna.lotmc.marauder.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotmc.marauder.item.PrometheusTheftAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SimpleAbilityItem.class)
public class SimpleAbilityItemMixin {

    @Inject(method = "checkAll(Lnet/minecraft/world/entity/LivingEntity;)Z", at = @At("RETURN"), cancellable = true, remap = false)
    private void onCheckAll(LivingEntity living, CallbackInfoReturnable<Boolean> cir) {

        if (cir.getReturnValue() && PrometheusTheftAbility.isStolenAbility(living, (Item) (Object) this)) {
            cir.setReturnValue(false);
            return;
        }

        if (!cir.getReturnValue() && PrometheusTheftAbility.isBorrowedAbility(living, (Item) (Object) this)) {
            BeyonderUtil.copyAbilities(living.level(), living, (SimpleAbilityItem) (Object) this);
            cir.setReturnValue(true);
        }
    }
}
