package de.magic.jumpduplicator.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import de.magic.jumpduplicator.JumpDuplicatorMod;
import de.magic.jumpduplicator.config.ConfigManager;
import de.magic.jumpduplicator.config.JumpDuplicatorConfig;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.DoubleSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class JumpDuplicatorModMenuIntegration implements ModMenuApi {

    private static final JumpDuplicatorConfig DEFAULTS = new JumpDuplicatorConfig();

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return this::buildScreen;
    }

    private Screen buildScreen(Screen parent) {
        ConfigManager configManager = JumpDuplicatorMod.configManager();
        JumpDuplicatorConfig config = configManager.get();

        return YetAnotherConfigLib.createBuilder()
                .title(Component.literal("Jump Duplicator"))
                .category(ConfigCategory.createBuilder()
                        .name(Component.literal("General"))
                        .option(bool("Enabled", "Master switch for the whole mod.",
                                DEFAULTS.enabled, () -> config.enabled, v -> config.enabled = v))
                        .option(bool("Notify Player", "Sends a chat message to the player when entities are duplicated.",
                                DEFAULTS.notifyPlayer, () -> config.notifyPlayer, v -> config.notifyPlayer = v))
                        .group(OptionGroup.createBuilder()
                                .name(Component.literal("Permission"))
                                .option(bool("Require Permission", "Only players with the configured permission level may trigger duplication.",
                                        DEFAULTS.requirePermission, () -> config.requirePermission, v -> config.requirePermission = v))
                                .option(intSlider("Permission Level", "Minecraft operator permission level required (0-4).",
                                        DEFAULTS.permissionLevel, () -> config.permissionLevel, v -> config.permissionLevel = v, 0, 4))
                                .build())
                        .build())
                .category(ConfigCategory.createBuilder()
                        .name(Component.literal("Duplication"))
                        .option(doubleSlider("Radius", "Search radius (blocks) used to find nearby entities on jump.",
                                DEFAULTS.radius, () -> config.radius, v -> config.radius = v, 1.0D, 128.0D))
                        .option(doubleSlider("Vertical Offset", "Vertical offset (blocks) applied to duplicated entities.",
                                DEFAULTS.verticalOffset, () -> config.verticalOffset, v -> config.verticalOffset = v, -64.0D, 64.0D))
                        .option(intSlider("Duplication Amount", "How many copies to spawn per matched entity.",
                                DEFAULTS.duplicationAmount, () -> config.duplicationAmount, v -> config.duplicationAmount = v, 1, 10))
                        .option(intSlider("Cooldown Ticks", "Ticks a player must wait between duplication jumps.",
                                DEFAULTS.cooldownTicks, () -> config.cooldownTicks, v -> config.cooldownTicks = v, 0, 72000))
                        .option(intSlider("Max Entities Per Jump", "Upper bound on entities duplicated in a single jump.",
                                DEFAULTS.maximumEntitiesPerJump, () -> config.maximumEntitiesPerJump, v -> config.maximumEntitiesPerJump = v, 1, 1000))
                        .option(bool("Copy Velocity", "Duplicated entities keep the original's velocity.",
                                DEFAULTS.copyVelocity, () -> config.copyVelocity, v -> config.copyVelocity = v))
                        .option(bool("Copy Custom NBT", "Duplicated entities keep custom NBT data (enchantments, names, etc).",
                                DEFAULTS.copyCustomNbt, () -> config.copyCustomNbt, v -> config.copyCustomNbt = v))
                        .option(bool("Safe Spawn Check", "Avoids spawning duplicates inside blocks or unsafe locations.",
                                DEFAULTS.safeSpawnCheck, () -> config.safeSpawnCheck, v -> config.safeSpawnCheck = v))
                        .build())
                .category(ConfigCategory.createBuilder()
                        .name(Component.literal("Entity Filters"))
                        .option(bool("Items", "Allow duplicating item entities.",
                                DEFAULTS.duplicateItems, () -> config.duplicateItems, v -> config.duplicateItems = v))
                        .option(bool("Only Single-Stack Items", "Only duplicate item stacks with a single item.",
                                DEFAULTS.duplicateOnlySingleItems, () -> config.duplicateOnlySingleItems, v -> config.duplicateOnlySingleItems = v))
                        .option(bool("Experience Orbs", "Allow duplicating experience orbs.",
                                DEFAULTS.duplicateExperienceOrbs, () -> config.duplicateExperienceOrbs, v -> config.duplicateExperienceOrbs = v))
                        .option(bool("Passive Mobs", "Allow duplicating passive mobs.",
                                DEFAULTS.duplicatePassiveMobs, () -> config.duplicatePassiveMobs, v -> config.duplicatePassiveMobs = v))
                        .option(bool("Hostile Mobs", "Allow duplicating hostile mobs.",
                                DEFAULTS.duplicateHostileMobs, () -> config.duplicateHostileMobs, v -> config.duplicateHostileMobs = v))
                        .option(bool("Villagers", "Allow duplicating villagers.",
                                DEFAULTS.duplicateVillagers, () -> config.duplicateVillagers, v -> config.duplicateVillagers = v))
                        .option(bool("Projectiles", "Allow duplicating projectiles.",
                                DEFAULTS.duplicateProjectiles, () -> config.duplicateProjectiles, v -> config.duplicateProjectiles = v))
                        .option(bool("Vehicles", "Allow duplicating boats, minecarts and other vehicles.",
                                DEFAULTS.duplicateVehicles, () -> config.duplicateVehicles, v -> config.duplicateVehicles = v))
                        .option(bool("Armor Stands", "Allow duplicating armor stands.",
                                DEFAULTS.duplicateArmorStands, () -> config.duplicateArmorStands, v -> config.duplicateArmorStands = v))
                        .option(bool("TNT", "Allow duplicating primed TNT.",
                                DEFAULTS.duplicateTnt, () -> config.duplicateTnt, v -> config.duplicateTnt = v))
                        .option(bool("Falling Blocks", "Allow duplicating falling blocks.",
                                DEFAULTS.duplicateFallingBlocks, () -> config.duplicateFallingBlocks, v -> config.duplicateFallingBlocks = v))
                        .option(bool("Other Entities", "Allow duplicating entities that don't match any other category.",
                                DEFAULTS.duplicateOtherEntities, () -> config.duplicateOtherEntities, v -> config.duplicateOtherEntities = v))
                        .build())
                .category(ConfigCategory.createBuilder()
                        .name(Component.literal("Safety"))
                        .option(bool("Skip Bosses", "Never duplicate boss entities (e.g. Ender Dragon, Wither).",
                                DEFAULTS.skipBosses, () -> config.skipBosses, v -> config.skipBosses = v))
                        .option(bool("Skip Tamed Entities", "Never duplicate tamed entities.",
                                DEFAULTS.skipTamedEntities, () -> config.skipTamedEntities, v -> config.skipTamedEntities = v))
                        .option(bool("Skip Named Entities", "Never duplicate entities with a custom name.",
                                DEFAULTS.skipNamedEntities, () -> config.skipNamedEntities, v -> config.skipNamedEntities = v))
                        .build())
                .save(() -> {
                    config.validate();
                    configManager.save();
                })
                .build()
                .generateScreen(parent);
    }

    private static Option<Boolean> bool(String name, String tooltip, boolean defaultValue, Supplier<Boolean> getter, Consumer<Boolean> setter) {
        return Option.<Boolean>createBuilder()
                .name(Component.literal(name))
                .description(OptionDescription.of(Component.literal(tooltip)))
                .binding(defaultValue, getter, setter)
                .controller(TickBoxControllerBuilder::create)
                .build();
    }

    private static Option<Integer> intSlider(String name, String tooltip, int defaultValue, Supplier<Integer> getter, Consumer<Integer> setter, int min, int max) {
        return Option.<Integer>createBuilder()
                .name(Component.literal(name))
                .description(OptionDescription.of(Component.literal(tooltip)))
                .binding(defaultValue, getter, setter)
                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(min, max).step(1))
                .build();
    }

    private static Option<Double> doubleSlider(String name, String tooltip, double defaultValue, Supplier<Double> getter, Consumer<Double> setter, double min, double max) {
        return Option.<Double>createBuilder()
                .name(Component.literal(name))
                .description(OptionDescription.of(Component.literal(tooltip)))
                .binding(defaultValue, getter, setter)
                .controller(opt -> DoubleSliderControllerBuilder.create(opt).range(min, max).step(0.5))
                .build();
    }
}
