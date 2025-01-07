/*
 * This file is part of Radical Cobblemon Trainers API.
 * Copyright (c) 2024, HDainester, All rights reserved.
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
package com.gitlab.srcmc.rctapi.api.trainer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.gitlab.srcmc.rctapi.api.errors.RCTErrors;
import com.gitlab.srcmc.rctapi.api.errors.RCTException;
import com.gitlab.srcmc.rctapi.api.models.TrainerModel;
import com.gitlab.srcmc.rctapi.api.models.converter.PokemonModelConverter;
import com.gitlab.srcmc.rctapi.api.models.converter.TrainerModelConverter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

/**
 * Key/value trainer storage.
 */
public class TrainerRegistry {
    private Map<String, Trainer> trainers = new HashMap<>();
    private Set<String> trainerNPCs = new HashSet<>();
    private Set<String> trainerPlayers = new HashSet<>();
    private TrainerModelConverter tmc;
    private String id;
    
    public TrainerRegistry() {
        this("");
    }

    public TrainerRegistry(String id) {
        this.id = id;
    }

    /**
     * Initializes and clears the trainer registry.
     * 
     * @param server Minecraft server.
     */
    public void init(@NotNull MinecraftServer server) {
        this.tmc = new TrainerModelConverter(server, new PokemonModelConverter());
        this.clear();
    }

    /**
     * Instantiates and registers a {@link TrainerPlayer} to the trainer registry.
     * 
     * @param trainerId Unique id of the {@link TrainerPlayer} to register.
     * @param player {@link ServerPlayer} to associate with the trainer.
     * @return Registered {@link TrainerPlayer} instance.
     * @throws IllegalArgumentException If a {@link Trainer} with the given id is already registered.
     */
    @NotNull
    public TrainerPlayer registerPlayer(@NotNull String trainerId, @NotNull ServerPlayer player) {
        return registerPlayer(trainerId, new TrainerPlayer(player));
    }

    /**
     * Registers the given {@link TrainerPlayer} to the trainer registry.
     * 
     * @param <T> {@link TrainerPlayer} type.
     * @param trainerId Unique id of the {@link TrainerPlayer} to register.
     * @param trainer {@link TrainerPlayer} instance to register.
     * @return Registered {@link TrainerPlayer} instance.
     * @throws IllegalArgumentException If a {@link Trainer} with the given id is already registered.
     */
    @NotNull
    public <T extends TrainerPlayer> T registerPlayer(@NotNull String trainerId, @NotNull T trainer) {
        this.register(trainerId, trainer);
        this.trainerPlayers.add(trainerId);
        return trainer;
    }

    /**
     * Instantiates and registers a {@link TrainerNPC} to the trainer registry. Uses a
     * default {@link PokemonModelConverter} to create the pokemon party.
     * 
     * @param trainerId Unique id of the {@link TrainerNPC} to register.
     * @param model {@link TrainerModel} that represents the trainer.
     * @return Registered {@link TrainerNPC} instance.
     * @throws RCTException In case of validation failures with the provided model (the trainer will be registered regardless).
     * @throws IllegalArgumentException If a {@link Trainer} with the given id is already registered.
     */
    @NotNull
    public TrainerNPC registerNPC(@NotNull String trainerId, @NotNull TrainerModel model) {
        var errors = RCTErrors.create();
        var trainer = this.tmc.toTarget(model, errors);
        this.registerNPC(trainerId, trainer);
        errors.check();
        return trainer;
    }

    /**
     * Registers the given {@link TrainerNPC} to the trainer registry.
     * 
     * @param <T> {@link TrainerNPC} type.
     * @param trainerId Unique id of the {@link TrainerNPC} to register.
     * @param trainer {@link TrainerNPC} instance to register.
     * @return Registered {@link TrainerNPC} instance.
     * @throws IllegalArgumentException If a {@link Trainer} with the given id is already registered.
     */
    @NotNull
    public <T extends TrainerNPC> T registerNPC(@NotNull String trainerId, @NotNull T trainer) {
        this.register(trainerId, trainer);
        this.trainerNPCs.add(trainerId);
        trainer.initTeam(toOTId(trainerId));
        return trainer;
    }

    /**
     * Unregisters the {@link Trainer} with the given trainer id.
     * 
     * @param trainerId Id of the {@link Trainer} to unregister.
     * @return Unregistered {@link Trainer} instance or null if no such {@link Trainer} was registered.
     */
    public Trainer unregisterById(@NotNull String trainerId) {
        var trainer = this.trainers.remove(trainerId);

        if(trainer != null) {
            this.trainerPlayers.remove(trainerId);
            this.trainerNPCs.remove(trainerId);
        }

        return trainer;
    }

    /**
     * Retrieves the trainer with the given id.
     * 
     * @param trainerId Id of the trainer to retrieve.
     * @return Trainer instance from the trainer registry or null if no such {@link Trainer} is registered.
     */
    public Trainer getById(@NotNull String trainerId) {
        return this.getById(trainerId, Trainer.class);
    }

    /**
     * Retrieves the {@link Trainer} with the given id as the specified type.
     * 
     * @param <T> Target {@link Trainer} type.
     * @param trainerId Id of the {@link Trainer} to retrieve.
     * @param type Target {@link Trainer} type class instance.
     * @throws IllegalArgumentException If the trainer is not from the given type.
     * @return {@link Trainer} instance from the trainer registry or null if no such {@link Trainer} is registered.
     */
    public <T extends Trainer> T getById(@NotNull String trainerId, @NotNull Class<T> type) {
        var trainer = this.trainers.get(trainerId);

        if(trainer == null) {
            return null;
        }

        if(!type.isInstance(trainer)) {
            throw new IllegalArgumentException(String.format("invalid trainer type '%s' for '%s', expected '%s'", trainer.getClass().getName(), trainerId, type.getName()));
        }

        return type.cast(trainer);
    }

    /**
     * Retrieves the original {@link Trainer} instance for the given {@link Pokemon}.
     * 
     * @param pkmn Pokemon to retrieve the original {@link Trainer} for.
     * @throws IllegalArgumentException If the trainer is not from the given type.
     * @return {@link Trainer} instance from the trainer registry or null if no such {@link Trainer} is registered.
     */
    public Trainer getByOT(@NotNull Pokemon pkmn) {        
        return this.getByOT(pkmn, Trainer.class);
    }

    /**
     * Retrieves the original {@link Trainer} instance for the given {@link Pokemon}.
     * 
     * @param <T> Target {@link Trainer} type.
     * @param pkmn Pokemon to retrieve the original {@link Trainer} for.
     * @param type Target {@link Trainer} type class instance.
     * @throws IllegalArgumentException If the trainer is not from the given type.
     * @return {@link Trainer} instance from the trainer registry or null if no such {@link Trainer} is registered.
     */
    public <T extends Trainer> T getByOT(@NotNull Pokemon pkmn, @NotNull Class<T> type) {
        var otId = pkmn.getOriginalTrainer();
        var parts = otId != null ? otId.split(":") : new String[0];

        if(parts.length == 2 && parts[0].equals(this.id)) {
            return this.getById(parts[1], type);
        }
        
        return null;
    }

    /**
     * Retrieves an unmodifiable set of all registered trainer ids.
     * 
     * @return Set of trainer ids.
     */
    @NotNull
    public Set<String> getIds() {
        return this.trainers.keySet();
    }

    /**
     * Retrieves the trainer id for the given {@link LivingEntity}. Note that the id
     * for an entity can change at any time if either a different {@link Trainer} is
     * attached to that entity or, in case of players for potential other reasons (the
     * id handling of trainer players is not defined by this api).
     * 
     * @param entity {@link LivingEntity} to retreive the trainer id from.
     * @return Id of the attached {@link Trainer} or the id of the {@link
     * TrainerPlayer} if entity is a player. Returns null if no trainer was found.
     */
    public String getId(LivingEntity entity) {
        for(var entry : this.trainers.entrySet()) {
            var tnpc = entry.getValue().getEntity();

            if(tnpc != null && tnpc.equals(entity)) {
                return entry.getKey();
            }
        }

        return null;
    }

    /**
     * Removes all registered trainers.
     */
    public void clear() {
        this.clearNPCs();
        this.clearPlayers();
    }

    /**
     * Removes all registered {@link TrainerNPC}s.
     */
    public void clearNPCs() {
        this.trainerNPCs.forEach(this.trainers::remove);
        this.trainerNPCs.clear();
    }

    /**
     * Removes all registered {@link TrainerPlayer}s.
     */
    public void clearPlayers() {
        this.trainerPlayers.forEach(this.trainers::remove);
        this.trainerPlayers.clear();
    }

    /**
     * Constructs the original trainer id, which is used by this mod to associate
     * trainers to entities.
     * 
     * @param trainerId Trainer id to construct the OT id for.
     * @return Original trainer id.
     */
    protected String toOTId(String trainerId) {
        return String.format("%s:%s", this.id, trainerId);
    }

    private void register(String trainerId, Trainer trainer) {
        if(this.trainers.putIfAbsent(trainerId, trainer) != null) {
            throw new IllegalArgumentException(String.format("trainer already registered '%s'", trainerId));
        }
    }
}
