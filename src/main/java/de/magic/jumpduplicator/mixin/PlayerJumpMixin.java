package de.magic.jumpduplicator.mixin;

import de.magic.jumpduplicator.JumpDuplicatorMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class PlayerJumpMixin {
    @Inject(method = "jumpFromGround", at = @At("HEAD"))
    private void jumpduplicator$onJumpFromGround(CallbackInfo ci) {
        Object self = this;
        if (self instanceof ServerPlayer player && JumpDuplicatorMod.jumpDetector() != null) {
            JumpDuplicatorMod.jumpDetector().onPlayerJump(player);
        }
    }
}
