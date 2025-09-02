package com.gitlab.srcmc.rctapi;

import java.util.List;
import java.util.Objects;

import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;

public class DebugState {
    public static DebugState INSTANCE = new DebugState();
    public static int ticks;
    
    public int dispatches;
    public int afterDispatches;
    public boolean wasStuck;
    public List<BattleActor> actors = List.of();

    @Override
    public boolean equals(Object o) {
        return o instanceof DebugState s
            && this.dispatches == s.dispatches
            && this.afterDispatches == s.afterDispatches
            && this.wasStuck == s.wasStuck
            && this.actors.size() == s.actors.size()
            && equalActors(this.actors, s.actors);
    }

    static boolean equalActors(List<BattleActor> arg0, List<BattleActor> arg1) {
        if(arg0.size() != arg1.size()) {
            return false;
        }

        for(int i = 0; i < arg0.size(); i++) {
            var a0 = arg0.get(i);
            var a1 = arg1.get(i);

            if(a0.getResponses().size() != a1.getResponses().size()
            || a0.getMustChoose() != a1.getMustChoose()
            || !Objects.equals(a0.getRequest(), a1.getRequest())) {
                return false;
            }
        }

        return true;
    }
}