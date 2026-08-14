package br.mevis.kencraft.mixin;

import br.mevis.kencraft.client.JioPlayerAnimator;
import br.mevis.kencraft.data.JioAnimationData;
import br.mevis.kencraft.data.ModAttachments;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Applies KenCraft's Jio pose after vanilla PlayerModel.setupAnim has finished.
 * This is the key part that makes the animation visible in actual third-person
 * rendering: RenderPlayerEvent.Pre fires before vanilla prepares the model,
 * whereas this hook runs after that preparation.
 */
@Mixin(PlayerModel.class)
public abstract class PlayerModelMixin<T extends LivingEntity> {

    @Inject(method = "setupAnim", at = @At("TAIL"))
    private void kencraft$applyJioAnimation(T entity, float limbSwing, float limbSwingAmount,
                                              float ageInTicks, float netHeadYaw, float headPitch,
                                              CallbackInfo ci) {
        if (!(entity instanceof Player player) || !player.isAlive()) return;

        JioAnimationData animation = player.getData(ModAttachments.JIO_ANIMATION);
        long gameTime = player.level().getGameTime();
        JioPlayerAnimator.apply((PlayerModel<?>) (Object) this, player, animation, gameTime);
    }
}
