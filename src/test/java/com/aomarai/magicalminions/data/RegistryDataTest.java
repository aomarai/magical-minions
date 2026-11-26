package com.aomarai.magicalminions.data;

import com.aomarai.magicalminions.MagicalMinions;
import com.aomarai.magicalminions.minion.Frame;
import com.aomarai.magicalminions.minion.PartSlot;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Loads the real JSON shipped in the jar, then feeds the codecs deliberately broken JSON. */
class RegistryDataTest {
    private static final ResourceLocation RUNE_GOLEM = MagicalMinions.id("rune_golem");

    /** Every entry of a registry folder, parsed the way the game parses it. A parse failure names the file. */
    private static <T> Map<ResourceLocation, T> loadAll(String registry, Codec<T> codec) throws Exception {
        URL folder = RegistryDataTest.class.getResource("/data/magicalminions/magicalminions/" + registry);
        assertNotNull(folder, "no data folder for " + registry);
        Map<ResourceLocation, T> entries = new TreeMap<>();
        try (Stream<Path> files = Files.list(Path.of(folder.toURI()))) {
            for (Path file : files.filter(f -> f.toString().endsWith(".json")).toList()) {
                String name = file.getFileName().toString().replace(".json", "");
                JsonElement json = JsonParser.parseString(Files.readString(file));
                DataResult<T> result = codec.parse(JsonOps.INSTANCE, json);
                assertTrue(result.result().isPresent(),
                        registry + "/" + file.getFileName() + ": " + result.error().map(DataResult.Error::message).orElse("?"));
                entries.put(MagicalMinions.id(name), result.result().get());
            }
        }
        return entries;
    }

    private static String errorOf(Codec<?> codec, String json) {
        DataResult<?> result = codec.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
        assertTrue(result.isError(), "expected an error but it parsed: " + result);
        return result.error().orElseThrow().message();
    }

    @Test
    void shippedLineagesPartsAndMaterialsAllParse() throws Exception {
        assertFalse(loadAll("lineage", Lineage.CODEC).isEmpty());
        assertFalse(loadAll("part", MinionPart.CODEC).isEmpty());
        assertFalse(loadAll("material", MinionMaterial.CODEC).isEmpty());
    }

    @Test
    void runeGolemIsAStartingLineage() throws Exception {
        Lineage golem = loadAll("lineage", Lineage.CODEC).get(RUNE_GOLEM);
        assertNotNull(golem, "rune_golem.json is missing");
        assertTrue(golem.unlockItem().isEmpty(), "starting lineages need no Sigil");
        assertTrue(golem.allows(Frame.MEDIUM));
        assertTrue(golem.hasSocket(PartSlot.HEAD));
    }

    @Test
    void everyDefaultPartExistsFitsItsSlotAndHasAModelForEachFrame() throws Exception {
        Map<ResourceLocation, Lineage> lineages = loadAll("lineage", Lineage.CODEC);
        Map<ResourceLocation, MinionPart> parts = loadAll("part", MinionPart.CODEC);
        lineages.forEach((lineageId, lineage) -> lineage.defaultParts().forEach((slot, partId) -> {
            MinionPart part = parts.get(partId);
            assertNotNull(part, lineageId + " default " + slot + " points at missing part " + partId);
            assertTrue(part.slot() == slot, partId + " is a " + part.slot() + " part but " + lineageId + " uses it as " + slot);
            for (Frame frame : lineage.frames()) {
                assertTrue(part.hasModel(lineageId, frame),
                        partId + " has no " + lineageId + " model for the " + frame.getSerializedName() + " frame");
            }
        }));
    }

    @Test
    void everyPartModelFollowsTheNamingConvention() throws Exception {
        // <slot>_<part>_<style>_<frame>: the part id is <slot>_<part>, so the model is <part id>_<style>_<frame>.
        loadAll("part", MinionPart.CODEC).forEach((partId, part) -> part.styles().forEach((style, look) ->
                look.models().forEach((frame, model) -> {
                    String expected = "minion/part/" + partId.getPath() + "_" + style.getPath() + "_" + frame.getSerializedName();
                    assertTrue(model.getPath().equals(expected), partId + ": expected model " + expected + " but found " + model);
                })));
    }

    @Test
    void fullResonanceNeedsEverySocketPlusTheTorso() throws Exception {
        assertEquals(6, loadAll("lineage", Lineage.CODEC).get(RUNE_GOLEM).fullResonanceTarget());
        Lineage wisp = Lineage.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString("""
                {"frames": ["small"], "trait": "magicalminions:ethereal",
                 "torso_models": {"small": "magicalminions:minion/torso/wisp_small"},
                 "sockets": ["head", "arms", "legs", "accessory"],
                 "default_parts": {"torso": "magicalminions:torso_wisp"}}""")).getOrThrow();
        assertEquals(5, wisp.fullResonanceTarget(), "a lineage with no Back socket must still be able to reach full resonance");
    }

    @Test
    void lineageNamingAnUnknownFrameSaysWhichOne() {
        String error = errorOf(Lineage.CODEC, """
                {"frames": ["huge"], "trait": "magicalminions:stoneguard", "torso_models": {},
                 "sockets": [], "default_parts": {"torso": "magicalminions:torso_barrel"}}""");
        assertTrue(error.contains("huge"), error);
    }

    @Test
    void lineageListingAFrameWithoutATorsoModelIsRejected() {
        String error = errorOf(Lineage.CODEC, """
                {"frames": ["small", "medium"], "trait": "magicalminions:stoneguard",
                 "torso_models": {"medium": "magicalminions:minion/torso/rune_golem_medium"},
                 "sockets": [], "default_parts": {"torso": "magicalminions:torso_barrel"}}""");
        assertTrue(error.contains("small") && error.contains("no torso model"), error);
    }

    @Test
    void lineageDefaultPartForAMissingSocketIsRejected() {
        String error = errorOf(Lineage.CODEC, """
                {"frames": ["medium"], "trait": "magicalminions:ethereal",
                 "torso_models": {"medium": "magicalminions:minion/torso/wisp_medium"},
                 "sockets": ["head"],
                 "default_parts": {"torso": "magicalminions:torso_wisp", "back": "magicalminions:back_satchel"}}""");
        assertTrue(error.contains("back") && error.contains("no such socket"), error);
    }

    @Test
    void lineageWithoutADefaultTorsoIsRejected() {
        String error = errorOf(Lineage.CODEC, """
                {"frames": ["medium"], "trait": "magicalminions:stoneguard",
                 "torso_models": {"medium": "magicalminions:minion/torso/rune_golem_medium"},
                 "sockets": ["head"], "default_parts": {"head": "magicalminions:head_round"}}""");
        assertTrue(error.contains("torso"), error);
    }

    @Test
    void partMissingARequiredFieldNamesIt() {
        String error = errorOf(MinionPart.CODEC, """
                {"function": "magicalminions:round_head", "frames": ["medium"], "styles": {}}""");
        assertTrue(error.contains("slot"), error);
    }

    @Test
    void partStyleWithoutAModelForAListedFrameIsRejected() {
        String error = errorOf(MinionPart.CODEC, """
                {"slot": "head", "function": "magicalminions:round_head", "frames": ["small", "medium"],
                 "styles": {"magicalminions:rune_golem":
                            {"models": {"medium": "magicalminions:minion/part/head_round_rune_golem_medium"}}}}""");
        assertTrue(error.contains("small") && error.contains("no model"), error);
    }

    @Test
    void partWithNoStylesIsRejected() {
        String error = errorOf(MinionPart.CODEC, """
                {"slot": "head", "function": "magicalminions:round_head", "frames": ["medium"], "styles": {}}""");
        assertTrue(error.contains("at least one style"), error);
    }

    @Test
    void materialTextureSuffixMustBeAFileName() {
        String error = errorOf(MinionMaterial.CODEC, """
                {"tier": 1, "texture_suffix": "Rune Clay"}""");
        assertTrue(error.contains("texture_suffix"), error);
    }

    @Test
    void materialMultiplierOutsideTheSaneRangeIsRejected() {
        errorOf(MinionMaterial.CODEC, """
                {"tier": 1, "texture_suffix": "runeclay", "multipliers": {"health": 99}}""");
    }
}
