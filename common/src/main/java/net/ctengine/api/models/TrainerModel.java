package net.ctengine.api.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

import com.cobblemon.mod.common.api.battles.model.ai.BattleAI;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.ctengine.api.ai.SelfdotGen5AI;
import net.ctengine.api.battle.AIBattleParticipant;

/**
 * A pojo class for parsing trainers.
 */
public class TrainerModel implements AIBattleParticipant {
    private String name = "";
    private List<PokemonModel> team = new ArrayList<>();
    private transient BattleAI battleAI = new SelfdotGen5AI();

    @Override @NotNull
    public String getName() {
        return this.name;
    }

    @Override @NotNull
    public Pokemon[] getTeam() {
        return this.team.stream().map(pm -> pm.toPokemon()).toList().toArray(new Pokemon[0]);
    }

    @Override @NotNull
    public BattleAI getBattleAI() {
        return this.battleAI;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.team);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof TrainerModel other)
            && this.name.equals(other.name)
            && this.team.equals(other.team);
    }
}
