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
     * Retrieves the pokemon team of this trainer.
     * 
     * @return Array of pokemon.
     */
    @NotNull Pokemon[] getTeam();

    /**
     * Retrieves the entity associated to this trainer.
     * 
     * @return Entity associated to this trainer.
     */
    @NotNull LivingEntity getEntity();
}
