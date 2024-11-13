package net.ctengine.api.util;

import java.util.Set;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import net.ctengine.api.CTEngine;
import net.ctengine.api.models.TrainerModel;
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
     * @see {@link TrainerRegistry#init()}
     */
    public static void init(@NotNull MinecraftServer server) {
        CTEngine.getInstance().getTrainerRegistry().init(server);
    }

    /**
     * @see {@link TrainerRegistry#registerPlayer(String, ServerPlayer)}
     */
    @NotNull
    public static TrainerPlayer registerPlayer(@NotNull String trainerId, @NotNull ServerPlayer player) {
        return CTEngine.getInstance().getTrainerRegistry().registerPlayer(trainerId, player);
    }

    /**
     * @see {@link TrainerRegistry#registerPlayer(String, TrainerPlayer)}
     */
    @NotNull
    public static <T extends TrainerPlayer> T registerPlayer(@NotNull String trainerId, @NotNull T trainer) {
        return CTEngine.getInstance().getTrainerRegistry().registerPlayer(trainerId, trainer);
    }

    /**
     * @see {@link TrainerRegistry#registerNPC(String, TrainerModel, MinecraftServer)}
     */
    @NotNull
    public static TrainerNPC registerNPC(@NotNull String trainerId, @NotNull TrainerModel model) {
        return CTEngine.getInstance().getTrainerRegistry().registerNPC(trainerId, model);
    }

    /**
     * @see {@link TrainerRegistry#registerNPC(String, TrainerNPC)}
     */
    @NotNull
    public static <T extends TrainerNPC> T registerNPC(@NotNull String trainerId, @NotNull T trainer) {
        return CTEngine.getInstance().getTrainerRegistry().registerNPC(trainerId, trainer);
    }

    /**
     * @see {@link TrainerRegistry#unregisterById(String)}
     */
    public static Trainer unregisterById(@NotNull String trainerId) {
        return CTEngine.getInstance().getTrainerRegistry().unregisterById(trainerId);
    }

    /**
     * @see {@link TrainerRegistry#unregisterByUUID(UUID)}
     */
    public static Trainer unregisterByUUID(@NotNull UUID trainerUUID) {
        return CTEngine.getInstance().getTrainerRegistry().unregisterByUUID(trainerUUID);
    }

    /**
     * @see {@link TrainerRegistry#unregisterByStringUUID(String)}
     */
    public static Trainer unregisterByStringUUID(@NotNull String trainerUUID) {
        return CTEngine.getInstance().getTrainerRegistry().unregisterByStringUUID(trainerUUID);
    }

    /**
     * @see {@link TrainerRegistry#getById(String)}
     */
    public static Trainer getById(@NotNull String trainerId) {
        return CTEngine.getInstance().getTrainerRegistry().getById(trainerId);
    }

    /**
     * @see {@link TrainerRegistry#getById(String, Class)}
     */
    public static <T extends Trainer> T getById(@NotNull String trainerId, @NotNull Class<T> type) {
        return CTEngine.getInstance().getTrainerRegistry().getById(trainerId, type);
    }

    /**
     * @see {@link TrainerRegistry#getByUUID(UUID)}
     */
    public static Trainer getByUUID(@NotNull UUID trainerUUID) {
        return CTEngine.getInstance().getTrainerRegistry().getByUUID(trainerUUID);
    }

    /**
     * @see {@link TrainerRegistry#getByUUID(UUID, Class)}
     */
    public static <T extends Trainer> T getByUUID(@NotNull UUID trainerUUID, @NotNull Class<T> type) {
        return CTEngine.getInstance().getTrainerRegistry().getByUUID(trainerUUID, type);
    }

    /**
     * @see {@link TrainerRegistry#getByStringUUID(String)}
     */
    public static Trainer getByStringUUID(@NotNull String trainerUUID) {
        return CTEngine.getInstance().getTrainerRegistry().getByStringUUID(trainerUUID);
    }

    /**
     * @see {@link TrainerRegistry#getByStringUUID(String, Class)}
     */
    public static <T extends Trainer> T getByStringUUID(@NotNull String trainerUUID, @NotNull Class<T> type) {
        return CTEngine.getInstance().getTrainerRegistry().getByStringUUID(trainerUUID, type);
    }

    /**
     * @see {@link TrainerRegistry#getIds()}
     */
    @NotNull
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
