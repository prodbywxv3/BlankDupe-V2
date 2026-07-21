package de.magic.jumpduplicator.duplication;

import de.magic.jumpduplicator.JumpDuplicatorMod;
import de.magic.jumpduplicator.config.ConfigManager;
import de.magic.jumpduplicator.config.JumpDuplicatorConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.fish.WaterAnimal;
import net.minecraft.world.entity.animal.golem.AbstractGolem;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.EnderDragonPart;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class EntityDuplicationService {
    private final ConfigManager configManager;

    public EntityDuplicationService(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public int duplicateNearbyEntities(ServerPlayer player) {
        JumpDuplicatorConfig config = configManager.get();
        if (!config.enabled) {
            player.sendSystemMessage(Component.literal("Jump Duplicator is disabled."));
            return 0;
        }

        ServerLevel level = player.level();
        double radius = config.radius;
        double radiusSquared = radius * radius;
        AABB box = player.getBoundingBox().inflate(radius);
        List<Entity> candidates = new ArrayList<>(level.getEntities(EntityTypeTest.forClass(Entity.class), box, entity -> shouldConsider(player, entity, config, radiusSquared)));

        int duplicated = 0;
        int limit = Math.min(config.maximumEntitiesPerJump, candidates.size() * config.duplicationAmount);
        for (Entity entity : candidates) {
            if (duplicated >= limit) {
                break;
            }

            for (int i = 0; i < config.duplicationAmount && duplicated < limit; i++) {
                Vec3 destination = EntityCopyHelper.sameBlockDestination(entity, config.verticalOffset);
                if (!level.hasChunkAt(BlockPos.containing(destination))) {
                    JumpDuplicatorMod.LOGGER.debug("Skipping {} because destination chunk is not loaded.", entity);
                    continue;
                }

                try {
                    Optional<Entity> created = EntityCopyHelper.copy(entity, level, destination, config);
                    if (created.isEmpty()) {
                        JumpDuplicatorMod.LOGGER.debug("Skipping {} because its entity type could not be copied.", entity);
                        continue;
                    }

                    Entity copy = created.get();
                    Optional<Vec3> resolvedDestination = EntityCopyHelper.resolveDestination(copy, level, destination, config.safeSpawnCheck);
                    if (resolvedDestination.isEmpty()) {
                        JumpDuplicatorMod.LOGGER.debug("Skipping {} because no valid destination was found.", entity);
                        continue;
                    }

                    Vec3 finalPosition = resolvedDestination.get();
                    copy.moveOrInterpolateTo(finalPosition, entity.getYRot(), entity.getXRot());
                    EntityCopyHelper.applyRandomJumpBurst(copy, level, config);
                    if (level.addFreshEntity(copy)) {
                        duplicated++;
                    } else {
                        JumpDuplicatorMod.LOGGER.debug("Skipping {} because the level rejected the copied entity.", entity);
                    }
                } catch (Exception exception) {
                    JumpDuplicatorMod.LOGGER.debug("Failed to duplicate entity {}", entity, exception);
                }
            }
        }
        return duplicated;
    }

    private boolean shouldConsider(ServerPlayer player, Entity entity, JumpDuplicatorConfig config, double radiusSquared) {
        if (entity == player || entity instanceof Player || entity instanceof EnderDragonPart) {
            return false;
        }
        if (entity.isRemoved() || !entity.isAlive()) {
            return false;
        }
        if (entity.distanceToSqr(player) > radiusSquared) {
            return false;
        }
        if (config.skipNamedEntities && entity.hasCustomName()) {
            return false;
        }
        if (config.skipBosses && (entity instanceof EnderDragon || entity instanceof WitherBoss)) {
            return false;
        }
        if (config.skipTamedEntities && isOwnedOrTamed(entity)) {
            return false;
        }
        return isEnabledEntityType(entity, config);
    }

    private boolean isEnabledEntityType(Entity entity, JumpDuplicatorConfig config) {
        if (entity instanceof ItemEntity) {
            return config.duplicateItems;
        }
        if (entity instanceof ExperienceOrb) {
            return config.duplicateExperienceOrbs;
        }
        if (entity instanceof Villager) {
            return config.duplicateVillagers;
        }
        if (entity instanceof Projectile) {
            return config.duplicateProjectiles;
        }
        if (entity instanceof AbstractMinecart || entity instanceof AbstractBoat) {
            return config.duplicateVehicles;
        }
        if (entity instanceof ArmorStand) {
            return config.duplicateArmorStands;
        }
        if (entity instanceof PrimedTnt) {
            return config.duplicateTnt;
        }
        if (entity instanceof FallingBlockEntity) {
            return config.duplicateFallingBlocks;
        }
        if (entity instanceof Enemy || entity instanceof Monster) {
            return config.duplicateHostileMobs;
        }
        if (entity instanceof Animal || entity instanceof AgeableMob || entity instanceof WaterAnimal || entity instanceof AbstractVillager || entity instanceof AbstractGolem || entity instanceof AbstractHorse || entity instanceof NeutralMob) {
            return config.duplicatePassiveMobs;
        }
        return config.duplicateOtherEntities;
    }

    private boolean isOwnedOrTamed(Entity entity) {
        if (entity instanceof TamableAnimal tamableAnimal && tamableAnimal.isTame()) {
            return true;
        }
        if (entity instanceof AbstractHorse horse && horse.isTamed()) {
            return true;
        }
        return entity instanceof OwnableEntity ownableEntity && ownableEntity.getOwnerReference() != null;
    }
}
