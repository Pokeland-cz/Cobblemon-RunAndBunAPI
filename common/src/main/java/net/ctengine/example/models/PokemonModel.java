package net.ctengine.example.models;

import com.cobblemon.mod.common.pokemon.Gender;

// A pojo for parsing pokemon from json.
public class PokemonModel {
    private String species = "cobblemon:missingno";
    private Gender gender = Gender.GENDERLESS;
    private int level;

    public String getSpecies() {
        return this.species;
    }

    public Gender getGender() {
        return this.gender;
    }
    
    public int getLevel() {
        return this.level;
    }
}
