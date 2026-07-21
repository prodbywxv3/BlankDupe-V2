package de.magic.jumpduplicator;

import de.magic.jumpduplicator.command.JumpDuplicatorCommands;
import de.magic.jumpduplicator.config.ConfigManager;
import de.magic.jumpduplicator.duplication.EntityDuplicationService;
import de.magic.jumpduplicator.jump.CooldownManager;
import de.magic.jumpduplicator.jump.JumpDetector;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JumpDuplicatorMod implements ModInitializer {
    public static final String MOD_ID = "jumpduplicator";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static ConfigManager configManager;
    private static CooldownManager cooldownManager;
    private static EntityDuplicationService duplicationService;
    private static JumpDetector jumpDetector;

    @Override
    public void onInitialize() {
        configManager = new ConfigManager();
        configManager.load();

        cooldownManager = new CooldownManager();
        duplicationService = new EntityDuplicationService(configManager);
        jumpDetector = new JumpDetector(configManager, cooldownManager, duplicationService);

        JumpDuplicatorCommands.register(configManager, cooldownManager, duplicationService);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> cooldownManager.clear());
    }

    public static JumpDetector jumpDetector() {
        return jumpDetector;
    }

    public static ConfigManager configManager() {
        return configManager;
    }
}
