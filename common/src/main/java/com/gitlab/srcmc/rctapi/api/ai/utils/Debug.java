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
package com.gitlab.srcmc.rctapi.api.ai.utils;

import com.gitlab.srcmc.rctapi.ModCommon;

// Method calls can be easily stripped from bytecode (see https://gitlab.com/srcjava/jcut).
public final class Debug {
    // 0: no logging, 1: log ai choices, 2: log battle contexts, 3: log battle actions
    private final static int LEVEL = 1;

    public static void log(Action action) {
        log(0, action);
    }

    public static void log(int level, Action action) {
        if(LEVEL >= level) {
            action.perform();
        }
    }

    public static void log(String format, Object... args) {
        log(0, format, args);
    }

    public static void log(int level, String format, Object... args) {
        if(LEVEL >= level) {
            ModCommon.LOG.info(String.format(format, args));
        }
    }

    public interface Action {
        void perform();
    }

    private Debug() {
    }
}
