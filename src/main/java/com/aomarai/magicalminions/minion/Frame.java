package com.aomarai.magicalminions.minion;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

/** A minion's size class. It sets the Command Point cost and which parts fit. */
public enum Frame implements StringRepresentable {
    SMALL("small", 1),
    MEDIUM("medium", 1),
    LARGE("large", 2);

    public static final Codec<Frame> CODEC = StringRepresentable.fromEnum(Frame::values);
    public static final StreamCodec<ByteBuf, Frame> STREAM_CODEC = ByteBufCodecs.idMapper(
            ByIdMap.continuous(Frame::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO), Frame::ordinal);

    private final String serializedName;
    private final int commandPoints;

    Frame(String serializedName, int commandPoints) {
        this.serializedName = serializedName;
        this.commandPoints = commandPoints;
    }

    /** Command Points this frame uses in the Active Party. */
    public int commandPoints() {
        return commandPoints;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}
