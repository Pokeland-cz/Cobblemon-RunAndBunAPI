package net.ctengine.api.trainer;

import java.util.ArrayList;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.battles.model.ai.BattleAI;
import com.cobblemon.mod.common.pokemon.Pokemon;

import net.ctengine.api.battle.BattleParticipant;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public class TrainerPlayer implements BattleParticipant {
    private ServerPlayerEntity player;

    public TrainerPlayer(@NotNull ServerPlayerEntity player) {
        this.player = player;
    }

    public ServerPlayerEntity getPlayer() {
        return this.player;
    }

    @Override
    public String getName() {
        return this.player.getDisplayName().getString();
    }

    @Override
    public Pokemon[] getTeam() {
        var party = new ArrayList<Pokemon>();
        Cobblemon.INSTANCE.getStorage().getParty(this.player).forEach(party::add);
        return party.toArray(new Pokemon[party.size()]);
    }

    @Override
    public LivingEntity getSourceEntity() {
        return this.player;
    }

    @Override
    public @Nullable BattleAI getBattleAI() {
        return null;
    }
}
