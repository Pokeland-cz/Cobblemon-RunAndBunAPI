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
import net.ctengine.api.trainer.TrainerBag;
import net.ctengine.api.trainer.TrainerNPC;
import net.ctengine.api.trainer.TrainerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import static com.cobblemon.mod.common.util.LocalizationUtilsKt.battleLang;

/**
 * A service to manage pokemon battles.
 */
public class BattleManager {
    private BattleContextValidator validator = new BattleContextValidator();

    /**
     * Starts a new pokemon battle. Potential errors that may occur at the start or
     * during a battle a sent to all participating players.
     * 
     * @param participants1 List of trainer participants for one side.
     * @param participants2 List of trainer participants for the other side.
     * @param battleFormat Battle format to use.
     * @return True if a battle was started.
     */
    public boolean start(
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
                // TODO: how to log on server?
                sendErrors(error, participants1, participants2);
                return Unit.INSTANCE;
            }).ifSuccessful(battle -> Unit.INSTANCE);
        } else {
            // TODO: how to log on server?
            sendErrors(errors, participants1, participants2);
            return false;
        }

        return true;
    }

    private static void sendErrors(ErroredBattleStart errors, List<Trainer> participants1, List<Trainer> participants2) {
        for(var participants : List.of(participants1, participants2)) {
            for(var participant : participants) {
                if(participant instanceof TrainerPlayer trainerPlayer) {
                    errors.sendTo(trainerPlayer.getPlayer(), t -> t);
                }
            }
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
                    "invalid participant '%s', must extend from %s or %s",
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
                battlePokemons.add(clone
                    ? new BattlePokemon(pokemon, pokemon.clone(true), entity -> { entity.recallWithAnimation(); return Unit.INSTANCE; })
                    : new BattlePokemon(pokemon, pokemon, entity -> Unit.INSTANCE));
            }
        }

        return battlePokemons;
    }

    private static BattleActor toBattleActor(TrainerPlayer participant) {
        return new PlayerBattleActor(participant.getPlayer().getUUID(), toBattlePokemons(participant.getTeam()));
    }

    private static BattleActor toBattleActor(TrainerNPC participant) {
        return new TrainerEntityBattleActor(
            participant.getName(), participant.getEntity(),
            participant.getEntity().getUUID(),
            toBattlePokemons(true, participant.getTeam()),
            participant.getBag().clone(),
            participant.getBattleAI());
    }

    // TODO: move to different package
    public static class TrainerEntityBattleActor extends AIBattleActor implements EntityBackedBattleActor<LivingEntity> {
        private final String name;
        private final LivingEntity entity;
        private final TrainerBag bag;

        public TrainerEntityBattleActor(
            String name,
            LivingEntity entity,
            UUID uuid,
            List<BattlePokemon> pokemonList,
            TrainerBag bag,
            BattleAI artificialDecider)
        {
            super(uuid, pokemonList, artificialDecider);
            this.name = name;
            this.bag = bag;
            this.entity = entity;
        }

        @NotNull
        public TrainerBag getBag() {
            return this.bag;
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
        public MutableComponent getName() {
            return Component.literal(this.name);
        }

        @Override @NotNull
        public MutableComponent nameOwned(@NotNull String s) {
            return battleLang("owned_pokemon", getName(), this.name);
        }

        @Override @Nullable
        public Vec3 getInitialPos() {
            return this.entity.position();
        }
    }
}
