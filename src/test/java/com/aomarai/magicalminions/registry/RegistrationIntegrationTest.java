package com.aomarai.magicalminions.registry;

import com.aomarai.magicalminions.MagicalMinions;
import com.aomarai.magicalminions.data.Lineage;
import com.aomarai.magicalminions.data.MinionMaterial;
import com.aomarai.magicalminions.data.MinionPart;
import io.netty.buffer.Unpooled;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DataPackRegistriesHooks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static com.aomarai.magicalminions.minion.TestSamples.core;
import static com.aomarai.magicalminions.minion.TestSamples.golem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks the wiring the codec tests cannot see: that the mod registers its components and datapack registries, and
 * that the JSON folder layout is the one the game actually reads.
 */
class RegistrationIntegrationTest {
    private static RegistryAccess builtIns() {
        return RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
    }

    @Test
    void dataComponentsAreRegistered() {
        assertTrue(BuiltInRegistries.DATA_COMPONENT_TYPE.containsKey(MagicalMinions.id("minion_design")));
        assertTrue(BuiltInRegistries.DATA_COMPONENT_TYPE.containsKey(MagicalMinions.id("core_data")));
    }

    @Test
    void shippedJsonLoadsThroughTheRealDatapackRegistryLoader() throws Exception {
        // The folder that holds data/ in the built jar; the same files a player's game reads.
        Path packRoot = Path.of(Lineage.class.getResource("/data").toURI()).getParent();
        PackLocationInfo info = new PackLocationInfo("test", Component.literal("test"), PackSource.BUILT_IN, Optional.empty());

        List<RegistryDataLoader.RegistryData<?>> ours = DataPackRegistriesHooks.getDataPackRegistries().stream()
                .filter(data -> data.key().location().getNamespace().equals(MagicalMinions.MOD_ID))
                .toList();
        assertEquals(3, ours.size(), "lineage, part and material should be registered as datapack registries");

        try (MultiPackResourceManager manager = new MultiPackResourceManager(PackType.SERVER_DATA,
                List.of(new PathPackResources(info, packRoot)))) {
            RegistryAccess loaded = RegistryDataLoader.load(manager, builtIns(), ours);

            Registry<Lineage> lineages = loaded.registryOrThrow(MinionRegistries.LINEAGE);
            Registry<MinionPart> parts = loaded.registryOrThrow(MinionRegistries.PART);
            Registry<MinionMaterial> materials = loaded.registryOrThrow(MinionRegistries.MATERIAL);

            assertNotNull(lineages.get(MagicalMinions.id("rune_golem")), "rune_golem.json was not found by the loader");
            assertEquals(7, parts.size());
            assertNotNull(parts.get(MagicalMinions.id("head_round")));
            assertEquals(2, materials.size());
            assertNotNull(materials.get(MagicalMinions.id("starbrass")));
        }
    }

    @Test
    void aBrokenEntryStopsTheLoadInsteadOfSilentlyDroppingTheEntry(@TempDir Path packRoot) throws Exception {
        // A lineage that lists a frame it has no torso model for. Vanilla logs the file and the reason, then
        // refuses to load the registry. That is deliberate: a part that quietly vanished would break every minion
        // saved with it.
        Path folder = Files.createDirectories(packRoot.resolve("data/magicalminions/magicalminions/lineage"));
        Files.writeString(folder.resolve("broken.json"), """
                {"frames": ["small"], "trait": "magicalminions:stoneguard", "torso_models": {},
                 "sockets": [], "default_parts": {"torso": "magicalminions:torso_barrel"}}""");
        PackLocationInfo info = new PackLocationInfo("broken", Component.literal("broken"), PackSource.BUILT_IN, Optional.empty());
        List<RegistryDataLoader.RegistryData<?>> lineageOnly = DataPackRegistriesHooks.getDataPackRegistries().stream()
                .filter(data -> data.key().equals(MinionRegistries.LINEAGE))
                .toList();

        try (MultiPackResourceManager manager = new MultiPackResourceManager(PackType.SERVER_DATA,
                List.of(new PathPackResources(info, packRoot)))) {
            assertThrows(IllegalStateException.class, () -> RegistryDataLoader.load(manager, builtIns(), lineageOnly));
        }
    }

    private static ItemStack stackWithBothComponents() {
        ItemStack stack = new ItemStack(Items.STONE);
        stack.set(ModDataComponents.MINION_DESIGN.get(), golem());
        stack.set(ModDataComponents.CORE_DATA.get(), core());
        return stack;
    }

    @Test
    void componentsSurviveSavingAnItemStackToNbt() {
        RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, builtIns());

        Tag saved = ItemStack.CODEC.encodeStart(ops, stackWithBothComponents()).getOrThrow();
        ItemStack loaded = ItemStack.CODEC.parse(ops, saved).getOrThrow();

        assertEquals(golem(), loaded.get(ModDataComponents.MINION_DESIGN.get()));
        assertEquals(core(), loaded.get(ModDataComponents.CORE_DATA.get()));
    }

    @Test
    void componentsSurviveBeingSentToAClient() {
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), builtIns());

        ItemStack.STREAM_CODEC.encode(buf, stackWithBothComponents());
        ItemStack received = ItemStack.STREAM_CODEC.decode(buf);

        assertEquals(golem(), received.get(ModDataComponents.MINION_DESIGN.get()));
        assertEquals(core(), received.get(ModDataComponents.CORE_DATA.get()));
    }
}
