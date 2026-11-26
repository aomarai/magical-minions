package com.aomarai.magicalminions.data;

import com.aomarai.magicalminions.minion.Frame;
import com.aomarai.magicalminions.minion.PartSlot;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

import java.util.List;
import java.util.Map;

/**
 * A part in the catalog, loaded from <code>data/&lt;ns&gt;/magicalminions/part/*.json</code>.
 * The function (what it does) is shared by every style; the styles say how it looks per lineage.
 *
 * @param slot      which slot it fills
 * @param function  what it is, for example <code>magicalminions:fists</code>
 * @param frames    frames it fits; every style must have a model for each
 * @param benchTier bench tier needed to use it, 1 for starting parts
 * @param modifiers flat stat bonuses
 * @param abilities ability ids it grants
 * @param styles    per-lineage look, keyed by lineage id
 */
public record MinionPart(PartSlot slot, ResourceLocation function, List<Frame> frames, int benchTier,
                         StatModifiers modifiers, List<ResourceLocation> abilities,
                         Map<ResourceLocation, PartStyle> styles) {
    public static final Codec<MinionPart> CODEC = RecordCodecBuilder.<MinionPart>create(i -> i.group(
            PartSlot.CODEC.fieldOf("slot").forGetter(MinionPart::slot),
            ResourceLocation.CODEC.fieldOf("function").forGetter(MinionPart::function),
            ExtraCodecs.nonEmptyList(Frame.CODEC.listOf()).fieldOf("frames").forGetter(MinionPart::frames),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("bench_tier", 1).forGetter(MinionPart::benchTier),
            StatModifiers.CODEC.optionalFieldOf("modifiers", StatModifiers.NONE).forGetter(MinionPart::modifiers),
            ResourceLocation.CODEC.listOf().optionalFieldOf("abilities", List.of()).forGetter(MinionPart::abilities),
            Codec.unboundedMap(ResourceLocation.CODEC, PartStyle.CODEC).fieldOf("styles").forGetter(MinionPart::styles)
    ).apply(i, MinionPart::new)).validate(MinionPart::validate);

    public MinionPart {
        frames = List.copyOf(frames);
        abilities = List.copyOf(abilities);
        styles = Map.copyOf(styles);
    }

    /** Whether this part can be drawn in the given lineage's style on the given frame. */
    public boolean hasModel(ResourceLocation style, Frame frame) {
        PartStyle s = styles.get(style);
        return s != null && s.models().containsKey(frame);
    }

    private DataResult<MinionPart> validate() {
        if (styles.isEmpty()) {
            return DataResult.error(() -> "A part needs at least one style");
        }
        for (Map.Entry<ResourceLocation, PartStyle> style : styles.entrySet()) {
            for (Frame frame : frames) {
                if (!style.getValue().models().containsKey(frame)) {
                    return DataResult.error(() -> "Style " + style.getKey() + " has no model for the "
                            + frame.getSerializedName() + " frame this part lists");
                }
            }
        }
        return DataResult.success(this);
    }
}
