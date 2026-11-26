package com.aomarai.magicalminions.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** How a material scales the stats it is built from. Missing fields are 1.0 (no change). */
public record StatMultipliers(double health, double armor, double speed, double attackDamage) {
    public static final StatMultipliers IDENTITY = new StatMultipliers(1, 1, 1, 1);

    private static final Codec<Double> MULTIPLIER = Codec.doubleRange(0.0, 10.0);

    public static final Codec<StatMultipliers> CODEC = RecordCodecBuilder.create(i -> i.group(
            MULTIPLIER.optionalFieldOf("health", 1.0).forGetter(StatMultipliers::health),
            MULTIPLIER.optionalFieldOf("armor", 1.0).forGetter(StatMultipliers::armor),
            MULTIPLIER.optionalFieldOf("speed", 1.0).forGetter(StatMultipliers::speed),
            MULTIPLIER.optionalFieldOf("attack_damage", 1.0).forGetter(StatMultipliers::attackDamage)
    ).apply(i, StatMultipliers::new));
}
