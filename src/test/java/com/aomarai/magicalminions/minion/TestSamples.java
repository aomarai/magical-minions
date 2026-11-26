package com.aomarai.magicalminions.minion;

import com.aomarai.magicalminions.MagicalMinions;
import net.minecraft.resources.ResourceLocation;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Sample values shared by the tests. */
public final class TestSamples {
    public static final UUID CORE_ID = UUID.fromString("5f1c2a7d-3b8e-4f6a-8c5d-7e9b1a3c5e70");

    private TestSamples() {}

    public static ResourceLocation id(String path) {
        return MagicalMinions.id(path);
    }

    /** A Rune Golem with an Ember-style back part, so it is a mixed-lineage design. */
    public static MinionDesign golem() {
        ResourceLocation style = id("rune_golem");
        ResourceLocation clay = id("runeclay");
        Map<PartSlot, PartChoice> parts = new EnumMap<>(PartSlot.class);
        parts.put(PartSlot.TORSO, new PartChoice(id("torso_barrel"), style, clay));
        parts.put(PartSlot.HEAD, new PartChoice(id("head_round"), style, clay));
        parts.put(PartSlot.ARMS, new PartChoice(id("arms_fists"), style, clay));
        parts.put(PartSlot.LEGS, new PartChoice(id("legs_stubby"), style, clay));
        parts.put(PartSlot.BACK, new PartChoice(id("back_glide_wings"), id("ember_imp"), id("starbrass")));
        return new MinionDesign(style, Frame.MEDIUM, parts, new Tints(0x112233, 0x445566, 0x778899), "Pebble");
    }

    public static MinionCoreData core() {
        return new MinionCoreData(CORE_ID, 3, CoreTier.STARBRASS, 12, 340, 650, 70, Optional.of(id("cheerful")),
                List.of(new SocketedRune(id("ember"), 2), new SocketedRune(id("mending"), 1)));
    }
}
