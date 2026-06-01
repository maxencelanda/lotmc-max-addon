package net.swimmingtuna.lotmc.marauder.attributes.PathwayAttributes;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.swimmingtuna.lotm.attributes.BaseAttributes;
import net.swimmingtuna.lotm.attributes.ModAttributes;

import java.util.List;
import java.util.UUID;

public class MarauderAttributes extends BaseAttributes {
    public static final List<Double> healthList = List.of(80.0, 55.0, 45.0, 38.0, 32.0, 28.0, 26.0, 24.0, 23.0, 22.0);
    public static final List<Double> speedList = List.of(0.06, 0.06, 0.05, 0.05, 0.04, 0.03, 0.02, 0.01, 0.0, 0.0);
    public static final List<Double> attackList = List.of(8.0, 7.0, 6.0, 5.0, 4.0, 3.0, 2.0, 0.0, 0.0, 0.0);
    public static final List<Double> jumpList = List.of(0.1, 0.08, 0.06, 0.04, 0.02, 0.01, 0.0, 0.0, 0.0, 0.0);
    public static final List<Double> digSpeedList = List.of(1.0, 0.5, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
    public static final List<Double> fireResistanceList = List.of(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
    public static final List<Double> attackSpeedList = List.of(0.3, 0.28, 0.26, 0.24, 0.22, 0.2, 0.18, 0.16, 0.14, 0.12);

    private static final UUID ATTACK_SPEED_UUID = UUID.fromString("8a9b1c2d-3e4f-5a6b-7c8d-9e0fa1b2c3d4");

    public static void applyAll(LivingEntity entity, int sequence) {
        AttributeInstance healthAttr = entity.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr != null) {
            apply(healthAttr, healthBoostID, healthList.get(sequence) - 20.0, "HealthBoost");
        }

        AttributeInstance speedAttr = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr != null) {
            apply(speedAttr, speedID, speedList.get(sequence), "SpeedBoost");
        }

        AttributeInstance attackAttr = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackAttr != null) {
            apply(attackAttr, attackID, attackList.get(sequence), "AttackBoost");
        }

        AttributeInstance jumpAttr = entity.getAttribute(ModAttributes.JUMP_BOOST.get());
        if (jumpAttr != null) {
            apply(jumpAttr, jumpID, jumpList.get(sequence), "JumpBoost");
        }

        AttributeInstance digSpeedAttr = entity.getAttribute(ModAttributes.DIG_SPEED.get());
        if (digSpeedAttr != null) {
            apply(digSpeedAttr, digSpeedID, digSpeedList.get(sequence), "DigSpeed");
        }

        AttributeInstance fireResistanceAttr = entity.getAttribute(ModAttributes.FIRE_RESISTANCE.get());
        if (fireResistanceAttr != null) {
            apply(fireResistanceAttr, fireResistanceID, fireResistanceList.get(sequence), "FireResistance");
        }

        AttributeInstance attackSpeedAttr = entity.getAttribute(Attributes.ATTACK_SPEED);
        if (attackSpeedAttr != null) {
            apply(attackSpeedAttr, ATTACK_SPEED_UUID, attackSpeedList.get(sequence), "AttackSpeed");
        }
    }

    public static void cleanAll(LivingEntity entity) {
        BaseAttributes.cleanAll(entity);

        AttributeInstance attackSpeedAttr = entity.getAttribute(Attributes.ATTACK_SPEED);
        if (attackSpeedAttr != null) {
            clean(attackSpeedAttr, ATTACK_SPEED_UUID);
        }
    }
}
