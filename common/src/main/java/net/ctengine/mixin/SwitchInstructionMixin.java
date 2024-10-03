package net.ctengine.mixin;

import com.cobblemon.mod.common.api.battles.interpreter.BattleMessage;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.battles.interpreter.instructions.SwitchInstruction;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import kotlin.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// This mixin fixes a bug in cobblemon.
// If a Pokemon has been switched in before by a trainer then their
// willBeSwitchedIn attribute is never reset so they cannot be switched in
// a second time.
@Mixin(SwitchInstruction.class)
public abstract class SwitchInstructionMixin {

    @Shadow(remap = false)
    public abstract BattleMessage getPublicMessage();

    @Inject(method = "invoke", at = @At("TAIL"), remap = false)
    private void injectInvoke(PokemonBattle battle, CallbackInfo ci) {
        Pair<String, String> pnxAndPokemonID = getPublicMessage().pnxAndUuid(0);
        if (pnxAndPokemonID == null) return;
        BattlePokemon battlePokemon = battle.getBattlePokemon(
                pnxAndPokemonID.component1(), pnxAndPokemonID.component2()
        );
        battlePokemon.setWillBeSwitchedIn(false);
    }

}