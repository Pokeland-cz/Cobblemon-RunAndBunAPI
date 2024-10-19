package net.ctengine.api.trainer;

import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.battles.model.ai.BattleAI;
import com.cobblemon.mod.common.api.storage.party.PartyStore;
import com.cobblemon.mod.common.pokemon.OriginalTrainerType;
import com.cobblemon.mod.common.pokemon.Pokemon;

import net.ctengine.api.ai.SelfdotGen5AI;
import net.ctengine.api.errors.CTError;
import net.ctengine.api.errors.CTErrors;
import net.ctengine.api.errors.CTException;
import net.ctengine.api.models.PokemonModel;
import net.ctengine.api.models.TrainerModel;
import net.ctengine.api.models.converter.Converter;
import net.ctengine.api.models.converter.PokemonModelConverter;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.entity.LivingEntity;

/**
 * An ai trainer that is represented by an arbitrary living entity.
 */
public class TrainerNPC implements Trainer {
    private BattleAI battleAI = new SelfdotGen5AI();
    private Pokemon[] team;
    private LivingEntity entity;
    private TrainerModel model;
    private Converter<PokemonModel, Pokemon> pmc;

    private RegistryAccess registryAccess;
    private UUID uuid;

    /**
     * Creates a new trainer npc.
     * 
     * @param uuid UUID of the trainer npc.
     * @param entity Living entity this trainer is (initially) attached to.
     * @param model {@link TrainerModel} representing this trainer.
     * @param pokemonModelConverter {@link PokemonModelConverter} instance used to instantiate party pokemon.
     */
    public TrainerNPC(@NotNull UUID uuid, @NotNull LivingEntity entity, @NotNull TrainerModel model, @NotNull Converter<PokemonModel, Pokemon> pokemonModelConverter) {
        this.registryAccess = RegistryAccess.EMPTY;
        this.pmc = pokemonModelConverter;
        this.entity = entity;
        this.uuid = uuid;
        this.setModel(model);
    }

    /**
     * Sets the battle ai of this trainer.
     * 
     * @param battleAI New battle ai.
     */
    public void setBattleAI(@NotNull BattleAI battleAI) {
        this.battleAI = battleAI;
    }

    /**
     * Sets the battle ai of this trainer.
     * 
     * @param battleAI New battle ai.
     * @return This trainer instance.
     */
    public TrainerNPC witBattleAI(@NotNull BattleAI battleAI) {
        this.battleAI = battleAI;
        return this;
    }

    /**
     * Sets the model of this trainer.
     * 
     * @param model New {@link TrainerModel}.
     * @throws CTException In case of validation failures with the provided model.
     */
    public void setModel(@NotNull TrainerModel model) {
        var errors = CTErrors.create();
        this.model = model;

        if(this.model.getTeam().size() > 6) {
            errors.add(CTError.of("too many pokemon in party " + this.model.getTeam().size() + "/6"));
        }

        this.team = this.model.getTeam().stream().limit(6)
            .map(pkModel -> this.pmc.toTarget(pkModel, errors))
            .toList().toArray(new Pokemon[0]);

        errors.check();
        this.storeTeam();
    }

    /**
     * Sets the pokemon model converter of this trainer.
     * 
     * @param pokemonModelConverter New {@link Converter}.
     */
    public void setPokemonModelConverter(@NotNull Converter<PokemonModel, Pokemon> pokemonModelConverter) {
        this.pmc = pokemonModelConverter;
    }

    /**
     * Sets the entity associated to this trainer.
     * 
     * @param entity Entity to associate to this trainer.
     */
    public void setEntity(@NotNull LivingEntity entity) {
        if(entity != this.entity) {
            this.entity = entity;
            this.storeTeam();
        }
    }

    /**
     * Retrieves the battle ai of this trainer.
     * 
     * @return Current battle ai.
     */
    @NotNull
    public BattleAI getBattleAI() {
        return this.battleAI;
    }

    /**
     * Retrieves the model of this trainer.
     * 
     * @return {@link TrainerModel}.
     */
    @NotNull
    public TrainerModel getModel() {
        return this.model;
    }

    @Override @NotNull
    public String getName() {
        return this.model.getName();
    }

    @Override @NotNull
    public Pokemon[] getTeam() {
        return this.team;
    }

    @Override @NotNull
    public LivingEntity getEntity() {
        return this.entity;
    }
    
    private void storeTeam() {
        var store = Cobblemon.INSTANCE.getStorage().getCustomStore(PartyStore.class, this.uuid, this.registryAccess);
        store.clearParty();

        for(var pkmn : this.team) {
            if(!store.add(pkmn)) {
                throw new IllegalStateException("failed to store pokemon for trainer npc");
            }

            pkmn.setOriginalTrainer(this.uuid);
            pkmn.setOriginalTrainerName(this.getName());
            pkmn.setOriginalTrainerType$common(OriginalTrainerType.NPC);
        }
    }
}
