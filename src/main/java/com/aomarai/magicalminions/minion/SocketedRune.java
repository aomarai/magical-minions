package com.aomarai.magicalminions.minion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

/** A rune socketed into a Core. Runes have three grades; a higher grade needs a higher Inscriber tier. */
public record SocketedRune(ResourceLocation rune, int grade) {
    public static final int MIN_GRADE = 1;
    public static final int MAX_GRADE = 3;

    public static final Codec<SocketedRune> CODEC = RecordCodecBuilder.create(i -> i.group(
            ResourceLocation.CODEC.fieldOf("rune").forGetter(SocketedRune::rune),
            Codec.intRange(MIN_GRADE, MAX_GRADE).fieldOf("grade").forGetter(SocketedRune::grade)
    ).apply(i, SocketedRune::new));

    public static final StreamCodec<ByteBuf, SocketedRune> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, SocketedRune::rune,
            ByteBufCodecs.VAR_INT, SocketedRune::grade,
            SocketedRune::new);
}
