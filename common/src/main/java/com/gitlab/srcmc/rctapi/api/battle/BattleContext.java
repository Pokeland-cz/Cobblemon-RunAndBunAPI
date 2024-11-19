/*
 * This file is part of Radical Cobblemon Trainers API.
 * Copyright (c) 2024, HDainester, All rights reserved.
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
package com.gitlab.srcmc.rctapi.api.battle;

import java.util.Collections;
import java.util.List;

import com.cobblemon.mod.common.battles.BattleSide;

import com.gitlab.srcmc.rctapi.api.trainer.Trainer;

/**
 * Contains information relevant to a {@link PokemonBattle}.
 */
public class BattleContext {
    private List<Trainer> participants1;
    private List<Trainer> participants2;
    private BattleSide battleSide1;
    private BattleSide battleSide2;
    private BattleFormat battleFormat;

    /**
     * Creates a new battle context.
     * 
     * @param participants1 List of {@link Trainer} participants from the first side.
     * @param participants2 List of {@link Trainer} participants from the other side.
     * @param battleSide1 {@link BattleSide} instance of the first side.
     * @param battleSide2 {@link BattleSide} instance of the other side.
     * @param battleFormat {@link BattleFormat} to use.
     */
    public BattleContext(List<Trainer> participants1, List<Trainer> participants2, BattleSide battleSide1, BattleSide battleSide2, BattleFormat battleFormat) {
        this.participants1 = participants1;
        this.participants2 = participants2;
        this.battleSide1 = battleSide1;
        this.battleSide2 = battleSide2;
        this.battleFormat = battleFormat;
    }

    /**
     * Retrieves all {@link Trainer} participants of the first side.
     * 
     * @return List of {@link Trainer} participants.
     */
    public List<Trainer> getParticipants1() {
        return Collections.unmodifiableList(this.participants1);
    }

    /**
     * Retrieves all {@link Trainer} participants of the second side.
     * 
     * @return List of {@link Trainer} participants.
     */
    public List<Trainer> getParticipants2() {
        return Collections.unmodifiableList(this.participants2);
    }

    /**
     * Retrieves the {@link BattleSide} instance of the first side.
     * 
     * @return {@link BattleSide} instance.
     */
    public BattleSide getBattleSide1() {
        return this.battleSide1;
    }

    /**
     * Retrieves the {@link BattleSide} instance of the second side.
     * 
     * @return {@link BattleSide} instance.
     */
    public BattleSide getBattleSide2() {
        return this.battleSide2;
    }

    /**
     * Retrieves the {@link BattleFormat} to use.
     * 
     * @return {@link BattleFormat} instance.
     */
    public BattleFormat getBattleFormat() {
        return this.battleFormat;
    }
}
