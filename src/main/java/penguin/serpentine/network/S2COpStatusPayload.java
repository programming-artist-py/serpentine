package penguin.serpentine.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record S2COpStatusPayload(boolean isOp) implements CustomPayload {

    public static final Id<S2COpStatusPayload> ID =
            new Id<>(Identifier.of("serpentine", "s2c_op_status"));

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static final PacketCodec<PacketByteBuf, S2COpStatusPayload> CODEC =
        PacketCodec.tuple(
                PacketCodecs.BOOL, S2COpStatusPayload::isOp,
                S2COpStatusPayload::new
        );

    public static void write(PacketByteBuf buf, S2COpStatusPayload payload) {
        buf.writeBoolean(payload.isOp);
    }

    public static S2COpStatusPayload read(PacketByteBuf buf) {
        return new S2COpStatusPayload(buf.readBoolean());
    }
}