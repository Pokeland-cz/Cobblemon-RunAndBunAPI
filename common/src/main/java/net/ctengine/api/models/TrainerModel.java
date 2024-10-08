package net.ctengine.api.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * A pojo class for parsing trainers.
 */
public class TrainerModel {
    private String name = "";
    private List<PokemonModel> team = new ArrayList<>();

    @NotNull
    public String getName() {
        return this.name;
    }

    @NotNull
    public List<PokemonModel> getTeam() {
        return this.team;
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
