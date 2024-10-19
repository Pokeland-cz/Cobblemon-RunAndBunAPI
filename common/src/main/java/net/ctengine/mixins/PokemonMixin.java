package net.ctengine.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.cobblemon.mod.common.pokemon.Pokemon;

import net.ctengine.api.trainer.Trainer;
import net.ctengine.api.util.Trainers;
import net.minecraft.world.entity.LivingEntity;

@Mixin(Pokemon.class)
public abstract class PokemonMixin {
    @Inject(method = "getOwnerEntity", at = @At("RETURN"), cancellable = true, remap = false)
    private void injectGetOwnerEntity(CallbackInfoReturnable<LivingEntity> cir) {
        if(cir.getReturnValue() == null) {
            var self = (Pokemon)(Object)this;
            var ot = self.getOriginalTrainer();
            Trainer npc;

            if(ot != null && (npc = Trainers.getByStringUUID(ot)) != null) {
                cir.setReturnValue(npc.getEntity());
            }
        }
    }
}
