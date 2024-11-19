package net.ctengine.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.cobblemon.mod.common.pokemon.Pokemon;

import net.ctengine.api.RCTApi;
import net.ctengine.api.trainer.Trainer;
import net.minecraft.world.entity.LivingEntity;

/**
 * Serves as hook between Cobblemons pokemon/trainer relation and trainers
 * registered by this api.
 */
@Mixin(Pokemon.class)
public abstract class PokemonMixin {
    @Inject(method = "getOwnerEntity", at = @At("RETURN"), cancellable = true, remap = false)
    private void injectGetOwnerEntity(CallbackInfoReturnable<LivingEntity> cir) {
        if(cir.getReturnValue() == null) {
            var self = (Pokemon)(Object)this;
            var ot = self.getOriginalTrainer();
            Trainer npc;

            if(ot != null && (npc = RCTApi.getInstance().getTrainerRegistry().getById(ot)) != null) {
                cir.setReturnValue(npc.getEntity());
            }
        }
    }
}
