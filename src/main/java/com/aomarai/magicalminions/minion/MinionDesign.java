package com.aomarai.magicalminions.minion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * What a minion is made of: the blueprint the bench edits and every minion item carries. It holds ids only, so it
 * stays valid when datapacks change; the registries turn ids into models and stats when the minion is built.
 *
 * <p>The lineage comes from the Torso, so the Torso's style must equal {@link #lineage()}.
 *
 * @param lineage lineage id, for example <code>magicalminions:rune_golem</code>
 * @param frame   size class
 * @param parts   one choice per filled slot; the Torso is required, the rest depend on the lineage's sockets
 * @param tints   eye, Core and accent colors
 * @param name    custom name, empty when unnamed
 */
public record MinionDesign(ResourceLocation lineage, Frame frame, Map<PartSlot, PartChoice> parts, Tints tints,
                           String name) {
    public static final int MAX_NAME_LENGTH = 32;

    private static final Codec<MinionDesign> RAW_CODEC = RecordCodecBuilder.create(i -> i.group(
            ResourceLocation.CODEC.fieldOf("lineage").forGetter(MinionDesign::lineage),
            Frame.CODEC.fieldOf("frame").forGetter(MinionDesign::frame),
            Codec.unboundedMap(PartSlot.CODEC, PartChoice.CODEC).fieldOf("parts").forGetter(MinionDesign::parts),
            Tints.CODEC.optionalFieldOf("tints", Tints.DEFAULT).forGetter(MinionDesign::tints),
            Codec.string(0, MAX_NAME_LENGTH).optionalFieldOf("name", "").forGetter(MinionDesign::name)
    ).apply(i, MinionDesign::new));

    /** Disk and NBT form. Rejects designs without a Torso or whose Torso style differs from the lineage. */
    public static final Codec<MinionDesign> CODEC = RAW_CODEC.validate(MinionDesign::validate);

    public static final StreamCodec<ByteBuf, MinionDesign> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, MinionDesign::lineage,
            Frame.STREAM_CODEC, MinionDesign::frame,
            ByteBufCodecs.<ByteBuf, PartSlot, PartChoice, Map<PartSlot, PartChoice>>map(
                    size -> new EnumMap<>(PartSlot.class), PartSlot.STREAM_CODEC, PartChoice.STREAM_CODEC),
            MinionDesign::parts,
            Tints.STREAM_CODEC, MinionDesign::tints,
            ByteBufCodecs.stringUtf8(MAX_NAME_LENGTH), MinionDesign::name,
            MinionDesign::new);

    public MinionDesign {
        Objects.requireNonNull(lineage, "lineage");
        Objects.requireNonNull(frame, "frame");
        Objects.requireNonNull(tints, "tints");
        Objects.requireNonNull(name, "name");
        EnumMap<PartSlot, PartChoice> copy = new EnumMap<>(PartSlot.class);
        copy.putAll(parts);
        parts = Collections.unmodifiableMap(copy);
    }

    public Optional<PartChoice> part(PartSlot slot) {
        return Optional.ofNullable(parts.get(slot));
    }

    private DataResult<MinionDesign> validate() {
        PartChoice torso = parts.get(PartSlot.TORSO);
        if (torso == null) {
            return DataResult.error(() -> "A minion design needs a Torso part");
        }
        if (!torso.style().equals(lineage)) {
            return DataResult.error(() -> "The Torso style " + torso.style() + " must match the lineage " + lineage);
        }
        return DataResult.success(this);
    }
}
