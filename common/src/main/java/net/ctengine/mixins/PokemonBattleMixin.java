package net.ctengine.mixins;

import org.spongepowered.asm.mixin.Mixin;

import com.cobblemon.mod.common.api.battles.model.PokemonBattle;

// "Borrowed" from selfdot
@Mixin(PokemonBattle.class)
public abstract class PokemonBattleMixin {
    // @Shadow(remap = false)
    // public abstract Iterable<BattleActor> getActors();

    // // This mixin triggers when a battle ends. If the battle actor is a trainer
    // // then we force them to recall the Pokemon. This will stop a
    // // leftover wild pokemon from spawning at the end of the battle.
    // @Inject(method = "end", at = @At("HEAD"), remap = false)
    // private void injectEnd(CallbackInfo ci) {
    //     getActors().forEach(actor -> {
    //         if (actor instanceof EntityBackerTrainerBattleActor trainerActor) {
    //             List<ActiveBattlePokemon> activeBattlePokemonList = trainerActor.getActivePokemon();
    //             if (activeBattlePokemonList.isEmpty()) return;
    //             BattlePokemon battlePokemon = activeBattlePokemonList.get(0).getBattlePokemon();
    //             if (battlePokemon == null) return;
    //             PokemonEntity pokemonEntity = battlePokemon.getEntity();
    //             if (pokemonEntity == null) return;
    //             pokemonEntity.recallWithAnimation();
    //         }

    //         // Players will no longer be in a battle so release them from the list
    //         actor.getPlayerUUIDs().forEach(BattleHandler.inTrainerBattle::remove);
    //     });
    // }
}