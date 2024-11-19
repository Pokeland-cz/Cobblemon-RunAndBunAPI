package net.ctengine.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;

import net.ctengine.api.CTEngine;

/**
 * Ensures pokemon entites from {@link Trainer}s are never saved to the world.
 */
@Mixin(PokemonEntity.class)
public abstract class PokemonEntityMixin {
    @Inject(method = "shouldBeSaved", at = @At("HEAD"), cancellable = true, remap = false)
    private void injectShouldBeSaved(CallbackInfoReturnable<Boolean> cir) {
        var self = (PokemonEntity)(Object)this;
        var ot = self.getPokemon().getOriginalTrainer();

        if(ot != null && CTEngine.getInstance().getTrainerRegistry().getById(ot) != null) {
            cir.setReturnValue(false);
        }
    }
}
