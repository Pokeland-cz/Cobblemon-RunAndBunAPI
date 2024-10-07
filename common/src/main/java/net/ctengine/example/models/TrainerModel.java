package net.ctengine.example.models;

import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import com.cobblemon.mod.common.api.battles.model.ai.BattleAI;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.properties.UncatchableProperty;

import net.ctengine.api.ai.SelfdotGen5AI;
import net.ctengine.api.battle.AIBattleParticipant;
import net.minecraft.util.Identifier;

// A pojo for parsing trainers from json. Since the AIBattleParticipant interface
// is rather simple it can be implemented right here.
public class TrainerModel implements AIBattleParticipant {
    private String name = "";
    private PokemonModel[] team = new PokemonModel[0];
    private transient BattleAI battleAI = new SelfdotGen5AI();

    @Override @NotNull
    public String getName() {
        return this.name;
    }

    @Override @NotNull
    public Pokemon[] getTeam() {
        var cobbleTeam = new Pokemon[this.team.length];

        for(int i = 0; i < this.team.length; i++) {
            cobbleTeam[i] = toCobblemon(this.team[i]);
        }

        return cobbleTeam;
    }

    private static Pokemon toCobblemon(PokemonModel model) {
        var cobblemon = new Pokemon();
        cobblemon.setSpecies(PokemonSpecies.INSTANCE.getByIdentifier(Identifier.of(model.getSpecies())));
        cobblemon.setGender(model.getGender());
        cobblemon.setLevel(model.getLevel());
        cobblemon.setUuid(UUID.randomUUID());
        cobblemon.getCustomProperties().add(UncatchableProperty.INSTANCE.uncatchable());
        return cobblemon;
    }

    @Override @NotNull
    public BattleAI getBattleAI() {
        return this.battleAI;
    }
}
