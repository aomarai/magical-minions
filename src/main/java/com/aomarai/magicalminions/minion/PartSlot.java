package com.aomarai.magicalminions.minion;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

/**
 * The six places a part can go. The Torso decides the lineage; the other five attach to socket bones on it
 * (<code>socket_head</code>, <code>socket_arm_*</code>, <code>socket_legs</code>, <code>socket_back</code>,
 * <code>socket_acc</code>).
 */
public enum PartSlot implements StringRepresentable {
    TORSO("torso"),
    HEAD("head"),
    ARMS("arms"),
    LEGS("legs"),
    BACK("back"),
    ACCESSORY("accessory");

    public static final Codec<PartSlot> CODEC = StringRepresentable.fromEnum(PartSlot::values);
    public static final StreamCodec<ByteBuf, PartSlot> STREAM_CODEC = ByteBufCodecs.idMapper(
            ByIdMap.continuous(PartSlot::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO), PartSlot::ordinal);

    private final String serializedName;

    PartSlot(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}
