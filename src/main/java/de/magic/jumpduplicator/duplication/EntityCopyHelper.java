package de.magic.jumpduplicator.duplication;

import de.magic.jumpduplicator.config.JumpDuplicatorConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityProcessor;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.UUID;

public final class EntityCopyHelper {
    private static final double MIN_HORIZONTAL_BURST = 0.05D;
    private static final double MAX_HORIZONTAL_BURST = 0.18D;
    private static final double MIN_UPWARD_BURST = 0.12D;
    private static final double MAX_UPWARD_BURST = 0.28D;
    private EntityCopyHelper() {
    }

    public static Optional<Entity> copy(Entity original, ServerLevel level, Vec3 destination, JumpDuplicatorConfig config) {
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
        if (config.copyCustomNbt) {
            original.saveWithoutId(output);
        } else {
            original.save(output);
        }

        CompoundTag tag = output.buildResult();
        tag.putString("id", EntityType.getKey(original.getType()).toString());
        tag.remove("UUID");
        tag.remove("UUIDMost");
        tag.remove("UUIDLeast");
        tag.put("Pos", positionTag(destination));

        Optional<Entity> created = Optional.ofNullable(EntityType.loadEntityRecursive(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), tag), level, EntitySpawnReason.COMMAND, EntityProcessor.NOP));
        created.ifPresent(copy -> {
            copy.setUUID(UUID.randomUUID());
            copy.moveOrInterpolateTo(destination, original.getYRot(), original.getXRot());
            if (copy instanceof ItemEntity itemEntity && Boolean.TRUE.equals(config.duplicateOnlySingleItems)) {
                itemEntity.setItem(itemEntity.getItem().copyWithCount(1));
            }
            if (config.copyVelocity) {
                copy.setDeltaMovement(original.getDeltaMovement());
            } else {
                copy.setDeltaMovement(Vec3.ZERO);
            }
        });
        return created;
    }

    public static void applyRandomJumpBurst(Entity copy, ServerLevel level, JumpDuplicatorConfig config) {
        RandomSource random = level.getRandom();
        double angle = random.nextDouble() * Mth.TWO_PI;
        double horizontalSpeed = Mth.lerp(random.nextDouble(), MIN_HORIZONTAL_BURST, MAX_HORIZONTAL_BURST);
        double upwardSpeed = Mth.lerp(random.nextDouble(), MIN_UPWARD_BURST, MAX_UPWARD_BURST);
        Vec3 burst = new Vec3(Math.cos(angle) * horizontalSpeed, upwardSpeed, Math.sin(angle) * horizontalSpeed);
        Vec3 baseVelocity = config.copyVelocity && !(copy instanceof ItemEntity) ? copy.getDeltaMovement() : Vec3.ZERO;

        copy.setDeltaMovement(baseVelocity.add(burst));
        copy.hurtMarked = true;
    }

    public static Vec3 sameBlockDestination(Entity original, double verticalOffset) {
        return original.position().add(0.0D, verticalOffset, 0.0D);
    }

    public static Optional<Vec3> resolveDestination(Entity copy, ServerLevel level, Vec3 preferred, boolean safeSpawnCheck) {
        if (!level.hasChunkAt(BlockPos.containing(preferred))) {
            return Optional.empty();
        }

        copy.setPos(preferred);
        if (!safeSpawnCheck || isValidDestination(copy, level)) {
            return Optional.of(preferred);
        }

        BlockPos base = BlockPos.containing(preferred);
        for (int y = 0; y <= 3; y++) {
            for (int x = -3; x <= 3; x++) {
                for (int z = -3; z <= 3; z++) {
                    Vec3 candidate = Vec3.atBottomCenterOf(base.offset(x, y, z));
                    if (!level.hasChunkAt(BlockPos.containing(candidate))) {
                        continue;
                    }
                    copy.setPos(candidate);
                    if (isValidDestination(copy, level)) {
                        return Optional.of(candidate);
                    }
                }
            }
        }
        return Optional.empty();
    }

    private static boolean isValidDestination(Entity entity, ServerLevel level) {
        return level.getWorldBorder().isWithinBounds(entity.blockPosition()) && level.noCollision(entity);
    }

    private static ListTag positionTag(Vec3 position) {
        ListTag list = new ListTag();
        list.add(DoubleTag.valueOf(position.x));
        list.add(DoubleTag.valueOf(position.y));
        list.add(DoubleTag.valueOf(position.z));
        return list;
    }
}
