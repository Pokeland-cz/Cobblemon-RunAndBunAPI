package net.ctengine.api.trainer;

import org.jetbrains.annotations.NotNull;
import com.cobblemon.mod.common.api.battles.model.ai.BattleAI;
import com.cobblemon.mod.common.pokemon.Pokemon;

import net.ctengine.api.ai.SelfdotGen5AI;
import net.ctengine.api.models.TrainerModel;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.MinecraftServer;

/**
 * An ai trainer that is represented by an arbitrary living entity.
 */
public class TrainerNPC implements Trainer {
    private BattleAI battleAI = new SelfdotGen5AI();
    private LivingEntity entity;
    private TrainerModel model;

    /**
     * Creates a new trainer with the given trainer model and instantiates a default
     * villager entity on the provided servers overworld (not spawned), which is
     * associated to the trainer.
     * 
     * @param server Minecraft server.
     * @param model Trainer model.
     */
    public TrainerNPC(@NotNull MinecraftServer server, @NotNull TrainerModel model) {
        this.model = model;
        this.entity = EntityType.VILLAGER.create(server.getOverworld());
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
     * Sets the entity associated to this trainer.
     * 
     * @param entity Entity to associate to this trainer.
     */
    public void setEntity(@NotNull LivingEntity entity) {
        this.entity = entity;
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
     * @return Trainer model.
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
        return this.model.getTeam().stream().map(pm -> pm.toPokemon()).toList().toArray(new Pokemon[0]);
    }

    @Override @NotNull
    public LivingEntity getEntity() {
        return this.entity;
    }
}
