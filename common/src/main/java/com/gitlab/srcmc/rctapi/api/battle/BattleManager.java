/*
 * This file is part of Radical Cobblemon Trainers API.
 * Copyright (c) 2025, HDainester, All rights reserved.
 *
 * Radical Cobblemon Trainers API is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Radical Cobblemon Trainers API is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Radical Cobblemon Trainers API. If not, see <http://www.gnu.org/licenses/lgpl>.
 */
package com.gitlab.srcmc.rctapi.api.battle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.CobblemonSounds;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
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
import com.gitlab.srcmc.rctapi.ModCommon;
import com.gitlab.srcmc.rctapi.api.events.EventContext;
import com.gitlab.srcmc.rctapi.api.events.Events;
import com.gitlab.srcmc.rctapi.api.trainer.Trainer;
import com.gitlab.srcmc.rctapi.api.trainer.TrainerBag;
import com.gitlab.srcmc.rctapi.api.trainer.TrainerNPC;
import com.gitlab.srcmc.rctapi.api.trainer.TrainerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import static com.cobblemon.mod.common.util.LocalizationUtilsKt.battleLang;

/**
 * A service to manage {@link PokemonBattle}s.
 */
public class BattleManager {
    private BattleContextValidator validator = new BattleContextValidator();
    private Map<UUID, BattleState> battleStates = new HashMap<>();
    private BattleRules defaultRules = new BattleRules();
    private EventContext eventContext;

    /**
     * Constructs a new {@link BattleManager} with its own {@link EventContext}.
     */
    public BattleManager() {
        this(new EventContext());
    }

    /**
     * Constructs a new {@link BattleManager} for the given {@link EventContext}.
     * 
     * @param eventContext {@link EventContext} used by the {@link BattleManager}.
     */
    public BattleManager(EventContext eventContext) {
        this.eventContext = eventContext;
    }

    /**
     * Starts a new {@link PokemonBattle} in the 'GEN 9 Singles' format with default
     * {@link BattleRules} (see {@link BattleManager#setDefaultRules(BattleRules)}).
     * 
     * @param participant1 First participating {@link Trainer}.
     * @param participant2 Second participating {@link Trainer}.
     * @return True if a battle was started.
     * @see also {@link BattleManager#start(List, List, BattleFormat, BattleRules)}
     */
    public boolean startSingle(
        @NotNull Trainer participant1,
        @NotNull Trainer participant2)
    {
        return this.startSingle(participant1, participant2, this.getDefaultRules());
    }

    /**
     * Starts a new {@link PokemonBattle} in the 'GEN 9 Singles' format with the given
     * {@link BattleRules}.
     * 
     * @param participant1 First participating {@link Trainer}.
     * @param participant2 Second participating {@link Trainer}.
     * @param battleRules {@link BattleRules} applied to the battle.
     * @return True if a battle was started.
     * @see also {@link BattleManager#start(List, List, BattleFormat, BattleRules)}
     */
    public boolean startSingle(
        @NotNull Trainer participant1,
        @NotNull Trainer participant2,
        @NotNull BattleRules battleRules)
    {
        return this.start(List.of(participant1), List.of(participant2), BattleFormat.GEN_9_SINGLES, battleRules);
    }

    /**
     * Starts a new {@link PokemonBattle} in the 'GEN 9 Doubles' format with default
     * {@link BattleRules} (see {@link BattleManager#setDefaultRules(BattleRules)}).
     * 
     * @param participant1 First participating {@link Trainer}.
     * @param participant2 Second participating {@link Trainer}.
     * @return True if a battle was started.
     * @see also {@link BattleManager#start(List, List, BattleFormat, BattleRules)}
     */
    public boolean startDouble(@NotNull Trainer participant1, @NotNull Trainer participant2) {
        return this.startDouble(participant1, participant2, this.getDefaultRules());
    }

    /**
     * Starts a new {@link PokemonBattle} in the 'GEN 9 Doubles' format with the given
     * {@link BattleRules}.
     * 
     * @param participant1 First participating {@link Trainer}.
     * @param participant2 Second participating {@link Trainer}.
     * @param battleRules {@link BattleRules} applied to the battle.
     * @return True if a battle was started.
     * @see also {@link BattleManager#start(List, List, BattleFormat, BattleRules)}
     */
    public boolean startDouble(
        @NotNull Trainer participant1,
        @NotNull Trainer participant2,
        @NotNull BattleRules battleRules)
    {
        return this.start(List.of(participant1), List.of(participant2), BattleFormat.GEN_9_DOUBLES, battleRules);
    }

    /**
     * Starts a new {@link PokemonBattle} in the 'GEN 9 Triples' format with default
     * {@link BattleRules} (see {@link BattleManager#setDefaultRules(BattleRules)}).
     * 
     * @param participant1 First participating {@link Trainer}.
     * @param participant2 Second participating {@link Trainer}.
     * @return True if a battle was started.
     * @see also {@link BattleManager#start(List, List, BattleFormat, BattleRules)}
     */
    public boolean startTriple(@NotNull Trainer participant1, @NotNull Trainer participant2) {
        return this.startTriple(participant1, participant2, this.getDefaultRules());
    }

    /**
     * Starts a new {@link PokemonBattle} in the 'GEN 9 Triples' format with the given
     * {@link BattleRules}.
     * 
     * @param participant1 First participating {@link Trainer}.
     * @param participant2 Second participating {@link Trainer}.
     * @param battleRules {@link BattleRules} applied to the battle.
     * @return True if a battle was started.
     * @see also {@link BattleManager#start(List, List, BattleFormat, BattleRules)}
     */
    public boolean startTriple(
        @NotNull Trainer participant1,
        @NotNull Trainer participant2,
        @NotNull BattleRules battleRules)
    {
        return this.start(List.of(participant1), List.of(participant2), BattleFormat.GEN_9_TRIPLES, battleRules);
    }

    /**
     * Starts a new {@link PokemonBattle} in the 'GEN 9 Multi' format with default
     * {@link BattleRules} (see {@link BattleManager#setDefaultRules(BattleRules)}).
     * 
     * @param participant1_l Participating {@link Trainer} for the first team on the left side.
     * @param participant2_r Participating {@link Trainer} for the first team on the right side.
     * @param participant1_l Participating {@link Trainer} for the second team on the left side.
     * @param participant2_r Participating {@link Trainer} for the second team on the right side.
     * @return True if a battle was started.
     * @see also {@link BattleManager#start(List, List, BattleFormat, BattleRules)}
     */
    public boolean startMulti(
        @NotNull Trainer participant1_l,
        @NotNull Trainer participant1_r,
        @NotNull Trainer participant2_l,
        @NotNull Trainer participant2_r)
    {
        return this.startMulti(participant1_l, participant1_r, participant2_l, participant2_r, this.getDefaultRules());
    }


    /**
     * Starts a new {@link PokemonBattle} in the 'GEN 9 Multi' format with the given
     * {@link BattleRules}.
     * 
     * @param participant1_l Participating {@link Trainer} for the first team on the left side.
     * @param participant2_r Participating {@link Trainer} for the first team on the right side.
     * @param participant1_l Participating {@link Trainer} for the second team on the left side.
     * @param participant2_r Participating {@link Trainer} for the second team on the right side.
     * @param battleRules {@link BattleRules} applied to the battle.
     * @return True if a battle was started.
     * @see also {@link BattleManager#start(List, List, BattleFormat, BattleRules)}
     */
    public boolean startMulti(
        @NotNull Trainer participant1_l,
        @NotNull Trainer participant1_r,
        @NotNull Trainer participant2_l,
        @NotNull Trainer participant2_r,
        @NotNull BattleRules battleRules)
    {
        return this.start(List.of(participant1_l, participant1_r), List.of(participant2_l, participant2_r), BattleFormat.GEN_9_MULTI, battleRules);
    }

    /**
     * Starts a new {@link PokemonBattle}. Potential errors that may occur at the start
     * or during a battle are sent to all participating players.
     * 
     * Note: The first participant in participants1 must be a player!
     * 
     * @param participants1 List of {@link Trainer} participants for one side.
     * @param participants2 List of {@link Trainer} participants for the other side.
     * @param battleFormat {@link BattleFormat} to use.
     * @param battleRules {@link BattleRules} enforced on the battle.
     * @return True if a battle was started.
     */
    public boolean start(
        @NotNull List<Trainer> participants1,
        @NotNull List<Trainer> participants2,
        @NotNull BattleFormat battleFormat,
        @NotNull BattleRules battleRules)
    {
        var side1 = toBattleSide(participants1);
        var side2 = toBattleSide(participants2);
        var errors = this.validator.validate(new ErroredBattleStart(), new BattleContext(participants1, participants2, side1, side2, battleFormat));

        if(errors.isEmpty()) {
            Cobblemon.INSTANCE.getBattleRegistry().startBattle(
                battleFormat.getCobblemonBattleFormat(),
                side1, side2, false
            ).ifErrored(error -> {
                ModCommon.LOG.error("Failed to start battle: " + toBattleArgsString(participants1, participants2));
                sendErrors(error, participants1, participants2);
                return Unit.INSTANCE;
            }).ifSuccessful(battle -> {
                this.eventContext.fire(Events.BATTLE_STARTED.create(this.battleStates.put(battle.getBattleId(), new BattleState(battle, battleFormat, battleRules, participants1, participants2))));
                return Unit.INSTANCE;
            });
        } else {
            ModCommon.LOG.error("Failed to validate battle: " + toBattleArgsString(participants1, participants2));
            sendErrors(errors, participants1, participants2);
            return false;
        }

        return true;
    }

    /**
     * Finishes and unregisters a previously started {@link PokemonBattle} that has
     * ended and fires a {@link Events#BATTLE_ENDED} event on success. Does nothing if
     * the {@link PokemonBattle} has not ended or was not registered by this {@link
     * BattleManager}.
     * 
     * @param battleId Id of the {@link PokemonBattle} to finish and unregister.
     * @return True if the {@link PokemonBattle} was finished and unregistered.
     */
    public boolean end(UUID battleId) {
        return this.end(battleId, false);
    }

    /**
     * Finishes and unregisters a previously started {@link PokemonBattle} that has
     * ended. Does nothing if the {@link PokemonBattle} has not ended or was not
     * registered by this {@link BattleManager}.
     * 
     * @param battleId Id of the {@link PokemonBattle} to finish and unregister.
     * @param forced If true the {@link PokemonBattle} will be forcefully stopped if it
     * has not ended. A {@link Events#BATTLE_ENDED} event will not be fired in that
     * case.
     * @return True if the {@link PokemonBattle} was finished and unregistered.
     */
    public boolean end(UUID battleId, boolean forced) {
        var state = this.battleStates.get(battleId);

        if(state != null) {
            var battle = state.getBattle();

            if(forced || battle == null || battle.getEnded()) {
                this.battleStates.remove(battleId);

                if(battle != null && battle.getEnded()) {
                    this.eventContext.fire(Events.BATTLE_ENDED.create(state));
                } else if(forced) {
                    state.getBattle().stop(); // should force tie
                }

                return true;
            }
        }

        return false;
    }

    /**
     * Retrieves the configured default {@link BattleRules}.
     * 
     * @return Default {@link BattleRules}.
     */
    @NotNull
    public BattleRules getDefaultRules() {
        return this.defaultRules;
    }

    /**
     * Sets the default {@link BattleRules}.
     * 
     * @param defaultRules {@link BattleRules} to be used as new default.
     */
    public void setDefaultRules(@NotNull BattleRules defaultRules) {
        this.defaultRules = defaultRules;
    }

    /**
     * Retrieves the {@link BattleState} for an ongoing {@link PokemonBattle} that was
     * previously started with {@link BattleManager#start(List, List, BattleFormat, BattleRules)}.
     * 
     * @param battleId UUID of the {@link PokemonBattle}.
     * @return The {@link BattleState} or null of no such battle is active.
     */
    public BattleState getState(UUID battleId) {
        return this.battleStates.get(battleId);
    }

    /**
     * Retrieves a unmodiable collection of all {@link BattleState}s for an ongoing
     * {@link PokemonBattle}s that were previously started with {@link
     * BattleManager#start(List, List, BattleFormat, BattleRules)}.
     * 
     * @return Collection of active {@link BattleState}s.
     */
    public Collection<BattleState> getStates() {
        return this.battleStates.values();
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
                ModCommon.LOG.error(String.format(
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
        var actor = new PlayerBattleActor(participant.getPlayer().getUUID(), toBattlePokemons(participant.getTeam()));
        actor.setBattleTheme(CobblemonSounds.PVN_BATTLE);
        return actor;
    }

    private static BattleActor toBattleActor(TrainerNPC participant) {
        return new TrainerEntityBattleActor(
            participant.getName(), participant.getEntity(),
            participant.getEntity().getUUID(),
            toBattlePokemons(true, participant.getTeam()),
            participant.getBag().clone(),
            participant.getBattleAI());
    }

    private static String toBattleArgsString(List<Trainer> participants1, List<Trainer> participants2) {
        var sb = new StringBuilder();

        if(participants1.size() > 0) {
            sb.append(participants1.getFirst().getName());
        }

        participants1.stream().skip(1).forEach(p -> sb.append(", ").append(p.getName()));
        sb.append(" vs ");

        if(participants2.size() > 0) {
            sb.append(participants2.getFirst().getName());
        }

        participants2.stream().skip(1).forEach(p -> sb.append(", ").append(p.getName()));
        return sb.toString();
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

        @Override
        public Vec3 getInitialPos() {
            return this.entity.position();
        }
    }
}
