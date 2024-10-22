package net.ctengine.api.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

import net.ctengine.api.ai.AIType;

/**
 * A pojo class for parsing {@link Trainer}.
 */
public class TrainerModel {
    private String name = "";
    private AIType ai = AIType.RNG; // TODO: maybe option for default
    private List<BagItemModel> bag = new ArrayList<>();
    private List<PokemonModel> team = new ArrayList<>();

    @NotNull
    public String getName() {
        return this.name;
    }

    @NotNull
    public List<BagItemModel> getBag() {
        return this.bag;
    }

    @NotNull
    public List<PokemonModel> getTeam() {
        return this.team;
    }

    @NotNull
    public AIType getAI() {
        return this.ai;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.team);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof TrainerModel other)
            && this.name.equals(other.name)
            && this.bag.equals(other.bag)
            && this.team.equals(other.team);
    }
}
