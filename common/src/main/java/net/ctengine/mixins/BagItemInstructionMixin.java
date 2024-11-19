package net.ctengine.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.battles.interpreter.instructions.BagItemInstruction;

import net.ctengine.api.RCTApi;

/**
 * Required to keep track of the number of items used by actors per battle.
 * 
 * @see BattleActorMixin
 */
@Mixin(BagItemInstruction.class)
public abstract class BagItemInstructionMixin {
    @Inject(method = "invoke", at = @At("TAIL"), remap = false)
    private void injectInvoke(PokemonBattle battle, CallbackInfo ci) {
        var self = (BagItemInstruction)(Object)this;
        var battleState = RCTApi.getInstance().getBattleManager().getState(battle.getBattleId());

        if(battleState != null) {
            var message = self.getMessage();
            var pkmn = message.pokemonByUuid(0, battle);

            if(pkmn != null) {
                var actor = pkmn.getActor();
                var actorState = battleState.getState(actor.getUuid());
                actorState.setItemsUsed(actorState.getItemsUsed() + 1);
            }
        }
    }
}