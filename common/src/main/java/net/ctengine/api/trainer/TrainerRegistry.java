package net.ctengine.api.trainer;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import net.ctengine.api.errors.CTErrors;
import net.ctengine.api.errors.CTException;
import net.ctengine.api.models.TrainerModel;
import net.ctengine.api.models.converter.PokemonModelConverter;
import net.ctengine.api.models.converter.TrainerModelConverter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * Key/value trainer storage.
 */
public class TrainerRegistry {
    private Map<String, Trainer> trainers = new HashMap<>();
    private Set<String> trainerIds = new HashSet<>();
    private TrainerModelConverter tmc;

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
        return trainer;
    }

    /**
     * Instantiates and registers a {@link TrainerNPC} to the trainer registry. Uses a
     * default {@link PokemonModelConverter} to create the pokemon party.
     * 
     * @param trainerId Unique id of the {@link TrainerNPC} to register.
     * @param model {@link TrainerModel} that represents the trainer.
     * @return Registered {@link TrainerNPC} instance.
     * @throws CTException In case of validation failures with the provided model (the trainer will be registered regardless).
     * @throws IllegalArgumentException If a {@link Trainer} with the given id is already registered.
     */
    @NotNull
    public TrainerNPC registerNPC(@NotNull String trainerId, @NotNull TrainerModel model) {
        var errors = CTErrors.create();
        var trainer = this.tmc.toTarget(model, errors);

        trainer.setUUID(UUID.nameUUIDFromBytes(trainerId.getBytes()));
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
        return trainer;
    }

    /**
     * Unregisters the {@link Trainer} with the given trainer id.
     * 
     * @param trainerId Id of the {@link Trainer} to unregister.
     * @return Unregistered {@link Trainer} instance or null if no such {@link Trainer} was registered.
     */
    public Trainer unregisterById(@NotNull String trainerId) {
        return this.unregisterByUUID(UUID.nameUUIDFromBytes(trainerId.getBytes()));
    }

    /**
     * Unregisters the {@link Trainer} with the given trainer uuid.
     * 
     * @param trainerUUID UUID of the {@link Trainer} to unregister.
     * @return Unregistered {@link Trainer} instance or null if no such {@link Trainer} was registered.
     */
    public Trainer unregisterByUUID(@NotNull UUID trainerUUID) {
        return this.unregisterByStringUUID(trainerUUID.toString());
    }

    /**
     * Unregisters the {@link Trainer} with the given trainer uuid string.
     * 
     * @param trainerUUID String representation of the uuid (as returned by {@link UUID#toString()}) of the trainer to unregister.
     * @return Unregistered {@link Trainer} instance or null if no such {@link Trainer} was registered.
     */
    public Trainer unregisterByStringUUID(@NotNull String trainerUUID) {
        return this.trainers.remove(trainerUUID);
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
        return this.getByUUID(UUID.nameUUIDFromBytes(trainerId.getBytes()), type);
    }

    /**
     * Retrieves the {@link Trainer} with the given uuid.
     * 
     * @param trainerUUID UUID of the {@link Trainer} to retrieve.
     * @return {@link Trainer} instance from the trainer registry or null if no such {@link Trainer} is registered.
     */
    public Trainer getByUUID(@NotNull UUID trainerUUID) {
        return this.getByUUID(trainerUUID, Trainer.class);
    }

    /**
     * Retrieves the {@link Trainer} with the given uuid as the specified type.
     * 
     * @param <T> Target {@link Trainer} type.
     * @param trainerUUID UUID of the {@link Trainer} to retrieve.
     * @param type Target {@link Trainer} type class instance.
     * @throws IllegalArgumentException If the trainer is not from the given type.
     * @return {@link Trainer} instance from the trainer registry or null if no such {@link Trainer} is registered.
     */
    public <T extends Trainer> T getByUUID(@NotNull UUID trainerUUID, @NotNull Class<T> type) {
        return this.getByStringUUID(trainerUUID.toString(), type);
    }

    /**
     * Retrieves the {@link Trainer} with the given uuid string.
     * 
     * @param trainerUUID String representation of the uuid (as returned by {@llink UUID.toString()}) of the {@link Trainer} to retrieve.
     * @return {@link Trainer} instance from the trainer registry or null if no such {@link Trainer} is registered.
     */
    public Trainer getByStringUUID(@NotNull String trainerUUID) {
        return this.getByStringUUID(trainerUUID, Trainer.class);
    }

    /**
     * Retrieves the {@link Trainer} with the given uuid string as the specified type.
     * 
     * @param <T> Target {@link Trainer} type.
     * @param trainerUUID String representation of the uuid (as returned by {@llink UUID.toString()}) of the {@link Trainer} to retrieve.
     * @param type Target {@link Trainer} type class instance.
     * @throws IllegalArgumentException If the trainer is not from the given type.
     * @return {@link Trainer} instance from the trainer registry or null if no such {@link Trainer} is registered.
     */
    public <T extends Trainer> T getByStringUUID(@NotNull String trainerUUID, @NotNull Class<T> type) {
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
    @NotNull
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

    private void register(String trainerId, Trainer trainer) {
        if(!this.trainerIds.add(trainerId) || this.trainers.putIfAbsent(UUID.nameUUIDFromBytes(trainerId.getBytes()).toString(), trainer) != null) {
            throw new IllegalArgumentException(String.format("trainer already registered '%s'", trainerId));
        }
    }
}
