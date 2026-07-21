package de.magic.jumpduplicator.jump;

import de.magic.jumpduplicator.command.JumpDuplicatorCommands;
import de.magic.jumpduplicator.config.ConfigManager;
import de.magic.jumpduplicator.config.JumpDuplicatorConfig;
import de.magic.jumpduplicator.duplication.EntityDuplicationService;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.entity.player.Abilities;

public final class JumpDetector {
    private final ConfigManager configManager;
    private final CooldownManager cooldownManager;
    private final EntityDuplicationService duplicationService;

    public JumpDetector(ConfigManager configManager, CooldownManager cooldownManager, EntityDuplicationService duplicationService) {
        this.configManager = configManager;
        this.cooldownManager = cooldownManager;
        this.duplicationService = duplicationService;
    }

    public void onPlayerJump(ServerPlayer player) {
        JumpDuplicatorConfig config = configManager.get();
        if (!canActivate(player, config)) {
            return;
        }

        cooldownManager.markUsed(player);
        int duplicated = duplicationService.duplicateNearbyEntities(player);
        if (config.notifyPlayer) {
            player.sendSystemMessage(Component.translatable("text.jumpduplicator.duplicated", duplicated));
        }
    }

    public boolean canActivate(ServerPlayer player, JumpDuplicatorConfig config) {
        if (!config.enabled || player.level().isClientSide()) {
            return false;
        }
        if (!player.onGround()) {
            return false;
        }
        Abilities abilities = player.getAbilities();
        if (abilities.flying || player.isSpectator() || player.isSwimming() || player.onClimbable() || player.isPassenger() || player.isSleeping()) {
            return false;
        }
        if (config.requirePermission) {
            if (!JumpDuplicatorCommands.hasPermissionLevel(player.permissions(), config.permissionLevel)) {
                return false;
            }
        } else {
            GameType gameType = player.gameMode.getGameModeForPlayer();
            if (gameType != GameType.SURVIVAL && gameType != GameType.ADVENTURE) {
                return false;
            }
        }
        return cooldownManager.isReady(player, config.cooldownTicks);
    }
}
