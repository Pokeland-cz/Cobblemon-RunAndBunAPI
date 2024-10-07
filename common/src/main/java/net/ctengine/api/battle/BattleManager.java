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
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor;
import com.cobblemon.mod.common.battles.actor.TrainerBattleActor;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.pokemon.Pokemon;

import kotlin.Unit;
import net.ctengine.CTEngineMod;
import net.ctengine.api.trainer.TrainerPlayer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import static com.cobblemon.mod.common.util.LocalizationUtilsKt.battleLang;

public class BattleManager {
    public void startBattle(
        @NotNull List<BattleParticipant> participants1,
        @NotNull List<BattleParticipant> participants2,
        BattleFormat battleFormat)
    {
        Cobblemon.INSTANCE.getBattleRegistry().startBattle(
            battleFormat.getCobblemonBattleFormat(),
            toBattleSide(participants1), toBattleSide(participants2),
            false
        ).ifErrored(error -> {
            for(var player : error.getPlayersToBlame()) {
                error.sendTo(player, t -> t);
            }

            // TODO: log on server
            return Unit.INSTANCE;
        }).ifSuccessful(battle -> {
            return Unit.INSTANCE;
        });
    }

    private static BattleSide toBattleSide(List<BattleParticipant> participants) {
        var battleActors = new ArrayList<BattleActor>();

        for(var participant : participants) {
            if(participant instanceof TrainerPlayer trainerPlayer) {
                battleActors.add(toBattleActor(trainerPlayer));
            } else if(participant instanceof AIBattleParticipant aiParticipant) {
                battleActors.add(toBattleActor(aiParticipant));
            } else {
                // note: registering trainers with the TrainerRegistry will already check if battle
                // participants extend from TrainerPlayer or implement AIBattleParticipant and
                // throw an exception if not. This check is just and additional security measure.
                CTEngineMod.LOG.error(String.format("invalid participant '%s', must extend from %s or implement %s, skipped", participant.getName(), TrainerPlayer.class.getName(), AIBattleParticipant.class.getName()));
            }
        }

        return new BattleSide(battleActors.toArray(new BattleActor[battleActors.size()]));
    }

    private static List<BattlePokemon> toBattlePokemons(Pokemon... pokemons) {
        var battlePokemons = new ArrayList<BattlePokemon>();

        for(var pokemon : pokemons) {
            if(!pokemon.isFainted()) {
                // TODO: mark as 'trainer owned'
                battlePokemons.add(new BattlePokemon(pokemon, pokemon, entity -> Unit.INSTANCE));
            }
        }

        return battlePokemons;
    }

    private static BattleActor toBattleActor(TrainerPlayer participant) {
        return new PlayerBattleActor(participant.getPlayer().getUuid(), toBattlePokemons(participant.getTeam()));
    }

    private static BattleActor toBattleActor(AIBattleParticipant participant) {
        return participant.getSourceEntity() != null
            ? new TrainerEntityBattleActor(participant.getName(), participant.getSourceEntity(), participant.getSourceEntity().getUuid(), toBattlePokemons(participant.getTeam()), participant.getBattleAI())
            : new TrainerBattleActor(participant.getName(), getParticipantUUID(participant), toBattlePokemons(participant.getTeam()), participant.getBattleAI());
    }

    private static UUID getParticipantUUID(BattleParticipant participant) {
        var entity = participant.getSourceEntity();
        return entity != null ? entity.getUuid() : UUID.randomUUID();
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
