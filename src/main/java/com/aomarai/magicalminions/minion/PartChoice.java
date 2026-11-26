package com.aomarai.magicalminions.minion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

/**
 * One filled slot of a design: which part, drawn in which lineage's style, made of which material.
 * Ids point into the datapack registries; they are resolved when the minion is built, not stored as objects.
 *
 * @param part     part id, for example <code>magicalminions:head_round</code>
 * @param style    lineage whose look the part uses, for example <code>magicalminions:rune_golem</code>
 * @param material material id, for example <code>magicalminions:runeclay</code>
 */
public record PartChoice(ResourceLocation part, ResourceLocation style, ResourceLocation material) {
    public static final Codec<PartChoice> CODEC = RecordCodecBuilder.create(i -> i.group(
            ResourceLocation.CODEC.fieldOf("part").forGetter(PartChoice::part),
            ResourceLocation.CODEC.fieldOf("style").forGetter(PartChoice::style),
            ResourceLocation.CODEC.fieldOf("material").forGetter(PartChoice::material)
    ).apply(i, PartChoice::new));

    public static final StreamCodec<ByteBuf, PartChoice> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, PartChoice::part,
            ResourceLocation.STREAM_CODEC, PartChoice::style,
            ResourceLocation.STREAM_CODEC, PartChoice::material,
            PartChoice::new);
}
