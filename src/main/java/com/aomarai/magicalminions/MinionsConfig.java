package com.aomarai.magicalminions;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Server config. Values match the "Config (server)" section of the design doc. */
public final class MinionsConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue MAX_COMMAND_POINTS = BUILDER
            .comment("Command Points available for the Active Party. Small and Medium minions cost 1, Large cost 2.")
            .defineInRange("maxCommandPoints", 4, 1, 16);

    public static final ModConfigSpec.DoubleValue ANIMUS_FARM_MULTIPLIER = BUILDER
            .comment("Animus multiplier for kills that look farmed (spawners, repeated kills in one spot).")
            .defineInRange("animusFarmMultiplier", 0.25, 0.0, 1.0);

    public static final ModConfigSpec.IntValue TELEPORT_FOLLOW_DISTANCE = BUILDER
            .comment("Distance in blocks at which a following minion teleports to its owner.")
            .defineInRange("teleportFollowDistance", 32, 8, 128);

    public static final ModConfigSpec.BooleanValue MINIONS_CAN_GRIEF = BUILDER
            .comment("Whether minions may break or place blocks outside their owner's direct orders.")
            .define("minionsCanGrief", false);

    public static final ModConfigSpec.BooleanValue ALLOW_CROSS_DIMENSION_RECALL = BUILDER
            .comment("Whether the Bond Bell can recall minions from another dimension.")
            .define("allowCrossDimensionRecall", true);

    public static final ModConfigSpec.IntValue BOND_LOSS_ON_HIT = BUILDER
            .comment("Bond lost when the owner hits their own minion.")
            .defineInRange("bondLossOnHit", 10, 0, 100);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private MinionsConfig() {}
}
