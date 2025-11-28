package penguin.serpentine.network;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public class SerpentineServerTicker {

    private static int tickCounter = 0;
    private static final int TICKS_BETWEEN_UPDATES = 20 * 30; // 30 seconds at 20 TPS

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(SerpentineServerTicker::onServerTick);
    }

    private static void onServerTick(MinecraftServer server) {
        tickCounter++;
        if (tickCounter >= TICKS_BETWEEN_UPDATES) {
            tickCounter = 0;
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                ConfigNetworking.sendS2COpStatus(player, player.hasPermissionLevel(2));
            }
        }
    }
}
