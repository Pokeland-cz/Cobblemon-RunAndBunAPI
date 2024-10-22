package net.ctengine.api.models;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.registries.BuiltInRegistries;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.pokemon.Pokemon;

/**
 * A pojo class for parsing {@link Pokemon}.
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
    private String gender = "GENDERLESS";
    private int level = 1;
    private String nature = "";
    private String ability = "";
    private Set<String> moveset = new HashSet<>();
    private StatsModel ivs = new StatsModel();
    private StatsModel evs = new StatsModel();
    private boolean shiny = false;
    private String heldItem = "";
    private Set<String> aspects = new HashSet<>();

    public String getSpecies() { return this.species; }
    public String getGender() { return this.gender; }
    public int getLevel() { return this.level; }
    public String getNature() { return this.nature; }
    public String getAbility() { return this.ability; }
    public Set<String> getMoveset() { return Collections.unmodifiableSet(this.moveset); }
    public StatsModel getIVs() { return this.ivs; }
    public StatsModel getEVs() { return this.evs; }
    public boolean isShiny() { return this.shiny; }
    public String getHeldItem() { return this.heldItem; }
    public Set<String> getAspects() { return Collections.unmodifiableSet(this.aspects); }

    /**
     * Creates a new pokemon model.
     */
    protected PokemonModel() {}

    /**
     * Creates a new pokemon model with its properties copied from the given pokemon.
     * 
     * @param pokemon Pokemon to copy.
     */
    public PokemonModel(Pokemon pokemon) {
        this.species = pokemon.getSpecies().getName();
        this.gender = pokemon.getGender().getSerializedName();
        this.level = pokemon.getLevel();
        this.nature = pokemon.getNature().getName().toString();
        this.ability = pokemon.getAbility().getName();
        this.moveset = pokemon.getMoveSet().getMoves().stream().map(m -> m.getName()).collect(Collectors.toSet());
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
        this.heldItem = BuiltInRegistries.ITEM.getKey(pokemon.heldItem().getItem()).toString();
        this.aspects = pokemon.getAspects();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof PokemonModel other)
            && this.species.equals(other.species)
            && this.gender.equals(other.gender)
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