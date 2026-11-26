package com.aomarai.magicalminions.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;

/** Flat bonuses a part adds to a minion. Missing fields are zero. */
public record StatModifiers(double health, double armor, double speed, double attackDamage, int inventorySlots) {
    public static final StatModifiers NONE = new StatModifiers(0, 0, 0, 0, 0);

    public static final Codec<StatModifiers> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.DOUBLE.optionalFieldOf("health", 0.0).forGetter(StatModifiers::health),
            Codec.DOUBLE.optionalFieldOf("armor", 0.0).forGetter(StatModifiers::armor),
            Codec.DOUBLE.optionalFieldOf("speed", 0.0).forGetter(StatModifiers::speed),
            Codec.DOUBLE.optionalFieldOf("attack_damage", 0.0).forGetter(StatModifiers::attackDamage),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("inventory_slots", 0).forGetter(StatModifiers::inventorySlots)
    ).apply(i, StatModifiers::new));
}
