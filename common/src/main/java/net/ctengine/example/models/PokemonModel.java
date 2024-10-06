package net.ctengine.example.models;

import com.cobblemon.mod.common.pokemon.Gender;

public class PokemonModel {
    public final String species;
    public final Gender gender;
    public final int level;

    public PokemonModel(String species, Gender gender, int level) {
        this.species = species;
        this.gender = gender;
        this.level = level;
    }
}
