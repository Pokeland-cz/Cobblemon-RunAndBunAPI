package net.ctengine.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;

import net.ctengine.api.util.Battles;

/**
 * Restricts usage of bag items based on a configurable limits per battle.
 * 
 * @see BagItemInstructionMixin
 */
@Mixin(BattleActor.class)
public class BattleActorMixin {
    // This is the only place I could figure to prevent the usage of items for any
    // battle actors. By the looks of it it shouldn't have any other than the desired
    // effect (especially since this injection only is effective in trainer battles
    // started by this api due to the check for a known battle state).
    @Inject(method = "canFitForcedAction", at = @At("RETURN"), cancellable = true, remap = false)
    private void injectCanFitForcedAction(CallbackInfoReturnable<Boolean> cir) {
        var self = (BattleActor)(Object)this;
        var battleState = Battles.getState(self.battle.getBattleId());
        
        if(cir.getReturnValue() && battleState != null) {
            var maxItems = battleState.getRules().getMaxItemUses();
            
            if(maxItems >= 0) {
                var actorState = battleState.getState(self.getUuid());
                cir.setReturnValue(actorState.getItemsUsed() < maxItems);
            }
        }
    }
}
