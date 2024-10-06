package net.ctengine.example.models;

import java.util.UUID;

import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.properties.UncatchableProperty;

import net.ctengine.api.battle.BattleParticipant;
import net.minecraft.util.Identifier;

public class TrainerModel implements BattleParticipant {
    public final String name;
    public final PokemonModel[] team;

    public TrainerModel(String name, PokemonModel... team) {
        this.name = name;
        this.team = team;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Pokemon[] getTeam() {
        var cobbleTeam = new Pokemon[this.team.length];

        for(int i = 0; i < this.team.length; i++) {
            cobbleTeam[i] = toCobblemon(this.team[i]);
        }

        return cobbleTeam;
    }

    private static Pokemon toCobblemon(PokemonModel model) {
        var cobblemon = new com.cobblemon.mod.common.pokemon.Pokemon();
        cobblemon.setSpecies(PokemonSpecies.INSTANCE.getByIdentifier(Identifier.of(model.species)));
        cobblemon.setGender(model.gender);
        cobblemon.setLevel(model.level);
        cobblemon.setUuid(UUID.randomUUID());
        cobblemon.getCustomProperties().add(UncatchableProperty.INSTANCE.uncatchable());
        return cobblemon;
    }
}
