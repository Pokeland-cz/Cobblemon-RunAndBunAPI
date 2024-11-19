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
package com.gitlab.srcmc.rctapi.api.ai.learning;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;
import com.cobblemon.mod.common.battles.ActiveBattlePokemon;
import com.cobblemon.mod.common.battles.InBattleMove;
import com.cobblemon.mod.common.battles.Targetable;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.item.battle.BagItem;

import com.gitlab.srcmc.rctapi.ModCommon;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

public class BattleMemory<T> extends SavedData {
    private static final Random RNG = new Random();
    private static final double RATING_MARGIN = 0.075;
    private static final String FILE = ModCommon.MOD_ID + ".lai.knowledge";

    private BattleEvaluator evaluator = new BattleEvaluator();
    private Map<String, RatedAction<T>> exactActions = new HashMap<>();
    private Map<String, RatedAction<T>> fuzzyActions = new HashMap<>();
    private BattleState previousState, currentState;
    private RatedAction<T> previousAction;

    public static <T> BattleMemory<T> load(MinecraftServer server) {
        return BattleMemory.load(server, BattleMemory.FILE);
    }

    public static <T> BattleMemory<T> load(MinecraftServer server, String path) {
        return server
            .overworld().getDataStorage()
            .computeIfAbsent(new Factory<BattleMemory<T>>(BattleMemory::new, BattleMemory::of, DataFixTypes.LEVEL), path);
    }

    public static <T> BattleMemory<T> of(CompoundTag tag, Provider provider) {
        var bm = new BattleMemory<T>();
        var exacts = tag.getCompound("exacts");
        var fuzzies = tag.getCompound("fuzzies");
        exacts.getAllKeys().forEach(key -> bm.exactActions.put(key, new RatedAction<>(exacts.getDouble(key))));
        fuzzies.getAllKeys().forEach(key -> bm.fuzzyActions.put(key, new RatedAction<>(fuzzies.getDouble(key))));
        return bm;
    }

    @Override
    public CompoundTag save(CompoundTag tag, Provider provider) {
        var exacts = new CompoundTag();
        var fuzzies = new CompoundTag();
        this.exactActions.entrySet().forEach(kv -> exacts.putDouble(kv.getKey(), kv.getValue().getRating()));
        this.fuzzyActions.entrySet().forEach(kv -> fuzzies.putDouble(kv.getKey(), kv.getValue().getRating()));
        tag.put("exacts", exacts);
        tag.put("fuzzies", fuzzies);
        return tag;
    }    

    /**
     * Call this to start another action selection process.
     */
    public void next(ActiveBattlePokemon pkmn) {
        this.currentState = BattleState.of(pkmn);

        if(this.previousAction != null) {
            this.evaluator.update(this.previousAction, this.previousState, this.currentState);
            this.setDirty();
        }

        this.previousState = this.currentState;
        this.previousAction = null;
    }

    /**
     * Retrieves the best added (or known) action since the last start of the action
     * selection process (@see BattleMemory#next()).
     */
    public T getChoice() {
        this.dump();
        ModCommon.LOG.info("## SELECTED ACTION RATING: " + this.previousAction.getRating());
        return this.previousAction.get();
    }

    /**
     * Switch action.
     */
    public RatedAction<T> getOrAdd(ActiveBattlePokemon from, BattlePokemon to, Supplier<T> supplier) {
        return this.storeIfBest(this.exactActions.compute(ActionKeys.switchKey(from, to), (k, v) -> {
            if(v == null) {
                return this.getOrAddFuzzy(ActionKeys.switchFuzzyKey(from, to), supplier);
            }

            return v.withSupplier(supplier);
        }));
    }

    /**
     * Item action.
     */
    public RatedAction<T> getOrAdd(BagItem item, BattlePokemon target, Supplier<T> supplier) {
        return this.storeIfBest(this.exactActions.compute(ActionKeys.itemKey(item, target), (k, v) -> {
            if(v == null) {
                return this.getOrAddFuzzy(ActionKeys.itemFuzzyKey(item, target), supplier);
            }

            return v.withSupplier(supplier);
        }));
    }

    /**
     * Move action.
     */
    public RatedAction<T> getOrAdd(ActiveBattlePokemon pkmn, InBattleMove move, Targetable target, Supplier<T> supplier) {
        return this.storeIfBest(this.exactActions.compute(ActionKeys.moveKey(pkmn, move, target), (k, v) -> {
            if(v == null) {
                return this.getOrAddFuzzy(ActionKeys.moveFuzzyKey(pkmn, move, target), supplier);
            }
            
            return v.withSupplier(supplier);
        }));
    }

    protected RatedAction<T> getOrAddFuzzy(String key, Supplier<T> supplier) {
        return this.fuzzyActions.compute(key, (k, v) -> {
            if(v == null) {
                return new RatedAction<>(supplier);
            }
            
            return v.withSupplier(supplier);
        });
    }

    protected RatedAction<T> storeIfBest(RatedAction<T> nextAction) {
        var r = nextAction.getRating();
        var m = RATING_MARGIN * RNG.nextDouble();
        ModCommon.LOG.info(String.format("prev: %s, next: %f, margin: %f, %b", this.previousAction == null ? "0" : String.valueOf(this.previousAction.getRating()), r, m, (this.previousAction == null || (r + m) > this.previousAction.getRating())));

        if(this.previousAction == null || (r + m) > this.previousAction.getRating()) {
            this.previousAction = nextAction;
        }

        return nextAction;
    }

    private void dump() {
        ModCommon.LOG.info("## EXACT:");

        for(var p : this.exactActions.entrySet()) {
            ModCommon.LOG.info(p.getKey() + ": " + p.getValue().getRating());
        }

        ModCommon.LOG.info("## FUZZY:");

        for(var p : this.fuzzyActions.entrySet()) {
            ModCommon.LOG.info(p.getKey() + ": " + p.getValue().getRating());
        }
    }
}
