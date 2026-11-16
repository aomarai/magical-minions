package com.aomarai.magicalminions;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

/**
 * Entry point for Magical Minions.
 *
 * <p>Registries (entities, items, blocks, menus, data components) are added here as
 * features land; see the design doc for the planned architecture.
 */
@Mod(MagicalMinions.MOD_ID)
public final class MagicalMinions {
    public static final String MOD_ID = "magicalminions";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MagicalMinions(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.SERVER, MinionsConfig.SPEC);
        LOGGER.info("Magical Minions is loading");
    }

    /** Builds a {@code magicalminions:<path>} id. */
    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
