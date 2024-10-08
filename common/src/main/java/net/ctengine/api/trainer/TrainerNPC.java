package net.ctengine.api.trainer;

import org.jetbrains.annotations.NotNull;
import com.cobblemon.mod.common.api.battles.model.ai.BattleAI;
import com.cobblemon.mod.common.pokemon.Pokemon;

import net.ctengine.api.ai.SelfdotGen5AI;
import net.ctengine.api.models.TrainerModel;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.MinecraftServer;

public class TrainerNPC implements Trainer {
    private BattleAI battleAI = new SelfdotGen5AI();
    private LivingEntity entity;
    private TrainerModel model;

    public TrainerNPC(@NotNull MinecraftServer server, @NotNull TrainerModel model) {
        this.model = model;
        this.entity = EntityType.VILLAGER.create(server.getOverworld());
    }

    public void setBattleAI(@NotNull BattleAI battleAI) {
        this.battleAI = battleAI;
    }

    public void setEntity(@NotNull LivingEntity entity) {
        this.entity = entity;
    }

    @NotNull
    public BattleAI getBattleAI() {
        return this.battleAI;
    }

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
