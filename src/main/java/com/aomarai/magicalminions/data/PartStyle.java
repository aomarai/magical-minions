package com.aomarai.magicalminions.data;

import com.aomarai.magicalminions.minion.Frame;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.Optional;

/**
 * How one part looks in one lineage's style: a model per frame, and the small perk that style adds.
 * Textures are not listed; they follow from the model id and the material.
 *
 * @param models     geo model id per frame, for example <code>magicalminions:minion/part/head_round_rune_golem_medium</code>
 * @param flavorPerk perk id the style adds, if any
 */
public record PartStyle(Map<Frame, ResourceLocation> models, Optional<ResourceLocation> flavorPerk) {
    public static final Codec<PartStyle> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.unboundedMap(Frame.CODEC, ResourceLocation.CODEC).fieldOf("models").forGetter(PartStyle::models),
            ResourceLocation.CODEC.optionalFieldOf("flavor_perk").forGetter(PartStyle::flavorPerk)
    ).apply(i, PartStyle::new));

    public PartStyle {
        models = Map.copyOf(models);
    }
}
