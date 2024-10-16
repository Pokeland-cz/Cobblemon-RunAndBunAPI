package net.ctengine.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;

@Mixin(PokemonBattle.class)
public abstract class PokemonBattleMixin {
    @Shadow(remap = false)
    public abstract Iterable<BattleActor> getActors();

    // This mixin triggers when a battle ends. If the battle actor is a trainer then we
    // force them to recall the Pokemon. This will stop a leftover wild pokemon from
    // spawning at the end of the battle.
    // @Inject(method = "end", at = @At("HEAD"), remap = false)
    // private void injectEnd(CallbackInfo ci) {
    //     getActors().forEach(actor -> {
    //         if(actor instanceof AIBattleActor aiActor) {
    //             aiActor.getActivePokemon().forEach(pkmn -> pkmn.getBattlePokemon().getEntity().recallWithAnimation());
    //         }
    //     });
    // }
}