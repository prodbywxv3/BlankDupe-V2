package de.magic.jumpduplicator.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import de.magic.jumpduplicator.config.ConfigManager;
import de.magic.jumpduplicator.config.JumpDuplicatorConfig;
import de.magic.jumpduplicator.duplication.EntityDuplicationService;
import de.magic.jumpduplicator.jump.CooldownManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.server.permissions.PermissionSet;

public final class JumpDuplicatorCommands {
    private JumpDuplicatorCommands() {
    }

    public static void register(ConfigManager configManager, CooldownManager cooldownManager, EntityDuplicationService duplicationService) {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            registerRoot(dispatcher, registryAccess, "jumpduplicator", configManager, cooldownManager, duplicationService);
            registerRoot(dispatcher, registryAccess, "jumpdupe", configManager, cooldownManager, duplicationService);
            registerRoot(dispatcher, registryAccess, "jd", configManager, cooldownManager, duplicationService);
        });
    }

    private static void registerRoot(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context, String rootName, ConfigManager configManager, CooldownManager cooldownManager, EntityDuplicationService duplicationService) {
        dispatcher.register(Commands.literal(rootName)
                .then(Commands.literal("help").executes(command -> help(command.getSource())))
                .then(Commands.literal("status").executes(command -> status(command.getSource(), configManager)))
                .then(Commands.literal("duplicate").executes(command -> duplicate(command.getSource(), duplicationService)))
                .then(Commands.literal("enable").requires(source -> isAdmin(source, configManager)).executes(command -> setEnabled(command.getSource(), configManager, true)))
                .then(Commands.literal("disable").requires(source -> isAdmin(source, configManager)).executes(command -> setEnabled(command.getSource(), configManager, false)))
                .then(Commands.literal("reload").requires(source -> isAdmin(source, configManager)).executes(command -> reload(command.getSource(), configManager, cooldownManager)))
                .then(Commands.literal("radius").requires(source -> isAdmin(source, configManager))
                        .then(Commands.argument("value", DoubleArgumentType.doubleArg(1.0D, 128.0D)).executes(command -> setRadius(command.getSource(), configManager, DoubleArgumentType.getDouble(command, "value")))))
                .then(Commands.literal("offset").requires(source -> isAdmin(source, configManager))
                        .then(Commands.argument("value", DoubleArgumentType.doubleArg(-64.0D, 64.0D)).executes(command -> setOffset(command.getSource(), configManager, DoubleArgumentType.getDouble(command, "value")))))
                .then(Commands.literal("amount").requires(source -> isAdmin(source, configManager))
                        .then(Commands.argument("value", IntegerArgumentType.integer(1, 10)).executes(command -> setAmount(command.getSource(), configManager, IntegerArgumentType.getInteger(command, "value")))))
                .then(Commands.literal("cooldown").requires(source -> isAdmin(source, configManager))
                        .then(Commands.argument("ticks", IntegerArgumentType.integer(0, 72000)).executes(command -> setCooldown(command.getSource(), configManager, IntegerArgumentType.getInteger(command, "ticks")))))
                .then(Commands.literal("maxentities").requires(source -> isAdmin(source, configManager))
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1, 1000)).executes(command -> setMaxEntities(command.getSource(), configManager, IntegerArgumentType.getInteger(command, "amount")))))
                .then(Commands.literal("notify").requires(source -> isAdmin(source, configManager))
                        .then(Commands.argument("value", BoolArgumentType.bool()).executes(command -> setNotify(command.getSource(), configManager, BoolArgumentType.getBool(command, "value"))))));
    }

    private static boolean isAdmin(CommandSourceStack source, ConfigManager configManager) {
        return hasPermissionLevel(source.permissions(), configManager.get().permissionLevel);
    }

    public static boolean hasPermissionLevel(PermissionSet permissions, int level) {
        if (permissions instanceof LevelBasedPermissionSet levelBasedPermissionSet) {
            return levelBasedPermissionSet.level().isEqualOrHigherThan(PermissionLevel.byId(level));
        }
        return level <= 0;
    }

    private static int help(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("Jump Duplicator commands: /jumpduplicator status, duplicate, enable, disable, reload, radius <1-128>, offset <-64-64>, amount <1-10>, cooldown <ticks>, maxentities <1-1000>, notify <true|false>"), false);
        return 1;
    }

    private static int status(CommandSourceStack source, ConfigManager configManager) {
        JumpDuplicatorConfig config = configManager.get();
        source.sendSuccess(() -> Component.literal("Jump Duplicator status:"), false);
        source.sendSuccess(() -> Component.literal("enabled=" + config.enabled
                + ", radius=" + config.radius
                + ", verticalOffset=" + config.verticalOffset
                + ", duplicationAmount=" + config.duplicationAmount
                + ", cooldownTicks=" + config.cooldownTicks
                + ", maximumEntitiesPerJump=" + config.maximumEntitiesPerJump), false);
        source.sendSuccess(() -> Component.literal("duplicateItems=" + config.duplicateItems
                + ", duplicateOnlySingleItems=" + config.duplicateOnlySingleItems
                + ", duplicateExperienceOrbs=" + config.duplicateExperienceOrbs
                + ", duplicatePassiveMobs=" + config.duplicatePassiveMobs
                + ", duplicateHostileMobs=" + config.duplicateHostileMobs
                + ", duplicateVillagers=" + config.duplicateVillagers
                + ", duplicateProjectiles=" + config.duplicateProjectiles), false);
        source.sendSuccess(() -> Component.literal("duplicateVehicles=" + config.duplicateVehicles
                + ", duplicateArmorStands=" + config.duplicateArmorStands
                + ", duplicateTnt=" + config.duplicateTnt
                + ", duplicateFallingBlocks=" + config.duplicateFallingBlocks
                + ", duplicateOtherEntities=" + config.duplicateOtherEntities), false);
        source.sendSuccess(() -> Component.literal("copyVelocity=" + config.copyVelocity
                + ", copyCustomNbt=" + config.copyCustomNbt
                + ", requirePermission=" + config.requirePermission
                + ", permissionLevel=" + config.permissionLevel
                + ", notifyPlayer=" + config.notifyPlayer), false);
        source.sendSuccess(() -> Component.literal("skipBosses=" + config.skipBosses
                + ", skipTamedEntities=" + config.skipTamedEntities
                + ", skipNamedEntities=" + config.skipNamedEntities
                + ", safeSpawnCheck=" + config.safeSpawnCheck), false);
        return 1;
    }

    private static int duplicate(CommandSourceStack source, EntityDuplicationService duplicationService) {
        ServerPlayer player;
        try {
            player = source.getPlayerOrException();
        } catch (Exception exception) {
            source.sendFailure(Component.literal("Only a player can manually duplicate nearby entities."));
            return 0;
        }

        int duplicated = duplicationService.duplicateNearbyEntities(player);
        source.sendSuccess(() -> Component.literal("Duplicated " + duplicated + " nearby entities."), false);
        return duplicated;
    }

    private static int setEnabled(CommandSourceStack source, ConfigManager configManager, boolean enabled) {
        JumpDuplicatorConfig config = configManager.get();
        config.enabled = enabled;
        save(configManager);
        source.sendSuccess(() -> Component.literal(enabled ? "Jump Duplicator enabled." : "Jump Duplicator disabled."), true);
        return 1;
    }

    private static int reload(CommandSourceStack source, ConfigManager configManager, CooldownManager cooldownManager) {
        configManager.load();
        cooldownManager.clear();
        source.sendSuccess(() -> Component.literal("Jump Duplicator configuration reloaded."), true);
        return 1;
    }

    private static int setRadius(CommandSourceStack source, ConfigManager configManager, double value) {
        configManager.get().radius = value;
        save(configManager);
        source.sendSuccess(() -> Component.literal("Jump Duplicator radius set to " + configManager.get().radius + " blocks."), true);
        return 1;
    }

    private static int setOffset(CommandSourceStack source, ConfigManager configManager, double value) {
        configManager.get().verticalOffset = value;
        save(configManager);
        source.sendSuccess(() -> Component.literal("Jump Duplicator vertical offset set to " + configManager.get().verticalOffset + " blocks."), true);
        return 1;
    }

    private static int setAmount(CommandSourceStack source, ConfigManager configManager, int value) {
        configManager.get().duplicationAmount = value;
        save(configManager);
        source.sendSuccess(() -> Component.literal("Jump Duplicator duplication amount set to " + configManager.get().duplicationAmount + "."), true);
        return 1;
    }

    private static int setCooldown(CommandSourceStack source, ConfigManager configManager, int ticks) {
        configManager.get().cooldownTicks = ticks;
        save(configManager);
        source.sendSuccess(() -> Component.literal("Jump Duplicator cooldown set to " + configManager.get().cooldownTicks + " ticks."), true);
        return 1;
    }

    private static int setMaxEntities(CommandSourceStack source, ConfigManager configManager, int amount) {
        configManager.get().maximumEntitiesPerJump = amount;
        save(configManager);
        source.sendSuccess(() -> Component.literal("Jump Duplicator maximum entities per jump set to " + configManager.get().maximumEntitiesPerJump + "."), true);
        return 1;
    }

    private static int setNotify(CommandSourceStack source, ConfigManager configManager, boolean value) {
        configManager.get().notifyPlayer = value;
        save(configManager);
        source.sendSuccess(() -> Component.literal("Jump Duplicator jump notifications set to " + value + "."), true);
        return 1;
    }

    private static void save(ConfigManager configManager) {
        configManager.get().validate();
        configManager.save();
    }
}
