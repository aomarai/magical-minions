package com.aomarai.magicalminions.minion;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.nbt.NbtOps;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static com.aomarai.magicalminions.minion.CodecTestSupport.errorOf;
import static com.aomarai.magicalminions.minion.TestSamples.CORE_ID;
import static com.aomarai.magicalminions.minion.TestSamples.core;
import static com.aomarai.magicalminions.minion.CodecTestSupport.roundTrip;
import static com.aomarai.magicalminions.minion.CodecTestSupport.streamRoundTrip;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MinionCoreDataCodecTest {
    /** The encoded form of {@link #core()} with one field replaced, to test a single bad value. */
    private static JsonObject encodedWith(String field, Object value) {
        JsonObject json = MinionCoreData.CODEC.encodeStart(JsonOps.INSTANCE, core()).getOrThrow().getAsJsonObject();
        if (value instanceof Number n) {
            json.addProperty(field, n);
        } else {
            json.addProperty(field, String.valueOf(value));
        }
        return json;
    }

    @Test
    void roundTripsThroughJson() {
        assertEquals(core(), roundTrip(MinionCoreData.CODEC, core(), JsonOps.INSTANCE));
    }

    @Test
    void roundTripsThroughNbt() {
        assertEquals(core(), roundTrip(MinionCoreData.CODEC, core(), NbtOps.INSTANCE));
    }

    @Test
    void roundTripsThroughNetworkCodec() {
        assertEquals(core(), streamRoundTrip(MinionCoreData.STREAM_CODEC, core()));
    }

    @Test
    void personalityAndRunesMayBeEmpty() {
        MinionCoreData fresh = new MinionCoreData(CORE_ID, 0, CoreTier.RUNECLAY, 1, 0, 0, 50, Optional.empty(), List.of());
        assertEquals(fresh, roundTrip(MinionCoreData.CODEC, fresh, JsonOps.INSTANCE));
        assertEquals(fresh, roundTrip(MinionCoreData.CODEC, fresh, NbtOps.INSTANCE));
        assertEquals(fresh, streamRoundTrip(MinionCoreData.STREAM_CODEC, fresh));
    }

    @Test
    void runesCannotBeChangedAfterConstruction() {
        assertThrows(UnsupportedOperationException.class, () -> core().runes().clear());
    }

    @Test
    void rejectsBondAboveTheMaximum() {
        String error = errorOf(MinionCoreData.CODEC.parse(JsonOps.INSTANCE, encodedWith("bond", MinionCoreData.MAX_BOND + 1)));
        org.junit.jupiter.api.Assertions.assertTrue(error.contains("1001"), error);
    }

    @Test
    void rejectsMoodAboveTheMaximum() {
        errorOf(MinionCoreData.CODEC.parse(JsonOps.INSTANCE, encodedWith("mood", MinionCoreData.MAX_MOOD + 1)));
    }

    @Test
    void rejectsNegativeInstanceCounter() {
        errorOf(MinionCoreData.CODEC.parse(JsonOps.INSTANCE, encodedWith("instance", -1)));
    }

    @Test
    void rejectsLevelZero() {
        errorOf(MinionCoreData.CODEC.parse(JsonOps.INSTANCE, encodedWith("level", 0)));
    }

    @Test
    void rejectsUnknownTierByName() {
        String error = errorOf(MinionCoreData.CODEC.parse(JsonOps.INSTANCE, encodedWith("tier", "copper")));
        org.junit.jupiter.api.Assertions.assertTrue(error.contains("copper"), error);
    }

    @Test
    void rejectsRuneGradeOutsideOneToThree() {
        JsonObject json = MinionCoreData.CODEC.encodeStart(JsonOps.INSTANCE, core()).getOrThrow().getAsJsonObject();
        json.getAsJsonArray("runes").get(0).getAsJsonObject().addProperty("grade", 4);
        errorOf(MinionCoreData.CODEC.parse(JsonOps.INSTANCE, json));
    }

    @Test
    void decodingChecksShapeOnlyNotTierLimits() {
        // Level caps come from the Core tier when used. A stored Core must still load if a cap is lowered later.
        MinionCoreData high = new MinionCoreData(CORE_ID, 1, CoreTier.RUNECLAY, 200, 0, 0, 50, Optional.empty(), List.of());
        assertEquals(high, roundTrip(MinionCoreData.CODEC, high, JsonOps.INSTANCE));
    }
}
