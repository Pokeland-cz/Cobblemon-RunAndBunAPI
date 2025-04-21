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
package com.gitlab.srcmc.rctapi.api.ai.config;

import com.gitlab.srcmc.rctapi.ModCommon;
import com.gitlab.srcmc.rctapi.api.ai.RCTBattleAI;
import com.gitlab.srcmc.rctapi.api.util.JTO;

public record RCTBattleAIConfig(
    Double moveBias,
    Double statusMoveBias,
    Double switchBias,
    Double itemBias,
    Double maxSelectMargin)
{
    private static final double DEFAULT_MOVE_BIAS = 1.0;
    private static final double DEFAULT_STATUS_MOVE_BIAS = 0.85;
    private static final double DEFAULT_SWITCH_BIAS = 0.65;
    private static final double DEFAULT_ITEM_BIAS = 0.85;
    private static final double DEFAULT_MAX_SELECT_MARGIN = 0.25;

    public RCTBattleAIConfig() {
        this(DEFAULT_MOVE_BIAS, DEFAULT_STATUS_MOVE_BIAS, DEFAULT_SWITCH_BIAS, DEFAULT_ITEM_BIAS, DEFAULT_MAX_SELECT_MARGIN);
    }

    public RCTBattleAIConfig {
        if(moveBias == null) moveBias = DEFAULT_MOVE_BIAS;
        if(statusMoveBias == null) statusMoveBias =  DEFAULT_STATUS_MOVE_BIAS;
        if(switchBias == null) switchBias =  DEFAULT_SWITCH_BIAS;
        if(itemBias == null) itemBias =  DEFAULT_ITEM_BIAS;
        if(maxSelectMargin == null) maxSelectMargin =  DEFAULT_MAX_SELECT_MARGIN;
    }

    /**
     * Registers the json parser for this config type. Needs to be called as early as
     * possible (e.g. in {@link ModCommon#init()}).
     */
    public static void register() {
        JTO.registerParser("rct", RCTBattleAI::new, RCTBattleAIConfig::new, RCTBattleAIConfig.class);
    }

    public static class Builder {
        private double moveBias = DEFAULT_MOVE_BIAS;
        private double statusMoveBias = DEFAULT_STATUS_MOVE_BIAS;
        private double switchBias = DEFAULT_SWITCH_BIAS;
        private double itemBias = DEFAULT_ITEM_BIAS;
        private double maxSelectMargin = DEFAULT_MAX_SELECT_MARGIN;

        public RCTBattleAIConfig.Builder withMoveBias(double moveBias) {
            this.moveBias = moveBias;

            return this;
        }
        public RCTBattleAIConfig.Builder withStatusMoveBias(double statusMoveBias) {
            this.statusMoveBias = statusMoveBias;

            return this;
        }
        public RCTBattleAIConfig.Builder withSwitchBias(double switchBias) {
            this.switchBias = switchBias;

            return this;
        }
        public RCTBattleAIConfig.Builder withItemBias(double itemBias) {
            this.itemBias = itemBias;

            return this;
        }
        public RCTBattleAIConfig.Builder withMaxSelectMargin(double maxSelectMargin) {
            this.maxSelectMargin = maxSelectMargin;

            return this;
        }

        public RCTBattleAIConfig build() {
            return new RCTBattleAIConfig(
                    this.moveBias,
                    this.statusMoveBias,
                    this.switchBias,
                    this.itemBias,
                    this.maxSelectMargin
            );
        }
    }
}

