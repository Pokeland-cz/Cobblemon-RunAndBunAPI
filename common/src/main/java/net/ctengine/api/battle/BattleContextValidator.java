package net.ctengine.api.battle;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import com.cobblemon.mod.common.api.battles.model.actor.EntityBackedBattleActor;
import com.cobblemon.mod.common.battles.AlreadyInBattleError;
import com.cobblemon.mod.common.battles.BattleRegistry;
import com.cobblemon.mod.common.battles.BattleStartError;
import com.cobblemon.mod.common.battles.ErroredBattleStart;

import net.minecraft.network.chat.Component;

/**
 * A service that is used by battle managers to validate the technical correctness
 * of battle contexts.
 */
public class BattleContextValidator {
    /**
     * Validates the given battle context and collects any errors that may occur. In
     * addition to checking for the correct amount of actors, party size, etc - in
     * accordance to the defined battle format - there is also the constraint that all
     * {@link BattleActor}s must implement {@link EntityBackedBattleActor} and may not
     * return null when retrieving the attached entity with {@link EntityBackedBattleActor#getEntity()}.
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
                if(actor instanceof EntityBackedBattleActor entityBacked && entityBacked.getEntity() != null) {
                    if(actor.getPokemonList().size() < slotsPerActor) {
                        errors.getParticipantErrors().get(actor).add(BattleStartError.Companion.insufficientPokemon(entityBacked.getEntity(), slotsPerActor, actor.getPokemonList().size()));
                    }

                    if(BattleRegistry.INSTANCE.getBattleByParticipatingPlayerId(entityBacked.getEntity().getUUID()) != null) {
                        errors.getParticipantErrors().get(actor).add(AlreadyInBattleError.Companion.alreadyInBattle(actor));
                    }

                    if(actorIds.contains(actor.getUuid())) {
                        errors.getParticipantErrors().get(actor).add(AlreadyInBattleError.Companion.alreadyInBattle(actor));
                    } else {
                        actorIds.add(actor.getUuid());
                    }
                } else {
                    errors.getParticipantErrors().get(actor).add(BattleStartError.Companion.canceledByEvent(Component.literal(String.format("%s is not attached to an entity", actor.getName().getString()))));
                }
            }
        }

        return errors;
    }
}
