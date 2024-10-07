package net.ctengine.api.battle;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.cobblemon.mod.common.pokemon.Pokemon;

import net.minecraft.entity.LivingEntity;

public interface BattleParticipant {
    @NotNull String getName();
    @NotNull Pokemon[] getTeam();
    default @Nullable LivingEntity getSourceEntity() { return null; }
}
