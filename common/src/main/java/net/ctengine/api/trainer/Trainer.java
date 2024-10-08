package net.ctengine.api.trainer;

import org.jetbrains.annotations.NotNull;
import com.cobblemon.mod.common.pokemon.Pokemon;

import net.minecraft.entity.LivingEntity;

public interface Trainer {
    @NotNull String getName();
    @NotNull Pokemon[] getTeam();
    @NotNull LivingEntity getEntity();
}
