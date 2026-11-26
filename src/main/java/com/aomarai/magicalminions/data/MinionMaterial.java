package com.aomarai.magicalminions.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * A material, loaded from <code>data/&lt;ns&gt;/magicalminions/material/*.json</code>.
 *
 * @param tier        rank on the tier ladder, 1 for the first material
 * @param multipliers how the material scales stats
 * @param setBonus    bonus id for wearing a full set, if any
 * @param textureSuffix name of the texture file for this material, for example <code>runeclay</code>
 * @param repairItem  items that repair a minion of this material, if any
 */
public record MinionMaterial(int tier, StatMultipliers multipliers, Optional<ResourceLocation> setBonus,
                             String textureSuffix, Optional<TagKey<Item>> repairItem) {
    private static final Pattern FILE_NAME = Pattern.compile("[a-z0-9_]+");

    public static final Codec<MinionMaterial> CODEC = RecordCodecBuilder.<MinionMaterial>create(i -> i.group(
            ExtraCodecs.POSITIVE_INT.fieldOf("tier").forGetter(MinionMaterial::tier),
            StatMultipliers.CODEC.optionalFieldOf("multipliers", StatMultipliers.IDENTITY).forGetter(MinionMaterial::multipliers),
            ResourceLocation.CODEC.optionalFieldOf("set_bonus").forGetter(MinionMaterial::setBonus),
            Codec.STRING.fieldOf("texture_suffix").forGetter(MinionMaterial::textureSuffix),
            TagKey.hashedCodec(Registries.ITEM).optionalFieldOf("repair_item").forGetter(MinionMaterial::repairItem)
    ).apply(i, MinionMaterial::new)).validate(MinionMaterial::validate);

    private DataResult<MinionMaterial> validate() {
        if (!FILE_NAME.matcher(textureSuffix).matches()) {
            return DataResult.error(() -> "texture_suffix '" + textureSuffix
                    + "' must be lower case letters, digits and underscores, because it names a texture file");
        }
        return DataResult.success(this);
    }
}
