package net.ctengine.api.battle;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.battles.model.actor.AIBattleActor;
import com.cobblemon.mod.common.api.battles.model.actor.ActorType;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.battles.model.actor.EntityBackedBattleActor;
import com.cobblemon.mod.common.api.battles.model.ai.BattleAI;
import com.cobblemon.mod.common.battles.BattleSide;
import com.cobblemon.mod.common.battles.ErroredBattleStart;
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.pokemon.Pokemon;

import kotlin.Unit;
import net.ctengine.CTEngineMod;
import net.ctengine.api.trainer.Trainer;
import net.ctengine.api.trainer.TrainerNPC;
import net.ctengine.api.trainer.TrainerPlayer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import static com.cobblemon.mod.common.util.LocalizationUtilsKt.battleLang;

/**
 * A service to manage pokemon battles.
 */
public class BattleManager {
    private BattleContextValidator validator = new BattleContextValidator();

    /**
     * Starts a new pokemon battle.
     * 
     * @param participants1 List of trainer participants for one side.
     * @param participants2 List of trainer participants for the other side.
     * @param battleFormat Battle format to use.
     */
    public void startBattle(
        @NotNull List<Trainer> participants1,
        @NotNull List<Trainer> participants2,
        BattleFormat battleFormat)
    {
        var side1 = toBattleSide(participants1);
        var side2 = toBattleSide(participants2);
        var errors = validator.validate(new ErroredBattleStart(), new BattleContext(participants1, participants2, side1, side2, battleFormat));

        if(errors.isEmpty()) {
            Cobblemon.INSTANCE.getBattleRegistry().startBattle(
                battleFormat.getCobblemonBattleFormat(),
                side1, side2, false
            ).ifErrored(error -> {
                for(var participants : List.of(participants1, participants2)) {
                    for(var participant : participants) {
                        if(participant instanceof TrainerPlayer trainerPlayer) {
                            error.sendTo(trainerPlayer.getPlayer(), t -> t);
                        }
                    }
                }

                error.getErrors().forEach(e -> CTEngineMod.LOG.error(e.getMessageFor(null).getString()));
                return Unit.INSTANCE;
            }).ifSuccessful(battle -> {
                CTEngineMod.LOG.info("BATTLE START:");
                
                for(var act : battle.getActors()) {
                    CTEngineMod.LOG.info(" " + act.getName().getString());

                    for(var poke : act.getPokemonList()) {
                        CTEngineMod.LOG.info("  " + String.format("%s, %d: %d/%d", poke.getName(), poke.getOriginalPokemon().getLevel(), poke.getHealth(), poke.getMaxHealth()));
                    }
                }

                return Unit.INSTANCE;
            });
        } else {
            errors.getErrors().forEach(e -> CTEngineMod.LOG.error(e.getMessageFor(null).getString()));
        }
    }

    private static BattleSide toBattleSide(List<Trainer> participants) {
        var battleActors = new ArrayList<BattleActor>();

        for(var participant : participants) {
            if(participant instanceof TrainerPlayer trainerPlayer) {
                battleActors.add(toBattleActor(trainerPlayer));
            } else if(participant instanceof TrainerNPC aiParticipant) {
                battleActors.add(toBattleActor(aiParticipant));
            } else {
                // note: registering trainers with the TrainerRegistry will already check if battle
                // participants extend from TrainerPlayer or TrainerNPC and throw an exception if
                // not. This check is just and additional safety measure.
                CTEngineMod.LOG.error(String.format(
                    "invalid participant '%s', must extend from %s or %s, skipped",
                    participant.getName(), TrainerPlayer.class.getName(), TrainerNPC.class.getName()));
            }
        }

        return new BattleSide(battleActors.toArray(new BattleActor[battleActors.size()]));
    }

    private static List<BattlePokemon> toBattlePokemons(Pokemon... pokemons) {
        return toBattlePokemons(false, pokemons);
    }

    private static List<BattlePokemon> toBattlePokemons(boolean clone, Pokemon... pokemons) {
        var battlePokemons = new ArrayList<BattlePokemon>();

        for(var pokemon : pokemons) {
            if(!pokemon.isFainted()) {
                // TODO: mark as 'trainer owned'
                battlePokemons.add(clone
                    ? new BattlePokemon(pokemon, pokemon.clone(true), entity -> Unit.INSTANCE)
                    : new BattlePokemon(pokemon, pokemon, entity -> Unit.INSTANCE));
            }
        }

        return battlePokemons;
    }

    private static BattleActor toBattleActor(TrainerPlayer participant) {
        return new PlayerBattleActor(participant.getPlayer().getUuid(), toBattlePokemons(participant.getTeam()));
    }

    private static BattleActor toBattleActor(TrainerNPC participant) {
        return new TrainerEntityBattleActor(
            participant.getName(), participant.getEntity(),
            participant.getEntity().getUuid(),
            toBattlePokemons(true, participant.getTeam()),
            participant.getBattleAI());
    }

    private static class TrainerEntityBattleActor extends AIBattleActor implements EntityBackedBattleActor<LivingEntity> {
        private final String name;
        private final LivingEntity entity;

        public TrainerEntityBattleActor(
            String name,
            LivingEntity entity,
            UUID uuid,
            List<BattlePokemon> pokemonList,
            BattleAI artificialDecider)
        {
            super(uuid, pokemonList, artificialDecider);
            this.name = name;
            this.entity = entity;
        }

        @Override
        public LivingEntity getEntity() {
            return this.entity;
        }

        @Override @NotNull
        public ActorType getType() {
            return ActorType.NPC;
        }

        @Override @NotNull
        public MutableText getName() {
            return Text.literal(this.name);
        }

        @Override @NotNull
        public MutableText nameOwned(@NotNull String s) {
            return battleLang("owned_pokemon", getName(), this.name);
        }

        @Override @Nullable
        public Vec3d getInitialPos() {
            return this.entity.getPos();
        }
    }
}
