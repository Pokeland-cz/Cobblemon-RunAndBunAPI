package net.ctengine.battle;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.battles.model.ai.BattleAI;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.battles.*;
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor;
import com.cobblemon.mod.common.battles.actor.TrainerBattleActor;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.pokemon.Pokemon;
import kotlin.Unit;
import net.ctengine.CTEngine;
import net.ctengine.trainer.Trainer;
import net.ctengine.trainer.WinRegistry;
import net.ctengine.trainer.ai.Gen5AI;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BattleHandler {
    public static void requestTrainerBattle(ServerPlayerEntity serverPlayer, Trainer trainer, LivingEntity trainerEntity){
        PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(serverPlayer);

        // Get first alive pokemon in players party as the leading pokemon
        // If none are alive then don't allow battle to go through otherwise it soft locks
        UUID leadingPokemon = null;
        for (Pokemon pokemon : party) {
            if (!pokemon.isFainted()) {
                leadingPokemon = pokemon.getUuid();
                break;
            }
        }
        if (leadingPokemon == null) {
            serverPlayer.sendMessage(Text.of("You have no alive Pokemon to battle with"));
            return;
        }

        startTrainerBattle(serverPlayer, trainer, trainerEntity, party, leadingPokemon).ifErrored(error -> {
                    error.sendTo(serverPlayer, t -> t);
                    return Unit.INSTANCE;
                })
                .ifSuccessful(battle -> {
                    TrainerBattleListener.addOnBattleVictory(battle, trainer);
                    TrainerBattleListener.addOnBattleLoss(battle, trainer);
                    return Unit.INSTANCE;
                });
    }

    public static BattleStartResult startTrainerBattle(ServerPlayerEntity serverPlayer, Trainer trainer, LivingEntity trainerEntity, PlayerPartyStore party, UUID leadingPokemon){
        BattleFormat battleFormat = BattleFormat.Companion.getGEN_9_SINGLES();

        BattleActor playerBattleActor = new PlayerBattleActor(
                serverPlayer.getUuid(), party.toBattleTeam(false, true, leadingPokemon)
        );

        BattleAI battleAI = new Gen5AI();
        List<BattlePokemon> battleTeam = trainer.getBattleTeam();
        CTEngine.LOGGER.info("Battle Team: "+battleTeam+", size: "+battleTeam.size());

        ErroredBattleStart errors = new ErroredBattleStart();
        Set<BattleStartError> playerErrors = errors.getParticipantErrors().get(playerBattleActor);

        BattleActor trainerBattleActor = trainerEntity == null ?
                new TrainerBattleActor(
                        trainer.getDisplayName(), UUID.randomUUID(), battleTeam, battleAI
                ) :
                new EntityBackerTrainerBattleActor(
                        trainer.getDisplayName(), trainerEntity, UUID.randomUUID(), battleTeam, battleAI
                );

        if (playerBattleActor.getPokemonList().size() < battleFormat.getBattleType().getSlotsPerActor()) {
            playerErrors.add(BattleStartError.Companion.insufficientPokemon(
                    serverPlayer,
                    battleFormat.getBattleType().getSlotsPerActor(),
                    playerBattleActor.getPokemonList().size()
            ));
        }

        List<String> trainersNotDefeatedIdList = new ArrayList<>();
        for (String defeatRequirementTrainerId : trainer.getDefeatRequirements()){
            if (!WinRegistry.getWin(defeatRequirementTrainerId, serverPlayer.getUuid())){
                trainersNotDefeatedIdList.add(defeatRequirementTrainerId);
            }
        }
        if (!trainersNotDefeatedIdList.isEmpty()) playerErrors.add(new TrainersNotDefeatedError(trainersNotDefeatedIdList));

        if (Cobblemon.INSTANCE.getBattleRegistry().getBattleByParticipatingPlayer(serverPlayer) != null) {
            playerErrors.add(BattleStartError.Companion.alreadyInBattle(serverPlayer));
        }

        if (trainer.getBattleTeam().isEmpty()) {
            playerErrors.add(entity -> Text.literal("Trainer " + trainer.getDisplayName() + " has no Pokémon."));
        }

        if (errors.isEmpty()) {
            return Cobblemon.INSTANCE.getBattleRegistry().startBattle(
                    BattleFormat.Companion.getGEN_9_SINGLES(),
                    new BattleSide(playerBattleActor),
                    new BattleSide(trainerBattleActor),
                    false
            );
        }

        return errors;
    }
}
