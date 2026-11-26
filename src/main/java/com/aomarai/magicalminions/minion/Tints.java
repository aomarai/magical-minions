package com.aomarai.magicalminions.minion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** The three tintable lights on a minion, as 0xRRGGBB. The bench limits choices to Glimmer Gems the player owns. */
public record Tints(int eyes, int core, int accent) {
    /** Foxfire for eyes and Core, madder for the one accent (design system tokens fox-3 and madder-2). */
    public static final Tints DEFAULT = new Tints(0x8AF5D4, 0x8AF5D4, 0x96222E);

    public static final Codec<Tints> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.optionalFieldOf("eyes", DEFAULT.eyes).forGetter(Tints::eyes),
            Codec.INT.optionalFieldOf("core", DEFAULT.core).forGetter(Tints::core),
            Codec.INT.optionalFieldOf("accent", DEFAULT.accent).forGetter(Tints::accent)
    ).apply(i, Tints::new));

    public static final StreamCodec<ByteBuf, Tints> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, Tints::eyes,
            ByteBufCodecs.INT, Tints::core,
            ByteBufCodecs.INT, Tints::accent,
            Tints::new);

    public Tints {
        eyes &= 0xFFFFFF;
        core &= 0xFFFFFF;
        accent &= 0xFFFFFF;
    }
}
