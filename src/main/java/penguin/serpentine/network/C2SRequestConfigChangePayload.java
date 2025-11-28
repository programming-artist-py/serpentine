package penguin.serpentine.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record C2SRequestConfigChangePayload (
        String modId,
        String key,
        String value
) implements CustomPayload {

    public static final Id<C2SRequestConfigChangePayload> ID =
            new Id<>(Identifier.of("serpentine", "c2s_request_config_change"));

    public static final PacketCodec<PacketByteBuf, C2SRequestConfigChangePayload> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.STRING, C2SRequestConfigChangePayload::modId,
                    PacketCodecs.STRING, C2SRequestConfigChangePayload::key,
                    PacketCodecs.STRING, C2SRequestConfigChangePayload::value,
                    C2SRequestConfigChangePayload::new
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
