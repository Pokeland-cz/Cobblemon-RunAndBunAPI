package net.ctengine.api.battle;

import org.jetbrains.annotations.Nullable;

import com.cobblemon.mod.common.api.battles.model.ai.BattleAI;
import com.cobblemon.mod.common.pokemon.Pokemon;

import net.minecraft.entity.LivingEntity;

public interface BattleParticipant {
    String getName();
    Pokemon[] getTeam();
    default @Nullable LivingEntity getSourceEntity() { return null; }
    default @Nullable BattleAI getBattleAI() { return null; }
}
