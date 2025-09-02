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
package com.gitlab.srcmc.rctapi.mixins;

import java.util.ArrayList;
import java.util.Collections;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.cobblemon.mod.common.api.battles.model.PokemonBattle;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.net.NetworkPacket;
import com.cobblemon.mod.common.battles.ActiveBattlePokemon;
import com.cobblemon.mod.common.battles.BattleFormat;
import com.cobblemon.mod.common.battles.ForfeitActionResponse;
import com.cobblemon.mod.common.battles.ShowdownActionResponse;
import com.cobblemon.mod.common.net.messages.client.battle.BattleMakeChoicePacket;
import com.cobblemon.mod.common.net.messages.client.battle.BattleQueueRequestPacket;
import com.cobblemon.mod.common.net.messages.client.battle.BattleSwapPokemonPacket;
import com.gitlab.srcmc.rctapi.DebugState;
import com.gitlab.srcmc.rctapi.ModCommon;
import com.gitlab.srcmc.rctapi.api.ai.utils.BattleStates;
import com.gitlab.srcmc.rctapi.api.battle.BattleState;
import com.google.common.collect.Streams;

import kotlin.Pair;
import kotlin.Unit;

@Mixin(PokemonBattle.class)
public abstract class PokemonBattleMixin {
    private static final int MAX_STUCK_COUNT = 2;

    @Shadow(remap = false)
    abstract BattleFormat getFormat();

    @Shadow(remap = false)
    abstract Pair<BattleActor, ActiveBattlePokemon> getActorAndActiveSlotFromPNX(String pnx);

    @Shadow(remap = false)
    abstract boolean checkForfeit();

    // private int _ticks, _stuck;

    // @Inject(method = "tick", at = @At("TAIL"), remap = false)
    // private void injectTick(CallbackInfo ci) {
    //     var self = (PokemonBattle)(Object)this;
    //     if(!self.getStarted()) return;

    //     if(((this._ticks++)) % 10 == 0) {
    //         var actors = Streams.stream(self.getActors()).filter(a -> a.getPokemonList().stream().anyMatch(p -> p.getHealth() > 0)).toList();
    //         var state = new DebugState();
    //         state.dispatches = self.getDispatches().size();
    //         state.afterDispatches = self.getAfterDispatches().size();
    //         state.actors = actors;

    //         if(!state.equals(DebugState.INSTANCE)) {
    //             ModCommon.LOG.info(String.format(":::: dispatches: %d, afterDispatches: %d, stuck: %b",
    //                 self.getDispatches().size(),
    //                 self.getAfterDispatches().size(),
    //                 state.wasStuck
    //             ));

    //             actors.forEach(a -> ModCommon.LOG.info(String.format("  %s[request: %s, responses: %d, mustChoose: %b, active: %d, alive: %d]",
    //                 a.getName().getString(),
    //                 a.getRequest() != null ? a.getRequest().getClass().getSimpleName() : "<null>",
    //                 a.getResponses().size(),
    //                 a.getMustChoose(),
    //                 a.getActivePokemon().size(),
    //                 a.getPokemonList().stream().filter(p -> p.getHealth() > 0).count()
    //             )));

    //             DebugState.INSTANCE = state;
    //         }

    //         // if(self.getDispatches().size() == 0 && self.getAfterDispatches().size() == 0) {
    //         //     if((this._stuck++) > 2) {
    //         //         self.checkForInputDispatch();
    //         //         this._stuck = 0;
    //         //     }
    //         // }
    //     }
    // }
        
    //     if(bs.ticks() % 40 == 0) {
    // @Inject(method = "tick", at = @At("TAIL"), remap = false)
    // private void injectTick(CallbackInfo ci) {
    //     var self = (PokemonBattle)(Object)this;
    //     if(!self.getStarted()) return;
    //     var bs = BattleStates.get(self);
        
    //     if(bs.ticks() % 40 == 0) {
    //         var actors = Streams.stream(self.getActors()).filter(a -> a.getPokemonList().stream().anyMatch(p -> p.getHealth() > 0)).toList();

    //         var state = new DebugState();
    //         state.dispatches = self.getDispatches().size();
    //         state.afterDispatches = self.getAfterDispatches().size();
    //         state.actors = actors;

    //         if(!state.equals(DebugState.INSTANCE)) {
    //             ModCommon.LOG.info(String.format(":::: dispatches: %d, afterDispatches: %d, stuck: %b",
    //                 self.getDispatches().size(),
    //                 self.getAfterDispatches().size(),
    //                 state.wasStuck
    //             ));

    //             actors.forEach(a -> ModCommon.LOG.info(String.format("  %s[request: %s, responses: %d, mustChoose: %b, active: %d, alive: %d]",
    //                 a.getName().getString(),
    //                 a.getRequest() != null ? a.getRequest().getClass().getSimpleName() : "<null>",
    //                 a.getResponses().size(),
    //                 a.getMustChoose(),
    //                 a.getActivePokemon().size(),
    //                 a.getPokemonList().stream().filter(p -> p.getHealth() > 0).count()
    //             )));

    //             DebugState.INSTANCE = state;
    //         }

    //         if(self.getDispatches().isEmpty() && self.getAfterDispatches().isEmpty() && bs.getStuckCount() > bs.getMaxStuckCount()) {
    //             actors.stream().filter(a -> a.getMustChoose() && (a.getRequest() == null || a.getResponses().size() < a.getActivePokemon().size())).forEach(a -> {
    //                 ModCommon.LOG.info(String.format(":::: RESPONSE STATE: %s [request: %s, responses: %d, mustChoose: %b, active: %d, alive: %d]",
    //                     a.getName().getString(),
    //                     a.getRequest() != null ? a.getRequest().getClass().getSimpleName() : "<null>",
    //                     a.getResponses().size(),
    //                     a.getMustChoose(),
    //                     a.getActivePokemon().size(),
    //                     a.getPokemonList().stream().filter(p -> p.getHealth() > 0).count()
    //                 ));

    //                 a.getBattle().dispatchGo(() -> {
    //                     if(a.getRequest() != null) {
    //                         ModCommon.LOG.info(":::: => RESENDING CHOICE REQUEST: active: " + a.getActivePokemon().size() + ", alive: " + a.getActivePokemon().stream().filter(p -> p.isAlive()).count());
    //                         // a.sendUpdate(new BattleQueueRequestPacket(a.getRequest()));
    //                         a.sendUpdate(new BattleMakeChoicePacket());
    //                     }

    //                     return Unit.INSTANCE;
    //                 });
    //             });

    //             actors.stream()
    //                 .filter(a -> a.getMustChoose() && (a.getRequest() == null || a.getResponses().size() >= a.getActivePokemon().size()))
    //                 .forEach(a -> {
    //                     ModCommon.LOG.info(":::: FORCED UNSET MUSTCHOOSE: " + a.getName().getString());
    //                     a.setMustChoose(false);
    //                 });

    //             self.checkForInputDispatch();
    //             bs.setMaxStuckCount(bs.getMaxStuckCount() + 2);
    //             bs.unstuck();
                
    //             state.wasStuck = false;
    //         } else if(bs.getStuckCount() <= bs.getMaxStuckCount()) {
    //             bs.wasStuck();
    //             state.wasStuck = bs.getStuckCount() >= bs.getMaxStuckCount();
    //         } else {
    //             bs.setMaxStuckCount(MAX_STUCK_COUNT);
    //             bs.unstuck();
    //         }
    //     }

    //     bs.tick();
    // }

    // @Inject(method = "tick", at = @At("TAIL"), remap = false)
    // private void injectTick(CallbackInfo ci) {
    //     var self = (PokemonBattle)(Object)this;
    //     if(!self.getStarted()) return;
    //     var bs = BattleStates.get(self);
        
    //     if(bs.ticks() % 20 == 0) {
    //         var actors = Streams.stream(self.getActors()).filter(a -> a.getPokemonList().stream().anyMatch(p -> p.getHealth() > 0)).toList();

    //         if(self.getDispatches().isEmpty() && self.getAfterDispatches().isEmpty()) {
    //             actors.forEach(a -> {
    //                 var handlers = bs.getActorState(a).getPostUpkeepHandlers();

    //                 while(!handlers.isEmpty()) {
    //                     ModCommon.LOG.info("RUNNING POST UPKEEP HANDLER: " + a.getName().getString());
    //                     handlers.poll().run();

    //                     a.getBattle().dispatchGo(() -> {
    //                         if(a.getRequest() != null) {
    //                             ModCommon.LOG.info("===> RESENDING CHOICE REQUEST: active: " + a.getActivePokemon().stream().filter(p -> p.isAlive()).count());
    //                             a.sendUpdate(new BattleQueueRequestPacket(a.getRequest()));
    //                             a.sendUpdate(new BattleMakeChoicePacket());
    //                         }

    //                         return Unit.INSTANCE;
    //                     });
    //                 }
    //             });

    //             actors.stream().filter(a -> a.getMustChoose() && (!a.getResponses().isEmpty() || a.getRequest() == null)).forEach(a -> {
    //                 ModCommon.LOG.info("FORCED UNSET MUSTCHOOSE " + a.getName().getString());
    //                 ModCommon.LOG.info(String.format("::: RESPONSE STATE: %s [request: %s, responses: %d, mustChoose: %b, active: %d, alive: %d]",
    //                     a.getName().getString(),
    //                     a.getRequest() != null ? a.getRequest().getClass().getSimpleName() : "<null>",
    //                     a.getResponses().size(),
    //                     a.getMustChoose(),
    //                     a.getActivePokemon().size(),
    //                     a.getPokemonList().stream().filter(p -> p.getHealth() > 0).count()
    //                 ));

    //                 a.getBattle().dispatchGo(() -> {
    //                     if(a.getRequest() != null) {
    //                         ModCommon.LOG.info("===> RESENDING CHOICE REQUEST: active: " + a.getActivePokemon().stream().filter(p -> p.isAlive()).count());
    //                         a.sendUpdate(new BattleQueueRequestPacket(a.getRequest()));
    //                         a.sendUpdate(new BattleMakeChoicePacket());
    //                     }

    //                     return Unit.INSTANCE;
    //                 });

    //                 a.setMustChoose(false);
    //             });

    //             self.checkForInputDispatch();
    //         }
    //     }

    //     bs.tick();
    // }

    // @Inject(method = "tick", at = @At("TAIL"), remap = false)
    // private void injectTick(CallbackInfo ci) {
    //     var self = (PokemonBattle)(Object)this;
    //     if(!self.getStarted()) return;
    //     var bs = BattleStates.get(self);
        
    //     if(bs.ticks() % 30 == 0) {
    //         var actors = Streams.stream(self.getActors()).filter(a -> a.getPokemonList().stream().anyMatch(p -> p.getHealth() > 0)).toList();

    //         if(self.getDispatches().isEmpty() && self.getAfterDispatches().isEmpty() && actors.stream().anyMatch(a -> a.getMustChoose() && (!a.getResponses().isEmpty() || a.getRequest() == null))) {
    //             if(bs.getStuckCount() > MAX_STUCK_COUNT) {
    //                 ModCommon.LOG.warn("------------------ BATTLE UNSTUCK ------------------");

    //                 actors.stream()
    //                     .filter(a -> a.getMustChoose() && a.getRequest() != null)
    //                     .forEach(a -> {
    //                         ModCommon.LOG.warn(String.format("::> RESENDING BattleQueueRequestPacket to %s", a.getName().getString()));
    //                         a.sendUpdate(new BattleQueueRequestPacket(a.getRequest()));
    //                         a.sendUpdate(new BattleMakeChoicePacket());
    //                         a.setMustChoose(false);
    //                     });

    //                 actors.stream()
    //                     .filter(a -> a.getMustChoose() && (!a.getResponses().isEmpty() || a.getRequest() == null))
    //                     .forEach(a -> a.setMustChoose(false));

    //                 self.checkForInputDispatch();
    //                 bs.unstuck();
    //             } else if(!bs.updateResponseStates(actors)) {
    //                 bs.wasStuck();
    //                 // ModCommon.LOG.info("WAS STUCK, " + bs.getStuckCount());
    //             } else {
    //                 actors.forEach(a -> ModCommon.LOG.info(String.format("::: RESPONSE STATE:  %s[request: %s, responses: %d, mustChoose: %b, active: %d, alive: %d]",
    //                     a.getName().getString(),
    //                     a.getRequest() != null ? a.getRequest().getClass().getSimpleName() : "<null>",
    //                     a.getResponses().size(),
    //                     a.getMustChoose(),
    //                     a.getActivePokemon().size(),
    //                     a.getPokemonList().stream().filter(p -> p.getHealth() > 0).count()
    //                 )));

    //                 // ModCommon.LOG.info("NOT STUCK");
    //                 bs.unstuck();
    //             }
    //         }
    //     }

    //     bs.tick();
    // }

    // @Inject(method = "tick", at = @At("TAIL"), remap = false)
    // private void injectTick(CallbackInfo ci) {
    //     var self = (PokemonBattle)(Object)this;

    //     if(!self.getStarted()) {
    //         DebugState.ticks = 0;
    //     } else if(DebugState.ticks++ % 10 == 0) {
    //         var actors = new ArrayList<BattleActor>();
    //         var it = self.getActors().iterator();

    //         while (it.hasNext()) {
    //             var a = it.next();

    //             if(a.getPokemonList().stream().anyMatch(p -> p.getHealth() > 0)) {
    //                 actors.add(a);
    //             }
    //         }

    //         var state = new DebugState();
    //         state.dispatches = self.getDispatches().size();
    //         state.afterDispatches = self.getAfterDispatches().size();
    //         state.actors = actors;

    //         if(!state.equals(DebugState.INSTANCE)) {
    //             ModCommon.LOG.info(String.format("dispatches: %d, afterDispatches: %d",
    //                 self.getDispatches().size(),
    //                 self.getAfterDispatches().size()
    //             ));

    //             actors.forEach(a -> ModCommon.LOG.info(String.format("  %s[request: %s, responses: %d, mustChoose: %b, active: %d, alive: %d]",
    //                 a.getName().getString(),
    //                 a.getRequest() != null ? a.getRequest().getClass().getSimpleName() : "<null>",
    //                 a.getResponses().size(),
    //                 a.getMustChoose(),
    //                 a.getActivePokemon().size(),
    //                 a.getPokemonList().stream().filter(p -> p.getHealth() > 0).count()
    //             )));

    //             DebugState.INSTANCE = state;
    //         }

    //         if(state.dispatches == 0 && state.afterDispatches == 0) {
    //             if(DebugState.wasStuck) {
    //                 ModCommon.LOG.warn("------------------ BATTLE UNSTUCK ------------------");

    //                 actors.stream()
    //                     .filter(a -> a.getMustChoose() && a.getResponses().isEmpty() && a.getRequest() != null)
    //                     .forEach(a -> {
    //                         var request = a.getRequest();

    //                         ModCommon.LOG.warn(String.format("RESENDING BattleQueueRequestPacket to %s", a.getName().getString()));
    //                         self.dispatchGo(() -> {
    //                             a.sendUpdate(new BattleQueueRequestPacket(request));
    //                             a.sendUpdate(new BattleMakeChoicePacket());
    //                             return Unit.INSTANCE;
    //                         });
    //                     });

    //                 DebugState.wasStuck = false;
    //             } else
    //             // if(actors.stream().allMatch(a -> !a.getResponses().isEmpty())
    //             // && actors.stream().anyMatch(a -> a.getMustChoose())) {
    //             if(actors.stream().anyMatch(a -> a.getMustChoose() && (!a.getResponses().isEmpty() || a.getRequest() == null))) {
    //                 // battle is stuck
    //                 ModCommon.LOG.warn("------------------- BATTLE STUCK -------------------");
    //                 actors.stream().forEach(a -> a.setMustChoose(false));
    //                 self.checkForInputDispatch();
    //                 DebugState.wasStuck = true;
    //             }
    //         }
    //     }
    // }

    @Inject(method = "checkForInputDispatch", at = @At("HEAD"), remap = false, cancellable = true)
    private void injectCheckForInputDispatch(CallbackInfo ci) {
        var self = (PokemonBattle)(Object)this;
        if(this.checkForfeit()) return;

        var actors = Streams.stream(self.getActors()).filter(a -> a.getPokemonList().stream().anyMatch(p -> p.getHealth() > 0)).toList();        
        var readyToInput = actors.stream().anyMatch(a -> !a.getMustChoose() && !a.getResponses().isEmpty()) && actors.stream().noneMatch(a -> a.getMustChoose());

        if(readyToInput && self.getCaptureActions().isEmpty()) {
            actors.stream()
                .filter(a -> !a.getResponses().isEmpty())
                .forEach(a -> {
                    a.writeShowdownResponse();
                    a.getResponses().clear();
                    a.setRequest(null);
                });
        }

        ci.cancel();
    }

    /**
     * Swaps pokemon in pokemonList of actors whenever they swap positions on
     * field (e.g. by 'Ally Switch'). Fixes issues with follow up switch responses.
     * 
     * Note that this is not synchronized with clients. It does not appear to be
     * an issue other than in triple battles. Although 'Ally Switch' appears to
     * cause issues in triples anyway (so I'll leave it at that for now).
     */
    @Inject(method = "sendUpdate", at = @At("HEAD"), remap = false)
    private void injectSendUpdate(NetworkPacket<?> packet, CallbackInfo ci) {
        if(BattleState.findFirst((PokemonBattle)(Object)this) != null) {
            if(packet instanceof BattleSwapPokemonPacket swPacket) {
                if(this.getFormat().component2().getPokemonPerSide() < 3) {
                    var pnxB = swPacket.getPnx(); // positions have already been swapped (so this actually refers to the target)
                    var actorAndSlotB = this.getActorAndActiveSlotFromPNX(pnxB);
                    
                    var actor = actorAndSlotB.component1();
                    var pkmnB = actorAndSlotB.component2();
                    var pkmnA = (ActiveBattlePokemon)pkmnB.getAdjacentAllies().stream().findFirst().get();
                    var actorPkmn = actor.getPokemonList();
                    Collections.swap(actorPkmn, actorPkmn.indexOf(pkmnA.getBattlePokemon()), actorPkmn.indexOf(pkmnB.getBattlePokemon()));
                    // ModCommon.LOG.info(String.format("==> SWAP FIX: pnx: %s, %s <=> %s", pnxB, pkmnA.getBattlePokemon().getName().getString(), pkmnB.getBattlePokemon().getName().getString()));
                } else {
                    // Ally switch in triple battles kinda messes things up.
                    // Applying this 'fix' here does not help the cause.
                    // ModCommon.LOG.info("==> SWAP FIX SKIPPED (too many pokemon per side)");
                }
            }
        }
    }
}
