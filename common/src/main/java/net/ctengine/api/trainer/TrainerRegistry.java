package net.ctengine.api.trainer;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.ctengine.api.errors.CTException;
import net.ctengine.api.models.TrainerModel;
import net.ctengine.api.models.converter.PokemonModelConverter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;

/**
 * Key/value trainer storage.
 */
public class TrainerRegistry {
    private Map<String, Trainer> trainers = new HashMap<>();
    private Set<String> trainerIds = new HashSet<>();

    private void register(String trainerId, Trainer trainer) {
        if(!this.trainerIds.add(trainerId) || this.trainers.putIfAbsent(UUID.nameUUIDFromBytes(trainerId.getBytes()).toString(), trainer) != null) {
            throw new IllegalArgumentException(String.format("trainer already registered '%s'", trainerId));
        }
    }

    /**
     * Instantiates and registers a {@link TrainerPlayer} to the trainer registry.
     * 
     * @param trainerId Unique id of the trainer to register.
     * @param trainer Player instance to represent the trainer.
     * @return Registered {@link TrainerPlayer} instance.
     * @throws IllegalArgumentException If a trainer with the given id is already registered.
     */
    public TrainerPlayer registerPlayer(String trainerId, ServerPlayer player) {
        return registerPlayer(trainerId, new TrainerPlayer(player));
    }

    /**
     * Registers the given {@link TrainerPlayer} to the trainer registry.
     * 
     * @param <T> {@link TrainerPlayer} type.
     * @param trainerId Unique id of the trainer to register.
     * @param trainer {@link TrainerPlayer} instance to register.
     * @return Registered {@link TrainerPlayer} instance.
     * @throws IllegalArgumentException If a trainer with the given id is already registered.
     */
    public <T extends TrainerPlayer> T registerPlayer(String trainerId, T trainer) {
        this.register(trainerId, trainer);
        return trainer;
    }

    /**
     * Instantiates and registers a {@link TrainerNPC} to the trainer registry. Uses a
     * default {@link PokemonModelConverter} to instantiate a party pokemon.
     * 
     * @param trainerId Unique id of the trainer to register.
     * @param model {@link TrainerModel} that represents the trainer.
     * @param server Minecraft server to instantiate a default villager the trainer will be attached to.
     * @return Registered {@link TrainerNPC} instance.
     * @throws CTException In case of validation failures with the provided model.
     * @throws IllegalArgumentException If a trainer with the given id is already registered.
     */
    public TrainerNPC registerNPC(String trainerId, TrainerModel model, MinecraftServer server) {
        return this.registerNPC(trainerId, model, server, new PokemonModelConverter());
    }

    /**
     * Instantiates and registers a {@link TrainerNPC} to the trainer registry.
     * 
     * @param trainerId Unique id of the trainer to register.
     * @param model {@link TrainerModel} that represents the trainer.
     * @param server Minecraft server to instantiate a default villager the trainer will be attached to.
     * @param pokemonModelConverter {@link PokemonModelConverter} instance used to instantiate party pokemon.
     * @return Registered {@link TrainerNPC} instance.
     * @throws CTException In case of validation failures with the provided model.
     * @throws IllegalArgumentException If a trainer with the given id is already registered.
     */
    public TrainerNPC registerNPC(String trainerId, TrainerModel model, MinecraftServer server, PokemonModelConverter pokemonModelConverter) {
        var trainer = this.registerNPC(trainerId, new TrainerNPC(
            UUID.nameUUIDFromBytes(trainerId.getBytes()),
            EntityType.VILLAGER.create(server.overworld()),
            pokemonModelConverter));

        trainer.setModel(model);
        return trainer;
    }

    /**
     * Registers the given {@link TrainerNPC} to the trainer registry.
     * 
     * @param <T> {@link TrainerNPC} type.
     * @param trainerId Unique id of the trainer to register.
     * @param trainer {@link TrainerNPC} instance to register.
     * @return Registered {@link TrainerNPC} instance.
     * @throws IllegalArgumentException If a trainer with the given id is already registered.
     */
    public <T extends TrainerNPC> T registerNPC(String trainerId, T trainer) {
        this.register(trainerId, trainer);
        return trainer;
    }

    /**
     * Unregisters the trainer with the given trainer id.
     * 
     * @param trainerId Id of the trainer to unregister.
     * @return Unregistered {@link Trainer} instance or null if no such trainer was registered.
     */
    public Trainer unregisterById(String trainerId) {
        return this.unregisterByUUID(UUID.nameUUIDFromBytes(trainerId.getBytes()));
    }

    /**
     * Unregisters the trainer with the given trainer uuid.
     * 
     * @param trainerUUID UUID of the trainer to unregister.
     * @return Unregistered {@link Trainer} instance or null if no such trainer was registered.
     */
    public Trainer unregisterByUUID(UUID trainerUUID) {
        return this.unregisterByStringUUID(trainerUUID.toString());
    }

    /**
     * Unregisters the trainer with the given trainer uuid string.
     * 
     * @param trainerUUID String representation of the uuid (as returned by {@llink UUID.toString()}) of the trainer to unregister.
     * @return Unregistered {@link Trainer} instance or null if no such trainer was registered.
     */
    public Trainer unregisterByStringUUID(String trainerUUID) {
        return this.trainers.remove(trainerUUID);
    }

    /**
     * Retrieves the trainer with the given id.
     * 
     * @param trainerId Id of the trainer to retrieve.
     * @return Trainer instance from the trainer registry.
     */
    public Trainer getById(String trainerId) {
        return this.getById(trainerId, Trainer.class);
    }

    /**
     * Retrieves the {@link Trainer} with the given id as the specified type.
     * 
     * @param <T> Target {@link Trainer} type.
     * @param trainerId Id of the {@link Trainer} to retrieve.
     * @param type Target {@link Trainer} type class instance.
     * @throws IllegalArgumentException If the trainer is not from the given type.
     * @return {@link Trainer} instance from the trainer registry.
     */
    public <T extends Trainer> T getById(String trainerId, Class<T> type) {
        return this.getByUUID(UUID.nameUUIDFromBytes(trainerId.getBytes()), type);
    }

    /**
     * Retrieves the {@link Trainer} with the given uuid.
     * 
     * @param trainerUUID UUID of the {@link Trainer} to retrieve.
     * @return {@link Trainer} instance from the trainer registry.
     */
    public Trainer getByUUID(UUID trainerUUID) {
        return this.getByUUID(trainerUUID, Trainer.class);
    }

    /**
     * Retrieves the {@link Trainer} with the given uuid as the specified type.
     * 
     * @param <T> Target {@link Trainer} type.
     * @param trainerUUID UUID of the {@link Trainer} to retrieve.
     * @param type Target {@link Trainer} type class instance.
     * @throws IllegalArgumentException If the trainer is not from the given type.
     * @return {@link Trainer} instance from the trainer registry.
     */
    public <T extends Trainer> T getByUUID(UUID trainerUUID, Class<T> type) {
        return this.getByStringUUID(trainerUUID.toString(), type);
    }

    /**
     * Retrieves the {@link Trainer} with the given uuid string.
     * 
     * @param trainerUUID String representation of the uuid (as returned by {@llink UUID.toString()}) of the {@link Trainer} to retrieve.
     * @return {@link Trainer} instance from the trainer registry.
     */
    public Trainer getByStringUUID(String trainerUUID) {
        return this.getByStringUUID(trainerUUID, Trainer.class);
    }

    /**
     * Retrieves the {@link Trainer} with the given uuid string as the specified type.
     * 
     * @param <T> Target {@link Trainer} type.
     * @param trainerUUID String representation of the uuid (as returned by {@llink UUID.toString()}) of the {@link Trainer} to retrieve.
     * @param type Target {@link Trainer} type class instance.
     * @throws IllegalArgumentException If the trainer is not from the given type.
     * @return {@link Trainer} instance from the trainer registry.
     */
    public <T extends Trainer> T getByStringUUID(String trainerUUID, Class<T> type) {
        var trainer = this.trainers.get(trainerUUID);

        if(trainer == null) {
            return null;
        }

        if(!type.isInstance(trainer)) {
            throw new IllegalArgumentException(String.format("invalid trainer type '%s' for '%s', expected '%s'", trainer.getClass().getName(), trainerUUID, type.getName()));
        }

        return type.cast(trainer);
    }

    /**
     * Retrieves an unmodifiable set of all registered trainer ids.
     * 
     * @return Set of trainer ids.
     */
    public Set<String> getIds() {
        return Collections.unmodifiableSet(this.trainerIds);
    }

    /**
     * Removes all registered trainers.
     */
    public void clear() {
        this.trainers.clear();
        this.trainerIds.clear();
    }
}
