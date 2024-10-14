package net.ctengine.api.battle;

import static com.cobblemon.mod.common.util.LocalizationUtilsKt.battleLang;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import com.cobblemon.mod.common.battles.BattleStartError;
import com.cobblemon.mod.common.battles.ErroredBattleStart;
import net.minecraft.entity.Entity;
import net.minecraft.text.MutableText;

/**
 * A service that is used by battle managers to validate the technical correctness
 * of battle contexts.
 */
public class BattleContextValidator {
    // placeholder (see todo below)
    class InsufficientPokemonError implements BattleStartError {
        public final int requiredCount;
        public final int hadCount;

        InsufficientPokemonError(int requiredCount, int hadCount) {
            this.requiredCount = requiredCount;
            this.hadCount = hadCount;
        }

        @Override
        public MutableText getMessageFor(Entity arg0) {
            return battleLang(
                "error.insufficient_pokemon", "trainer",
                this.hadCount, this.requiredCount);
        }
    }

    /**
     * An error that occurs if a battle actor appears more than once in a battle
     * context.
     */
    class DuplicateActorError implements BattleStartError {
        public final String actorName;

        DuplicateActorError(String actorName) {
            this.actorName = actorName;
        }

        // TODO: lang file? :/
        @Override
        public MutableText getMessageFor(Entity arg0) {
            return battleLang("error.duplicate_actor", actorName);
        }
    }

    /**
     * Validates the given battle context and collects any errors that may occur.
     * 
     * @param errors Battle start result to collect errors.
     * @param context Battle context.
     * @return The provided battle start result instance.
     */
    public ErroredBattleStart validate(ErroredBattleStart errors, BattleContext context) {
        var actorsPersSide = context.getBattleFormat().getCobblemonBattleFormat().component2().getActorsPerSide();
        var slotsPerActor = context.getBattleFormat().getCobblemonBattleFormat().component2().getSlotsPerActor();
        var actorIds = new HashSet<UUID>();

        for(var side : List.of(context.getBattleSide1(), context.getBattleSide2())) {
            if(side.getActors().length != actorsPersSide) {
                errors.getGeneralErrors().add(BattleStartError.Companion.incorrectActorCount(actorsPersSide, side.getActors().length));
            }

            for(var actor : side.getActors()) {
                if(actor.getPokemonList().size() < slotsPerActor) {
                    // TODO: custom error that does not require an entity (or rather associate actor to participant to gain access to the entity)
                    // errors.getParticipantErrors().get(actor).add(BattleStartError.Companion.insufficientPokemon(null, slotsPerActor, actor.getPokemonList().size()));
                    errors.getParticipantErrors().get(actor).add(new InsufficientPokemonError(slotsPerActor, actor.getPokemonList().size()));
                }

                if(actorIds.contains(actor.getUuid())) {
                    errors.getParticipantErrors().get(actor).add(new DuplicateActorError(actor.getName().getString()));
                } else {
                    actorIds.add(actor.getUuid());
                }
            }
        }

        return errors;
    }
}
