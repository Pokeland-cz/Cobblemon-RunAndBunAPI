package net.ctengine.api.ai.learning;

public class BattleEvaluator {
    public static final double HEALTH_DIFF_WEIGHT = 0.8;
    public static final double STAT_BOOSTS_WEIGHT = 0.2;
    public static final double STATUS_EFFECTS_WEIGHT = 0.2;
    public static final double RATING_FACTOR = 0.5;
    public static final double RATING_DELTA_FACTOR = 0.85;

    public <T> void update(RatedAction<T> action, BattleState prevState, BattleState nextState) {
        action.addRating(
            RATING_FACTOR * ratingDelta(action) * levelDiffDelta(prevState) * partyCountDelta(prevState)
            * (healthDelta(prevState, nextState) + boostsDelta(prevState, nextState) + statusDelta(prevState, nextState)));
    }

    protected <T> double ratingDelta(RatedAction<T> action) {
        var mid = (RatedAction.MAX_RATING - RatedAction.MIN_RATING)/2;
        var rat = action.getRating() - RatedAction.MIN_RATING;
        return 1.0 - RATING_DELTA_FACTOR * Math.abs(rat - mid)/mid;
    }

    protected double levelDiffDelta(BattleState prevState) {
        return (BattleState.MAX_PKMN_LEVEL - Math.min(BattleState.MAX_PKMN_LEVEL, Math.abs(prevState.sourceAverageLevel - prevState.targetAverageLevel)))/BattleState.MAX_PKMN_LEVEL;
    }

    protected double partyCountDelta(BattleState prevState) {
        return Math.min(BattleState.MAX_PARTY_COUNT, prevState.sourcePartyCount)/(double)BattleState.MAX_PARTY_COUNT;
    }

    protected double healthDelta(BattleState prevState, BattleState nextState) {
        var sourceHealthDelta = (nextState.sourcePartyHealth - prevState.sourcePartyHealth)/(double)nextState.sourcePartyMaxHealth;
        var targetHealthDelta = (nextState.targetPartyHealth - prevState.targetPartyHealth)/(double)nextState.targetPartyMaxHealth;
        return HEALTH_DIFF_WEIGHT*(sourceHealthDelta - targetHealthDelta);
    }

    protected double boostsDelta(BattleState prevState, BattleState nextState) {
        var sourceStatBoostsDelta = Math.max(-BattleState.MAX_STAT_BOOSTS, Math.min(BattleState.MAX_STAT_BOOSTS, nextState.sourcePartyStatBoosts - prevState.sourcePartyStatBoosts));
        var targetStatBoostsDelta = Math.max(-BattleState.MAX_STAT_BOOSTS, Math.min(BattleState.MAX_STAT_BOOSTS, nextState.targetPartyStatBoosts - prevState.targetPartyStatBoosts));
        return STAT_BOOSTS_WEIGHT*(sourceStatBoostsDelta - targetStatBoostsDelta)/BattleState.MAX_STAT_BOOSTS;
    }

    protected double statusDelta(BattleState prevState, BattleState nextState) {
        var sourceStatEffectsDelta = (nextState.sourcePartyStatusEffects - prevState.sourcePartyStatusEffects)/nextState.sourcePartyCount;
        var targetStatEffectsDelta = (nextState.sourcePartyStatusEffects - prevState.targetPartyStatusEffects)/nextState.targetPartyCount;
        return STATUS_EFFECTS_WEIGHT*(sourceStatEffectsDelta - targetStatEffectsDelta);
    }
}
