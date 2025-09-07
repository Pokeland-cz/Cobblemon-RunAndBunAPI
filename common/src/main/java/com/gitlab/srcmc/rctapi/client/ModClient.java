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

import java.util.ArrayList;
import java.util.List;
import com.gitlab.srcmc.rctapi.ModCommon;
import net.minecraft.network.chat.Component;

public class ModClient {
    public static final BattleState BATTLE_STATE = new BattleState();

    public static class BattleState {
        private boolean dispatchesComplete;

        private List<String> recentMessages = new ArrayList<>();
        private boolean forceSwitch;
        private int fainted;

        public void reset() {
            ModCommon.LOG.info(":: BATTLE_STATE RESET");
            this.recentMessages = new ArrayList<>();
            this.forceSwitch = false;
            this.dispatchesComplete = false;
            this.fainted = 0;
        }

        public void setDispatchesComplete(boolean dispatchesComplete) {
            this.dispatchesComplete = dispatchesComplete;
        }

        public boolean getDispatchesComplete() {
            return this.dispatchesComplete;
        }

        public Iterable<String> getMessages() {
            return this.recentMessages;
        }

        public void setForceSwitch() {
            this.forceSwitch = true;
        }

        public boolean getForceSwitch() {
            return this.forceSwitch;
        }

        public void addMessages(Iterable<Component> message) {
            for(var m : message) {
                var s = m.toString();

                if(s.contains("cobblemon.battle.turn")) {
                    this.reset();
                }
                
                this.recentMessages.add(s);

                if(s.contains("cobblemon.battle.fainted")) {
                    ModCommon.LOG.info("MESSAGE: " + s);
                }
            }
        }

        public int getFainted() {
            return this.fainted;
        }

        public void addFainted() {
            this.fainted++;
        }
    }

    public static void init() {
    }
}
