package com.aomarai.magicalminions.minion;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.nbt.NbtOps;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

import static com.aomarai.magicalminions.minion.CodecTestSupport.errorOf;
import static com.aomarai.magicalminions.minion.CodecTestSupport.json;
import static com.aomarai.magicalminions.minion.CodecTestSupport.roundTrip;
import static com.aomarai.magicalminions.minion.CodecTestSupport.streamRoundTrip;
import static com.aomarai.magicalminions.minion.TestSamples.golem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MinionDesignCodecTest {

    @Test
    void roundTripsThroughJson() {
        assertEquals(golem(), roundTrip(MinionDesign.CODEC, golem(), JsonOps.INSTANCE));
    }

    @Test
    void roundTripsThroughNbt() {
        assertEquals(golem(), roundTrip(MinionDesign.CODEC, golem(), NbtOps.INSTANCE));
    }

    @Test
    void roundTripsThroughNetworkCodec() {
        assertEquals(golem(), streamRoundTrip(MinionDesign.STREAM_CODEC, golem()));
    }

    @Test
    void equalDesignsHaveEqualHashes() {
        // Data components are compared and hashed to decide whether item stacks merge.
        assertEquals(golem(), golem());
        assertEquals(golem().hashCode(), golem().hashCode());
    }

    @Test
    void partsCannotBeChangedAfterConstruction() {
        Map<PartSlot, PartChoice> source = new EnumMap<>(golem().parts());
        MinionDesign design = new MinionDesign(golem().lineage(), Frame.MEDIUM, source, Tints.DEFAULT, "");
        source.remove(PartSlot.TORSO);
        assertTrue(design.part(PartSlot.TORSO).isPresent(), "the design must keep its own copy");
        assertThrows(UnsupportedOperationException.class, () -> design.parts().remove(PartSlot.HEAD));
    }

    @Test
    void optionalFieldsFallBackToDefaults() {
        MinionDesign design = MinionDesign.CODEC.parse(JsonOps.INSTANCE, json("""
                {"lineage": "magicalminions:rune_golem", "frame": "small",
                 "parts": {"torso": {"part": "magicalminions:torso_barrel",
                                     "style": "magicalminions:rune_golem",
                                     "material": "magicalminions:runeclay"}}}""")).getOrThrow();
        assertEquals(Tints.DEFAULT, design.tints());
        assertEquals("", design.name());
        assertEquals(Frame.SMALL, design.frame());
    }

    @Test
    void rejectsDesignWithoutTorso() {
        String error = errorOf(MinionDesign.CODEC.parse(JsonOps.INSTANCE, json("""
                {"lineage": "magicalminions:rune_golem", "frame": "medium", "parts": {}}""")));
        assertTrue(error.contains("Torso"), error);
    }

    @Test
    void rejectsTorsoStyleThatDiffersFromLineage() {
        String error = errorOf(MinionDesign.CODEC.parse(JsonOps.INSTANCE, json("""
                {"lineage": "magicalminions:rune_golem", "frame": "medium",
                 "parts": {"torso": {"part": "magicalminions:torso_barrel",
                                     "style": "magicalminions:ember_imp",
                                     "material": "magicalminions:runeclay"}}}""")));
        assertTrue(error.contains("must match the lineage"), error);
    }

    @Test
    void rejectsNamesLongerThanTheLimit() {
        String longName = "x".repeat(MinionDesign.MAX_NAME_LENGTH + 1);
        JsonElement tooLong = MinionDesign.CODEC.encodeStart(JsonOps.INSTANCE, golem()).getOrThrow();
        tooLong.getAsJsonObject().addProperty("name", longName);
        errorOf(MinionDesign.CODEC.parse(JsonOps.INSTANCE, tooLong));
    }

    @Test
    void rejectsUnknownFrameWithAReadableMessage() {
        String error = errorOf(MinionDesign.CODEC.parse(JsonOps.INSTANCE, json("""
                {"lineage": "magicalminions:rune_golem", "frame": "huge", "parts": {}}""")));
        assertTrue(error.contains("huge"), error);
    }
}
