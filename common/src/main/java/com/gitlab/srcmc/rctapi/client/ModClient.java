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
package com.gitlab.srcmc.rctapi.client;

public class ModClient {
    public static final BattleState BATTLE_STATE = new BattleState();

    public static class BattleState {
        private boolean dispatchesComplete;
        private boolean forceSwitch;

        public void reset() {
            this.forceSwitch = false;
            this.dispatchesComplete = false;
        }

        public void setDispatchesComplete(boolean dispatchesComplete) {
            this.dispatchesComplete = dispatchesComplete;
        }

        public boolean getDispatchesComplete() {
            return this.dispatchesComplete;
        }

        public void setForceSwitch() {
            this.forceSwitch = true;
        }

        public boolean getForceSwitch() {
            return this.forceSwitch;
        }
    }

    public static void init() {
    }
}
