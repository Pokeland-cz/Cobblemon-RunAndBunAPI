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

import java.util.HashSet;
import java.util.Set;

import com.cobblemon.mod.common.client.battle.ClientBattleSide;

public class ModClient {
    public static final BattleState BATTLE_STATE = new BattleState();
    private static final long MIN_LOCK_TIME_MS = 4000;

    public static class BattleState {
        private Set<ClientBattleSide> locked = new HashSet<>();
        private boolean forced;
        private Thread unforce;

        public void lock(ClientBattleSide side) {
            this.locked.add(side);
            
            if(this.unforce != null) {
                this.unforce.interrupt();

                try {
                    this.unforce.join();
                } catch (InterruptedException e) {
                }
            }

            if(this.locked.size() < 2) {
                this.forced = true;
                this.unforce = new Thread(() -> {
                    try {
                        Thread.sleep(MIN_LOCK_TIME_MS);
                        this.forced = false;
                    } catch(InterruptedException e) {
                    }
                });

                this.unforce.start();
            }
        }

        public void unlock() {
            this.locked = new HashSet<>();
            this.forced = false;
        }

        public boolean isOpen() {
            return !this.forced && this.locked.size() < 2;
        }
    }

    public static void init() {
    }
}
