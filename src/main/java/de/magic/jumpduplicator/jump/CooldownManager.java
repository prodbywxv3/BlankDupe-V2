package de.magic.jumpduplicator.jump;

import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class CooldownManager {
    private final Map<UUID, Long> lastActivationTick = new HashMap<>();

    public boolean isReady(ServerPlayer player, int cooldownTicks) {
        if (cooldownTicks <= 0) {
            return true;
        }
        long now = player.level().getGameTime();
        Long lastTick = lastActivationTick.get(player.getUUID());
        return lastTick == null || now - lastTick >= cooldownTicks;
    }

    public void markUsed(ServerPlayer player) {
        lastActivationTick.put(player.getUUID(), player.level().getGameTime());
    }

    public void clear() {
        lastActivationTick.clear();
    }
}
