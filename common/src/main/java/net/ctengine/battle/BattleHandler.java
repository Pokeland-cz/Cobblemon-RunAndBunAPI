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
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BattleHandler {
    // Just track if they are already in a battle so they can't initiate another one
    public static final List<UUID> inTrainerBattle = new ArrayList<>();

    public static void requestTrainerBattle(ServerPlayerEntity serverPlayer, Trainer trainer, LivingEntity trainerEntity){
        if (inTrainerBattle.contains(serverPlayer.getUuid())) return;

        // Check if the trainer is set to be only beatable once.
        // If they can only be beaten once and the trainer has already beaten them
        // then don't allow them to battle.
        if (trainer.getCanOnlyBeatOnce() &&
                WinRegistry.getWin(trainer.getId(), serverPlayer.getUuid())
        ) {
            serverPlayer.sendMessage(Text.literal(Formatting.RED + "You have already beaten this trainer!"));
            return;
        }

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
                // Start the battle with the listeners so we can update WinRegistry and fire any
                // win/loss commands
                .ifSuccessful(battle -> {
                    TrainerBattleListener.addOnBattleVictory(battle, trainer);
                    TrainerBattleListener.addOnBattleLoss(battle, trainer);
                    inTrainerBattle.add(serverPlayer.getUuid());
                    return Unit.INSTANCE;
                });
    }

    public static BattleStartResult startTrainerBattle(ServerPlayerEntity serverPlayer, Trainer trainer, LivingEntity trainerEntity, PlayerPartyStore party, UUID leadingPokemon){
        BattleFormat battleFormat = BattleFormat.Companion.getGEN_9_SINGLES();

        // Initiate player actor with leading pokemon
        BattleActor playerBattleActor = new PlayerBattleActor(
                serverPlayer.getUuid(), party.toBattleTeam(false, true, leadingPokemon)
        );

        // Initiate trainer team and actor
        BattleAI battleAI = new Gen5AI();
        List<BattlePokemon> battleTeam = trainer.getBattleTeam();

        ErroredBattleStart errors = new ErroredBattleStart();
        Set<BattleStartError> playerErrors = errors.getParticipantErrors().get(playerBattleActor);

        // Selfdot said that if the AI battle actor is not entity backed then cobblemon will not allow
        // the battle to happen. I tested this and could not get a battle to work unless the
        // AI battle actor was entity backed
        // Probably should handle this better then allowing it to go through as it softlocks
        BattleActor trainerBattleActor = trainerEntity == null ?
                new TrainerBattleActor(
                        trainer.getDisplayName(), UUID.randomUUID(), battleTeam, battleAI
                ) :
                new EntityBackerTrainerBattleActor(
                        trainer.getDisplayName(), trainerEntity, UUID.randomUUID(), battleTeam, battleAI
                );

        // Check against current battle format, maybe relevant when more battle formats are added.
        if (playerBattleActor.getPokemonList().size() < battleFormat.getBattleType().getSlotsPerActor()) {
            playerErrors.add(BattleStartError.Companion.insufficientPokemon(
                    serverPlayer,
                    battleFormat.getBattleType().getSlotsPerActor(),
                    playerBattleActor.getPokemonList().size()
            ));
        }

        // Check the defeat requirements and give error if they haven't defeated the proper trainers
        List<String> trainersNotDefeatedIdList = new ArrayList<>();
        for (String defeatRequirementTrainerId : trainer.getDefeatRequirements()){
            if (!WinRegistry.getWin(defeatRequirementTrainerId, serverPlayer.getUuid())){
                trainersNotDefeatedIdList.add(defeatRequirementTrainerId);
            }
        }
        if (!trainersNotDefeatedIdList.isEmpty()) playerErrors.add(new TrainersNotDefeatedError(trainersNotDefeatedIdList));

        // Don't let them battle if they are already in a battle.
        if (Cobblemon.INSTANCE.getBattleRegistry().getBattleByParticipatingPlayer(serverPlayer) != null) {
            playerErrors.add(BattleStartError.Companion.alreadyInBattle(serverPlayer));
        }

        // Don't let them battle if trainer has no pokemon as it causes a softlock
        if (trainer.getBattleTeam().isEmpty()) {
            playerErrors.add(entity -> Text.literal("Trainer " + trainer.getDisplayName() + " has no Pokémon."));
        }

        // Only start battle if none of the above errors were triggered
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
