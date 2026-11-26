package com.aomarai.magicalminions.minion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * The identity of one minion, stored on its Core. The body can be rebuilt from a {@link MinionDesign}; this is
 * what must survive death, revival and recall.
 *
 * <p>Decoding only checks shape (non-negative, in range). Level caps and rune slot limits come from the Core
 * tier at the time they are used, so a later balance change can never make a stored Core fail to load.
 *
 * @param id          the minion's UUID, stable for life
 * @param instance    anti-duplication counter, raised every time the minion is revived or recalled; only the
 *                    Core or entity with the newest value is real
 * @param tier        Core tier
 * @param level       level, at least 1
 * @param xp          XP towards the next level
 * @param bond        long-term bond, 0 to {@value #MAX_BOND}
 * @param mood        short-term mood, 0 to {@value #MAX_MOOD}
 * @param personality personality id, empty until it is rolled when the minion is first animated
 * @param runes       socketed runes
 */
public record MinionCoreData(UUID id, int instance, CoreTier tier, int level, int xp, int bond, int mood,
                             Optional<ResourceLocation> personality, List<SocketedRune> runes) {
    public static final int MAX_BOND = 1000;
    public static final int MAX_MOOD = 100;
    /** Safety bound for the network codec only; real rune slot counts come from the Core tier. */
    private static final int MAX_RUNES_ON_WIRE = 16;

    public static final Codec<MinionCoreData> CODEC = RecordCodecBuilder.create(i -> i.group(
            UUIDUtil.CODEC.fieldOf("id").forGetter(MinionCoreData::id),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("instance").forGetter(MinionCoreData::instance),
            CoreTier.CODEC.fieldOf("tier").forGetter(MinionCoreData::tier),
            ExtraCodecs.POSITIVE_INT.fieldOf("level").forGetter(MinionCoreData::level),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("xp", 0).forGetter(MinionCoreData::xp),
            Codec.intRange(0, MAX_BOND).optionalFieldOf("bond", 0).forGetter(MinionCoreData::bond),
            Codec.intRange(0, MAX_MOOD).fieldOf("mood").forGetter(MinionCoreData::mood),
            ResourceLocation.CODEC.optionalFieldOf("personality").forGetter(MinionCoreData::personality),
            SocketedRune.CODEC.listOf().optionalFieldOf("runes", List.of()).forGetter(MinionCoreData::runes)
    ).apply(i, MinionCoreData::new));

    private static final StreamCodec<ByteBuf, List<SocketedRune>> RUNES_STREAM =
            SocketedRune.STREAM_CODEC.apply(ByteBufCodecs.list(MAX_RUNES_ON_WIRE));

    /** Written by hand because a composite codec stops at six fields. */
    public static final StreamCodec<ByteBuf, MinionCoreData> STREAM_CODEC = StreamCodec.of(
            (buf, d) -> {
                UUIDUtil.STREAM_CODEC.encode(buf, d.id);
                ByteBufCodecs.VAR_INT.encode(buf, d.instance);
                CoreTier.STREAM_CODEC.encode(buf, d.tier);
                ByteBufCodecs.VAR_INT.encode(buf, d.level);
                ByteBufCodecs.VAR_INT.encode(buf, d.xp);
                ByteBufCodecs.VAR_INT.encode(buf, d.bond);
                ByteBufCodecs.VAR_INT.encode(buf, d.mood);
                ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC).encode(buf, d.personality);
                RUNES_STREAM.encode(buf, d.runes);
            },
            buf -> new MinionCoreData(
                    UUIDUtil.STREAM_CODEC.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    CoreTier.STREAM_CODEC.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC).decode(buf),
                    RUNES_STREAM.decode(buf)));

    public MinionCoreData {
        runes = List.copyOf(runes);
    }
}
