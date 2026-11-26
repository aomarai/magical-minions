package com.aomarai.magicalminions.minion;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

/**
 * Core tiers, lowest first. Only the names live here: level caps and rune slots belong to the levelling work
 * (#35), so a balance change never makes a stored Core fail to load.
 */
public enum CoreTier implements StringRepresentable {
    RUNECLAY("runeclay"),
    STARBRASS("starbrass"),
    MOONSILVER("moonsilver"),
    SOULGLASS("soulglass"),
    EMBER("ember"),
    VOID("void"),
    STARBOUND("starbound");

    public static final Codec<CoreTier> CODEC = StringRepresentable.fromEnum(CoreTier::values);
    public static final StreamCodec<ByteBuf, CoreTier> STREAM_CODEC = ByteBufCodecs.idMapper(
            ByIdMap.continuous(CoreTier::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO), CoreTier::ordinal);

    private final String serializedName;

    CoreTier(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}
