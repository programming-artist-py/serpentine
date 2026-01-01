package penguin.serpentine.network;

import java.util.HashMap;
import java.util.Map;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import penguin.serpentine.core.Config;
import penguin.serpentine.core.Serpentine;

public class ConfigNetworking {

    public static final Identifier S2C_OP_STATUS = Identifier.of("serpentine", "s2c_op_status");
    private static boolean payloadsRegistered = false;
    
    public static void registerPayloadTypes() {
        if (payloadsRegistered) return;
        payloadsRegistered = true;

        PayloadTypeRegistry.playC2S().register(
                C2SRequestConfigChangePayload.ID,
                C2SRequestConfigChangePayload.CODEC
        );

        PayloadTypeRegistry.playS2C().register(
                S2CSyncConfigPayload.ID,
                S2CSyncConfigPayload.CODEC
        );

        PayloadTypeRegistry.playS2C().register(
                S2COpStatusPayload.ID,
                S2COpStatusPayload.CODEC
        );
    }

    public static void sendS2COpStatus(ServerPlayerEntity player, boolean isOp) {
        S2COpStatusPayload packet = new S2COpStatusPayload(
            player.hasPermissionLevel(2)
        );
        ServerPlayNetworking.send(player, packet);
    }

    public static void registerC2SHandlers() {
        ServerPlayNetworking.registerGlobalReceiver(
                C2SRequestConfigChangePayload.ID,
                (payload, context) -> {
                    context.server().execute(() -> {
                        var player = context.player();
                        Config cfg = Serpentine.getConfig(payload.modId());
                        if (cfg == null) return;

                        if (!player.hasPermissionLevel(2)) return;

                        cfg.applyServerEdit(
                                player,
                                payload.key(),
                                payload.value()
                        );
                    });
                }
        );
    }
    public static void sendS2CSync(ServerPlayerEntity player, Config cfg, boolean includeServerOnly) {
        if (cfg == null) {
            penguin.serpentine.Serpentine.LOGGER.warn("Attempted to send S2C sync for null config: skipping.");
            return; // avoid crash
        }
        Map<String, String> values = new HashMap<>(cfg.getServerSyncableValues());
        if (includeServerOnly) values.putAll(cfg.getServerOnlyValues());
        S2CSyncConfigPayload packet = new S2CSyncConfigPayload(cfg.getModId(), values);
        ServerPlayNetworking.send(player, packet);
    }
}