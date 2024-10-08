package net.ctengine.api.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.cobblemon.mod.common.api.abilities.Abilities;
import com.cobblemon.mod.common.api.moves.Moves;
import com.cobblemon.mod.common.api.pokemon.Natures;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.pokemon.Gender;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

/**
 * A pojo class for parsing pokemon.
 */
public class PokemonModel {
    public static class StatsModel {
        private int hp;
        private int atk;
        private int def;
        private int spa;
        private int spd;
        private int spe;

        public int getHP() { return this.hp; }
        public int getAtk() { return this.atk; }
        public int getDef() { return this.def; }
        public int getSpA() { return this.spa; }
        public int getSpD() { return this.spd; }
        public int getSpe() { return this.spe; }

        @Override
        public boolean equals(Object obj) {
            return (obj instanceof StatsModel other)
                && this.hp == other.hp
                && this.atk == other.atk
                && this.def == other.def
                && this.spa == other.spa
                && this.spd == other.spd
                && this.spe == other.spe;
        }

        @Override
        public int hashCode() {
            return Objects.hash(
                this.hp, this.atk,
                this.def, this.spa,
                this.spd, this.spe);
        }
    }

    private String species = "";
    private Gender gender = Gender.GENDERLESS;
    private int level = 0;
    private String nature = "hardy";
    private String ability = "runaway";
    private List<String> moveset = new ArrayList<>();
    private StatsModel ivs = new StatsModel();
    private StatsModel evs = new StatsModel();
    private boolean shiny = false;
    private String heldItem = "minecraft:air";
    private Set<String> aspects = new HashSet<>();

    public String getSpecies() { return this.species; }
    public Gender getGender() { return this.gender; }
    public int getLevel() { return this.level; }
    public String getNature() { return this.nature; }
    public String getAbility() { return this.ability; }
    public List<String> getMoveset() { return Collections.unmodifiableList(this.moveset); }
    public StatsModel getIVs() { return this.ivs; }
    public StatsModel getEVs() { return this.evs; }
    public boolean isShiny() { return this.shiny; }
    public String getHeldItem() { return this.heldItem; }
    public Set<String> getAspects() { return Collections.unmodifiableSet(this.aspects); }

    public void fromPokemon(Pokemon pokemon) {
        this.species = pokemon.getSpecies().getName();
        this.gender = pokemon.getGender();
        this.level = pokemon.getLevel();
        this.nature = pokemon.getNature().getName().toString();
        this.ability = pokemon.getAbility().getName();
        this.moveset = pokemon.getMoveSet().getMoves().stream().map(m -> m.getName()).toList();
        this.ivs.hp = pokemon.getIvs().getOrDefault(Stats.HP);
        this.ivs.atk = pokemon.getIvs().getOrDefault(Stats.ATTACK);
        this.ivs.def = pokemon.getIvs().getOrDefault(Stats.DEFENCE);
        this.ivs.spa = pokemon.getIvs().getOrDefault(Stats.SPECIAL_ATTACK);
        this.ivs.spd = pokemon.getIvs().getOrDefault(Stats.SPECIAL_DEFENCE);
        this.ivs.spe = pokemon.getIvs().getOrDefault(Stats.SPEED);
        this.evs.hp = pokemon.getEvs().getOrDefault(Stats.HP);
        this.evs.atk = pokemon.getEvs().getOrDefault(Stats.ATTACK);
        this.evs.def = pokemon.getEvs().getOrDefault(Stats.DEFENCE);
        this.evs.spa = pokemon.getEvs().getOrDefault(Stats.SPECIAL_ATTACK);
        this.evs.spd = pokemon.getEvs().getOrDefault(Stats.SPECIAL_DEFENCE);
        this.evs.spe = pokemon.getEvs().getOrDefault(Stats.SPEED);
        this.shiny = pokemon.getShiny();
        this.heldItem = Registries.ITEM.getId(pokemon.heldItem().getItem()).toString();
        this.aspects = pokemon.getAspects();
    }

    public Pokemon toPokemon() {
        // TODO: validate properties (throws IllegalStateException)
        var pokemon = new Pokemon();
        pokemon.setSpecies(PokemonSpecies.INSTANCE.getByIdentifier(Identifier.of(this.species)));
        pokemon.setGender(this.gender);
        pokemon.setLevel(this.level);
        pokemon.setNature(Natures.INSTANCE.getNature(this.nature));
        pokemon.setAbility$common(Abilities.INSTANCE.get(this.ability).create(true));
        this.moveset.forEach(m -> pokemon.getMoveSet().add(Moves.INSTANCE.getByName(m).create()));
        pokemon.setIV(Stats.HP, this.ivs.hp);
        pokemon.setIV(Stats.ATTACK, this.ivs.atk);
        pokemon.setIV(Stats.DEFENCE, this.ivs.def);
        pokemon.setIV(Stats.SPECIAL_ATTACK, this.ivs.spd);
        pokemon.setIV(Stats.SPECIAL_DEFENCE, this.ivs.spa);
        pokemon.setIV(Stats.SPEED, this.ivs.spe);
        pokemon.setEV(Stats.HP, this.evs.hp);
        pokemon.setEV(Stats.ATTACK, this.evs.atk);
        pokemon.setEV(Stats.DEFENCE, this.evs.def);
        pokemon.setEV(Stats.SPECIAL_ATTACK, this.evs.spd);
        pokemon.setEV(Stats.SPECIAL_DEFENCE, this.evs.spa);
        pokemon.setEV(Stats.SPEED, this.evs.spe);
        pokemon.setShiny(this.shiny);
        pokemon.setHeldItem$common(Registries.ITEM.get(Identifier.of(this.heldItem)).getDefaultStack());
        pokemon.getAspects().addAll(this.aspects);
        return pokemon;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof PokemonModel other)
            && this.species.equals(other.species)
            && this.gender == other.gender
            && this.level == other.level
            && this.nature.equals(other.nature)
            && this.ability.equals(other.ability)
            && this.moveset.equals(other.moveset)
            && this.ivs.equals(other.ivs)
            && this.evs.equals(other.evs)
            && this.shiny == other.shiny
            && this.heldItem.equals(other.heldItem)
            && this.aspects.equals(other.aspects);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            this.species, this.gender,
            this.level, this.nature,
            this.ability, this.moveset,
            this.ivs, this.evs,
            this.shiny, this.heldItem,
            this.aspects);
    }
}