package com.aomarai.magicalminions.registry;

import com.aomarai.magicalminions.MagicalMinions;
import com.aomarai.magicalminions.minion.MinionCoreData;
import com.aomarai.magicalminions.minion.MinionDesign;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Item data components that carry a minion's design and its Core identity. */
public final class ModDataComponents {
    private static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MagicalMinions.MOD_ID);

    /** What the minion is built from. On minion items, the Core and Dormant Core. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<MinionDesign>> MINION_DESIGN =
            COMPONENTS.registerComponentType("minion_design", builder -> builder
                    .persistent(MinionDesign.CODEC)
                    .networkSynchronized(MinionDesign.STREAM_CODEC));

    /** Who the minion is: UUID, instance counter, tier, level, bond, runes. On Cores, Lanterns and Dormant Cores. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<MinionCoreData>> CORE_DATA =
            COMPONENTS.registerComponentType("core_data", builder -> builder
                    .persistent(MinionCoreData.CODEC)
                    .networkSynchronized(MinionCoreData.STREAM_CODEC));

    private ModDataComponents() {}

    public static void register(IEventBus modEventBus) {
        COMPONENTS.register(modEventBus);
    }
}
