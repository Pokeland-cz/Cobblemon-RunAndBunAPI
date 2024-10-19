package net.ctengine.api.util;

import java.util.Set;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import net.ctengine.api.CTEngine;
import net.ctengine.api.models.TrainerModel;
import net.ctengine.api.models.converter.PokemonModelConverter;
import net.ctengine.api.trainer.Trainer;
import net.ctengine.api.trainer.TrainerNPC;
import net.ctengine.api.trainer.TrainerPlayer;
import net.ctengine.api.trainer.TrainerRegistry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * Utility class for easy access to the api provided by the {@link TrainerRegistry}
 * instance of the {@link CTEngine} singleton.
 */
public final class Trainers {
    private Trainers() {
    }

    /**
     * @see {@link TrainerRegistry#registerPlayer(String, ServerPlayer)}
     */
    public static TrainerPlayer registerPlayer(String trainerId, ServerPlayer player) {
        return CTEngine.getInstance().getTrainerRegistry().registerPlayer(trainerId, player);
    }

    /**
     * @see {@link TrainerRegistry#registerPlayer(String, TrainerPlayer)}
     */
    public static <T extends TrainerPlayer> T registerPlayer(String trainerId, T trainer) {
        return CTEngine.getInstance().getTrainerRegistry().registerPlayer(trainerId, trainer);
    }

    /**
     * @see {@link TrainerRegistry#registerNPC(String, TrainerModel, MinecraftServer)}
     */
    public static TrainerNPC registerNPC(String trainerId, TrainerModel model, MinecraftServer server) {
        return CTEngine.getInstance().getTrainerRegistry().registerNPC(trainerId, model, server);
    }

    /**
     * @see {@link TrainerRegistry#registerNPC(String, TrainerModel, MinecraftServer, PokemonModelConverter)}
     */
    public static TrainerNPC registerNPC(String trainerId, TrainerModel model, MinecraftServer server, PokemonModelConverter pokemonModelConverter) {
        return CTEngine.getInstance().getTrainerRegistry().registerNPC(trainerId, model, server);
    }

    /**
     * @see {@link TrainerRegistry#registerNPC(String, TrainerNPC)}
     */
    public static <T extends TrainerNPC> T registerNPC(String trainerId, T trainer) {
        return CTEngine.getInstance().getTrainerRegistry().registerNPC(trainerId, trainer);
    }

    /**
     * @see {@link TrainerRegistry#unregisterById(String)}
     */
    @Nullable
    public static Trainer unregisterById(String trainerId) {
        return CTEngine.getInstance().getTrainerRegistry().unregisterById(trainerId);
    }

    /**
     * @see {@link TrainerRegistry#unregisterByUUID(UUID)}
     */
    @Nullable
    public static Trainer unregisterByUUID(UUID trainerUUID) {
        return CTEngine.getInstance().getTrainerRegistry().unregisterByUUID(trainerUUID);
    }

    /**
     * @see {@link TrainerRegistry#unregisterByStringUUID(String)}
     */
    @Nullable
    public static Trainer unregisterByStringUUID(String trainerUUID) {
        return CTEngine.getInstance().getTrainerRegistry().unregisterByStringUUID(trainerUUID);
    }

    /**
     * @see {@link TrainerRegistry#getById(String)}
     */
    @Nullable
    public static Trainer getById(String trainerId) {
        return CTEngine.getInstance().getTrainerRegistry().getById(trainerId);
    }

    /**
     * @see {@link TrainerRegistry#getById(String, Class)}
     */
    @Nullable
    public static <T extends Trainer> T getById(String trainerId, Class<T> type) {
        return CTEngine.getInstance().getTrainerRegistry().getById(trainerId, type);
    }

    /**
     * @see {@link TrainerRegistry#getByUUID(UUID)}
     */
    @Nullable
    public static Trainer getByUUID(UUID trainerUUID) {
        return CTEngine.getInstance().getTrainerRegistry().getByUUID(trainerUUID);
    }

    /**
     * @see {@link TrainerRegistry#getByUUID(UUID, Class)}
     */
    @Nullable
    public static <T extends Trainer> T getByUUID(UUID trainerUUID, Class<T> type) {
        return CTEngine.getInstance().getTrainerRegistry().getByUUID(trainerUUID, type);
    }

    /**
     * @see {@link TrainerRegistry#getByStringUUID(String)}
     */
    @Nullable
    public static Trainer getByStringUUID(String trainerUUID) {
        return CTEngine.getInstance().getTrainerRegistry().getByStringUUID(trainerUUID);
    }

    /**
     * @see {@link TrainerRegistry#getByStringUUID(String, Class)}
     */
    @Nullable
    public static <T extends Trainer> T getByStringUUID(String trainerUUID, Class<T> type) {
        return CTEngine.getInstance().getTrainerRegistry().getByStringUUID(trainerUUID, type);
    }

    /**
     * @see {@link TrainerRegistry#getIds()}
     */
    public static Set<String> getIds() {
        return CTEngine.getInstance().getTrainerRegistry().getIds();
    }

    /**
     * @see {@link TrainerRegistry#clear()}
     */
    public static void clear() {
        CTEngine.getInstance().getTrainerRegistry().clear();
    }
}
