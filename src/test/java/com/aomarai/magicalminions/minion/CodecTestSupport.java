package com.aomarai.magicalminions.minion;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.codec.StreamCodec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Round-trip and error helpers shared by the codec tests. */
public final class CodecTestSupport {
    private CodecTestSupport() {}

    /** Encodes then decodes, failing the test if either step fails. */
    public static <T, E> T roundTrip(Codec<T> codec, T value, DynamicOps<E> ops) {
        E encoded = codec.encodeStart(ops, value).getOrThrow();
        return codec.parse(ops, encoded).getOrThrow();
    }

    /** Writes then reads through the network codec and checks nothing is left unread. */
    public static <T> T streamRoundTrip(StreamCodec<ByteBuf, T> codec, T value) {
        ByteBuf buf = Unpooled.buffer();
        codec.encode(buf, value);
        T decoded = codec.decode(buf);
        assertEquals(0, buf.readableBytes(), "decoding must consume exactly what encoding wrote");
        return decoded;
    }

    /** The message of a failed decode; fails the test if the decode succeeded. */
    public static String errorOf(DataResult<?> result) {
        assertTrue(result.isError(), "expected a decode error but got " + result);
        return result.error().orElseThrow().message();
    }

    public static JsonElement json(String text) {
        return JsonParser.parseString(text);
    }
}
