package net.ctengine.api.trainer;

import org.jetbrains.annotations.NotNull;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.world.entity.LivingEntity;

/**
 * Defines the required functionality of a trainer.
 */
public interface Trainer {
    /**
     * Retrieves the name of this trainer.
     * 
     * @return Trainer name.
     */
    @NotNull String getName();

    /**
     * Retrieves the {@link Pokemon} team of this trainer.
     * 
     * @return Array of {@link Pokemon}.
     */
    @NotNull Pokemon[] getTeam();

    /**
     * Retrieves the {@link LivingEntity} associated with this trainer.
     * 
     * @return {@link LivingEntity} associated with this trainer.
     */
    @NotNull LivingEntity getEntity();
}
