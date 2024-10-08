package net.ctengine.api.battle;

import java.util.Collections;
import java.util.List;

import com.cobblemon.mod.common.battles.BattleSide;

public class BattleContext {
    private List<BattleParticipant> participants1;
    private List<BattleParticipant> participants2;
    private BattleSide battleSide1;
    private BattleSide battleSide2;
    private BattleFormat battleFormat;

    public BattleContext(List<BattleParticipant> participants1, List<BattleParticipant> participants2, BattleSide battleSide1, BattleSide battleSide2, BattleFormat battleFormat) {
        this.participants1 = participants1;
        this.participants2 = participants2;
        this.battleSide1 = battleSide1;
        this.battleSide2 = battleSide2;
        this.battleFormat = battleFormat;
    }

    public List<BattleParticipant> getParticipants1() {
        return Collections.unmodifiableList(this.participants1);
    }

    public List<BattleParticipant> getParticipants2() {
        return Collections.unmodifiableList(this.participants2);
    }

    public BattleSide getBattleSide1() {
        return this.battleSide1;
    }

    public BattleSide getBattleSide2() {
        return this.battleSide2;
    }

    public BattleFormat getBattleFormat() {
        return this.battleFormat;
    }
}
