package net.ctengine.api.battle;

/**
 * Wrapper enum for supported battle formats provided by Cobblemon.
 */
public enum BattleFormat {
    GEN_9_SINGLES(com.cobblemon.mod.common.battles.BattleFormat.Companion.getGEN_9_SINGLES()),
    GEN_9_DOUBLES(com.cobblemon.mod.common.battles.BattleFormat.Companion.getGEN_9_DOUBLES()),
    GEN_9_TRIPLES(com.cobblemon.mod.common.battles.BattleFormat.Companion.getGEN_9_TRIPLES()),
    GEN_9_MULTI(com.cobblemon.mod.common.battles.BattleFormat.Companion.getGEN_9_MULTI()),
    GEN_9_ROYAL(com.cobblemon.mod.common.battles.BattleFormat.Companion.getGEN_9_ROYAL());

    private com.cobblemon.mod.common.battles.BattleFormat cobblemonBattleFormat;

    private BattleFormat(com.cobblemon.mod.common.battles.BattleFormat cobblemonBattleFormat) {
        this.cobblemonBattleFormat = cobblemonBattleFormat;
    }

    /**
     * Retrieves the {@link com.cobblemon.mod.common.battles.BattleFormat} this enum value refers to.
     * 
     * @return Cobblemon {@link com.cobblemon.mod.common.battles.BattleFormat}.
     */
    public com.cobblemon.mod.common.battles.BattleFormat getCobblemonBattleFormat() {
        return this.cobblemonBattleFormat;
    }
}
