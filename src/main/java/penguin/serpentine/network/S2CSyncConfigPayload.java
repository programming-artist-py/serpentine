package penguin.serpentine.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public record S2CSyncConfigPayload(
        String modId,
        Map<String, String> values
) implements CustomPayload {

    public static final Id<S2CSyncConfigPayload> ID =
            new Id<>(Identifier.of("serpentine", "s2c_sync_config"));

    public static final PacketCodec<PacketByteBuf, S2CSyncConfigPayload> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.STRING, S2CSyncConfigPayload::modId,
                    PacketCodecs.map(HashMap::new, PacketCodecs.STRING, PacketCodecs.STRING),
                    S2CSyncConfigPayload::values,
                    S2CSyncConfigPayload::new
            );


    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}