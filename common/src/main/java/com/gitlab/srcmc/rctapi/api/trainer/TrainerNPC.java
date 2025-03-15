/*
 * This file is part of Radical Cobblemon Trainers API.
 * Copyright (c) 2025, HDainester, All rights reserved.
 *
 * Radical Cobblemon Trainers API is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Radical Cobblemon Trainers API is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Radical Cobblemon Trainers API. If not, see <http://www.gnu.org/licenses/lgpl>.
 */
package com.gitlab.srcmc.rctapi.api.trainer;

import org.jetbrains.annotations.NotNull;
import com.cobblemon.mod.common.api.battles.model.ai.BattleAI;
import com.cobblemon.mod.common.pokemon.OriginalTrainerType;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.properties.UncatchableProperty;
import com.gitlab.srcmc.rctapi.ModCommon;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

/**
 * An ai trainer that is represented by an arbitrary {@link LivingEntity}.
 */
public class TrainerNPC implements Trainer {
    /**
     * A tag which is added to dummy entities of all {@link TrainerNPC}s registererd to
     * a {@link TrainerRegistry}.
     */
    public static final String DUMMY_TAG = ModCommon.MOD_ID + ":dummy";

    private String name;
    private Pokemon[] team;
    private TrainerBag bag;
    private BattleAI battleAI;
    private LivingEntity entity;

    /**
     * Constructs a new {@link TrainerNPC}.
     * 
     * @param name The name of the trainer.
     * @param team The {@link Pokemon} party of the trainer.
     * @param bag {@link TrainerBag} containing the items a trainer can use per battle.
     * @param battleAI {@link BattleAI} used by this trainer.
     * @param entity {@link LivingEntity} this trainer is (initially) attached to (ideally an entity that never dies).
     */
    public TrainerNPC(@NotNull String name, @NotNull Pokemon[] team, @NotNull TrainerBag bag, @NotNull BattleAI battleAI, @NotNull LivingEntity entity) {
        this.name = name;
        this.team = team;
        this.bag = bag;
        this.battleAI = battleAI;
        this.entity = entity;
    }

    /**
     * Constructs a new {@link TrainerNPC} with a deep copy of the {@link Pokemon} team
     * from other and a shallow copy of the remaining properties.
     * 
     * @param other {@link TrainerNPC} to copy.
     */
    public TrainerNPC(@NotNull TrainerNPC other) {
        this.name = other.name;
        this.team = copyTeam(other.team);
        this.bag = other.bag;
        this.battleAI = other.battleAI;
        this.entity = other.entity;
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

    void initTeam(String otId) {
        for(var pkmn : this.team) {
            pkmn.setOriginalTrainer(otId);
            pkmn.setOriginalTrainerName(this.getName());
            pkmn.setOriginalTrainerType$common(OriginalTrainerType.NPC);
            pkmn.getCustomProperties().add(UncatchableProperty.INSTANCE.uncatchable());
        }
    }

    //////////////////////////////////////////////
    //                  STATIC                  //
    //////////////////////////////////////////////

    private static LivingEntity dummyEntity;

    private static Pokemon[] copyTeam(Pokemon[] team) {
        var copy = new Pokemon[team.length];

        for(int i = 0; i < team.length; i++) {
            copy[i] = team[i].clone(true, null);
        }

        return copy;
    }

    public static LivingEntity getDummyEntity(MinecraftServer server) {
        if(dummyEntity == null || dummyEntity.getServer() != server || !dummyEntity.isAlive()) {
            if(dummyEntity != null) {
                dummyEntity.discard();
            }

            var dummy = EntityType.VILLAGER.create(server.overworld());
            dummy = EntityType.VILLAGER.create(server.overworld());
            dummy.addTag(TrainerNPC.DUMMY_TAG);
            dummy.setNoGravity(true);
            dummy.setInvulnerable(true);
            dummy.setInvisible(true);
            dummy.setNoAi(true);
            dummy.noPhysics = true;
            dummy.setPos(0, Integer.MAX_VALUE/2, 0);
            dummyEntity = dummy;
        }

        return dummyEntity;
    };
}
