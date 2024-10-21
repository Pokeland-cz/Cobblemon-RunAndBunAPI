package net.ctengine.mixins;

import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.battles.interpreter.instructions.SwitchInstruction;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// "Borrowed" from selfdot
// This mixin fixes a bug in cobblemon.
// If a Pokemon has been switched in before by a trainer then their
// willBeSwitchedIn attribute is never reset, so they cannot be switched in
// a second time. This will cause a softlock if not fixed.
@Mixin(SwitchInstruction.class)
public abstract class SwitchInstructionMixin {
    @Shadow(remap = false)
    public abstract BattleMessage getPublicMessage();

    @Inject(method = "invoke", at = @At("TAIL"), remap = false)
    private void injectInvoke(PokemonBattle battle, CallbackInfo ci) {
        var pnxAndPokemonID = getPublicMessage().pnxAndUuid(0);

        if(pnxAndPokemonID != null) {
            battle
                .getBattlePokemon(pnxAndPokemonID.component1(), pnxAndPokemonID.component2())
                .setWillBeSwitchedIn(false);
        }
    }
}