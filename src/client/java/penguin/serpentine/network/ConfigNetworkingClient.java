package penguin.serpentine.network;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;

public class ConfigNetworkingClient {

    public static boolean isOp = false;
    public static final Map<String, Map<String, String>> serverConfigCache = new HashMap<>();


    public static void sendC2SRequest(String modId, String key, String value) {
        ClientPlayNetworking.send(
                new C2SRequestConfigChangePayload(modId, key, value)
        );
    }

    public static void registerS2C() {
        ClientPlayNetworking.registerGlobalReceiver(
            S2CSyncConfigPayload.ID,
            (payload, context) -> {
                MinecraftClient.getInstance().execute(() -> {
                    var config = penguin.serpentine.core.Serpentine.getConfig(payload.modId());
                    if (config != null) {
                        config.applyServerSync(payload.values());
                        serverConfigCache.put(payload.modId(), new LinkedHashMap<>(payload.values()));
                    }
                });
            }
        );
        ClientPlayNetworking.registerGlobalReceiver(
                S2COpStatusPayload.ID,
                (payload, context) -> {
                    ConfigNetworkingClient.isOp = payload.isOp();
                }
        );
    }
}