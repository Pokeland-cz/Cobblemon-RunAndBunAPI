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
import com.gitlab.srcmc.rctapi.api.ai.experimental.SelfdotGen5AI;
import com.gitlab.srcmc.rctapi.api.util.JTO;

public record SelfdotGen5AIConfig() {
    /**
     * Registers the json parser for this config type. Needs to be called as early as
     * possible (e.g. in {@link ModCommon#init()}).
     */
    public static void register() {
        JTO.registerParser("sd5", m -> new SelfdotGen5AI(), SelfdotGen5AIConfig::new, SelfdotGen5AIConfig.class);
    }
}
