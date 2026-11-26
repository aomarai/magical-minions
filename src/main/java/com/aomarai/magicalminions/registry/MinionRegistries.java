package com.aomarai.magicalminions.registry;

import com.aomarai.magicalminions.MagicalMinions;
import com.aomarai.magicalminions.data.Lineage;
import com.aomarai.magicalminions.data.MinionMaterial;
import com.aomarai.magicalminions.data.MinionPart;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

/**
 * Datapack registries. Entries load from <code>data/&lt;ns&gt;/magicalminions/{lineage,part,material}/*.json</code>
 * and are synced to clients, so the bench and renderer see the same catalog as the server.
 */
public final class MinionRegistries {
    public static final ResourceKey<Registry<Lineage>> LINEAGE = key("lineage");
    public static final ResourceKey<Registry<MinionPart>> PART = key("part");
    public static final ResourceKey<Registry<MinionMaterial>> MATERIAL = key("material");

    private MinionRegistries() {}

    private static <T> ResourceKey<Registry<T>> key(String name) {
        return ResourceKey.createRegistryKey(MagicalMinions.id(name));
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(MinionRegistries::onNewDataPackRegistries);
    }

    private static void onNewDataPackRegistries(DataPackRegistryEvent.NewRegistry event) {
        // The same codec for disk and network: clients need every field to build previews.
        event.dataPackRegistry(LINEAGE, Lineage.CODEC, Lineage.CODEC);
        event.dataPackRegistry(PART, MinionPart.CODEC, MinionPart.CODEC);
        event.dataPackRegistry(MATERIAL, MinionMaterial.CODEC, MinionMaterial.CODEC);
    }
}
