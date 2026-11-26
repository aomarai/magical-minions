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
import java.util.Optional;

/**
 * A lineage, the kind of creature a Torso makes, loaded from <code>data/&lt;ns&gt;/magicalminions/lineage/*.json</code>.
 *
 * @param frames       frames it can be built in; each needs a torso model
 * @param trait        innate trait id, for example <code>magicalminions:stoneguard</code>
 * @param torsoModels  geo model id of the Torso per frame
 * @param sockets      which of the five attachment slots this Torso has (a Wisp has no Back)
 * @param unlockItem   item that unlocks it (a Sigil); empty for a starting lineage
 * @param defaultParts part used per slot for a new minion; the Torso is required
 * @param largeForm    entity id of its Large mount form, if it has one
 */
public record Lineage(List<Frame> frames, ResourceLocation trait, Map<Frame, ResourceLocation> torsoModels,
                      List<PartSlot> sockets, Optional<ResourceLocation> unlockItem,
                      Map<PartSlot, ResourceLocation> defaultParts, Optional<ResourceLocation> largeForm) {
    public static final Codec<Lineage> CODEC = RecordCodecBuilder.<Lineage>create(i -> i.group(
            ExtraCodecs.nonEmptyList(Frame.CODEC.listOf()).fieldOf("frames").forGetter(Lineage::frames),
            ResourceLocation.CODEC.fieldOf("trait").forGetter(Lineage::trait),
            Codec.unboundedMap(Frame.CODEC, ResourceLocation.CODEC).fieldOf("torso_models").forGetter(Lineage::torsoModels),
            PartSlot.CODEC.listOf().fieldOf("sockets").forGetter(Lineage::sockets),
            ResourceLocation.CODEC.optionalFieldOf("unlock_item").forGetter(Lineage::unlockItem),
            Codec.unboundedMap(PartSlot.CODEC, ResourceLocation.CODEC).fieldOf("default_parts").forGetter(Lineage::defaultParts),
            ResourceLocation.CODEC.optionalFieldOf("large_form").forGetter(Lineage::largeForm)
    ).apply(i, Lineage::new)).validate(Lineage::validate);

    public Lineage {
        frames = List.copyOf(frames);
        torsoModels = Map.copyOf(torsoModels);
        sockets = List.copyOf(sockets);
        defaultParts = Map.copyOf(defaultParts);
    }

    public boolean allows(Frame frame) {
        return frames.contains(frame);
    }

    public boolean hasSocket(PartSlot slot) {
        return sockets.contains(slot);
    }

    /**
     * How many matching parts (Torso included) give full resonance and unlock the lineage's signature ability.
     * Every socket the lineage has, plus the Torso: 6 for most lineages, 5 for the Wisp (no Back socket). The
     * design doc says 6 of 6, which the Wisp could never reach.
     */
    public int fullResonanceTarget() {
        return sockets.size() + 1;
    }

    private DataResult<Lineage> validate() {
        for (Frame frame : frames) {
            if (!torsoModels.containsKey(frame)) {
                return DataResult.error(() -> "The lineage lists the " + frame.getSerializedName()
                        + " frame but has no torso model for it");
            }
        }
        if (sockets.contains(PartSlot.TORSO)) {
            return DataResult.error(() -> "sockets lists the Torso, but the Torso is the base the sockets sit on");
        }
        if (!defaultParts.containsKey(PartSlot.TORSO)) {
            return DataResult.error(() -> "default_parts needs a torso entry");
        }
        for (PartSlot slot : defaultParts.keySet()) {
            if (slot != PartSlot.TORSO && !sockets.contains(slot)) {
                return DataResult.error(() -> "default_parts fills the " + slot.getSerializedName()
                        + " slot, but this lineage has no such socket");
            }
        }
        return DataResult.success(this);
    }
}
