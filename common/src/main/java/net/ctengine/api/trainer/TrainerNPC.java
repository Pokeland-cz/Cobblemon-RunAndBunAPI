package net.ctengine.api.trainer;

import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import com.cobblemon.mod.common.api.battles.model.ai.BattleAI;
import com.cobblemon.mod.common.pokemon.OriginalTrainerType;
import com.cobblemon.mod.common.pokemon.Pokemon;

import net.minecraft.world.entity.LivingEntity;

/**
 * An ai trainer that is represented by an arbitrary {@link LivingEntity}.
 */
public class TrainerNPC implements Trainer {
    private String name;
    private Pokemon[] team;
    private TrainerBag bag;
    private BattleAI battleAI;
    private LivingEntity entity;
    private UUID uuid;

    /**
     * Constructs a new trainer npc with a random {@link UUID}.
     * 
     * @param name The name of the trainer.
     * @param team The {@link Pokemon} party of the trainer.
     * @param bag {@link TrainerBag} containing the items a trainer can use per battle.
     * @param battleAI {@link BattleAI} used by this trainer.
     * @param entity {@link LivingEntity} this trainer is (initially) attached to.
     */
    public TrainerNPC(@NotNull String name, @NotNull Pokemon[] team, @NotNull TrainerBag bag, @NotNull BattleAI battleAI, @NotNull LivingEntity entity) {
        this(UUID.randomUUID(), name, team, bag, battleAI, entity);
    }

    /**
     * Constructs a new trainer npc.
     * 
     * @param uuid {@link UUID} to identify this trainer.
     * @param name The name of the trainer.
     * @param team The {@link Pokemon} party of the trainer.
     * @param bag {@link TrainerBag} containing the items a trainer can use per battle.
     * @param battleAI {@link BattleAI} used by this trainer.
     * @param entity {@link LivingEntity} this trainer is (initially) attached to.
     */
    public TrainerNPC(@NotNull UUID uuid, @NotNull String name, @NotNull Pokemon[] team, @NotNull TrainerBag bag, @NotNull BattleAI battleAI, @NotNull LivingEntity entity) {
        this.uuid = uuid;
        this.name = name;
        this.team = team;
        this.bag = bag;
        this.battleAI = battleAI;
        this.entity = entity;
        this.initTeam();
    }

    /**
     * Sets the {@link UUID} of this trainer.
     * 
     * @param uuid New {@link UUID}.
     */
    public void setUUID(UUID uuid) {
        this.uuid = uuid;
        this.initTeam();
    }

    /**
     * Sets the {@link LivingEntity} associated with this trainer.
     * 
     * @param entity {@link LivingEntity} to associate with this trainer.
     */
    public void setEntity(@NotNull LivingEntity entity) {
        this.entity = entity;
    }

    /**
     * Retrieves the {@link UUID} of this trainer.
     * 
     * @return {@link UUID} of this trainer.
     */
    @NotNull
    public UUID getUUID() {
        return this.uuid;
    }

    /**
     * Retrieves the {@link BattleAI} of this trainer.
     * 
     * @return Current {@link BattleAI}.
     */
    @NotNull
    public BattleAI getBattleAI() {
        return this.battleAI;
    }

    /**
     * Retrieves the {@link TrainerBag} of this trainer.
     * 
     * @return {@link TrainerBag} of the trainer.
     */
    @NotNull
    public TrainerBag getBag() {
        return this.bag;
    }

    @Override @NotNull
    public String getName() {
        return this.name;
    }

    @Override @NotNull
    public Pokemon[] getTeam() {
        return this.team;
    }

    @Override @NotNull
    public LivingEntity getEntity() {
        return this.entity;
    }

    private void initTeam() {
        for(var pkmn : this.team) {
            pkmn.setOriginalTrainer(this.uuid);
            pkmn.setOriginalTrainerName(this.getName());
            pkmn.setOriginalTrainerType$common(OriginalTrainerType.NPC);
        }
    }
}
