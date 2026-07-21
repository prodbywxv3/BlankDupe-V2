package de.magic.jumpduplicator.config;

public final class JumpDuplicatorConfig {
    public boolean enabled = true;
    public double radius = 16.0D;
    public double verticalOffset = 1.0D;
    public int duplicationAmount = 1;
    public int cooldownTicks = 1;
    public int maximumEntitiesPerJump = 1000;
    public boolean duplicateItems = true;
    public Boolean duplicateOnlySingleItems = true;
    public boolean duplicateExperienceOrbs = true;
    public boolean duplicatePassiveMobs = true;
    public boolean duplicateHostileMobs = true;
    public boolean duplicateVillagers = true;
    public boolean duplicateProjectiles = true;
    public boolean duplicateVehicles = true;
    public boolean duplicateArmorStands = true;
    public boolean duplicateTnt = true;
    public boolean duplicateFallingBlocks = true;
    public boolean duplicateOtherEntities = true;
    public boolean copyVelocity = true;
    public boolean copyCustomNbt = true;
    public boolean requirePermission = false;
    public int permissionLevel = 2;
    public boolean notifyPlayer = false;
    public boolean skipBosses = true;
    public boolean skipTamedEntities = false;
    public boolean skipNamedEntities = false;
    public boolean safeSpawnCheck = true;

    public void validate() {
        radius = clampFinite(radius, 16.0D, 1.0D, 128.0D);
        verticalOffset = clampFinite(verticalOffset, 1.0D, -64.0D, 64.0D);
        duplicationAmount = clamp(duplicationAmount, 1, 10);
        cooldownTicks = clamp(cooldownTicks, 0, 72000);
        maximumEntitiesPerJump = clamp(maximumEntitiesPerJump, 1, 1000);
        permissionLevel = clamp(permissionLevel, 0, 4);
        if (duplicateOnlySingleItems == null) {
            duplicateOnlySingleItems = true;
        }
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double clampFinite(double value, double fallback, double min, double max) {
        if (!Double.isFinite(value)) {
            return fallback;
        }
        return Math.max(min, Math.min(max, value));
    }
}
