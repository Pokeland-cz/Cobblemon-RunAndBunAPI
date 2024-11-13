package net.ctengine.api.ai.learning;

import com.cobblemon.mod.common.api.moves.Moves;
import com.cobblemon.mod.common.battles.ActiveBattlePokemon;
import com.cobblemon.mod.common.battles.InBattleMove;
import com.cobblemon.mod.common.battles.Targetable;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.item.battle.BagItem;

final class ActionKeys {
    // Format: s|<from_species>|<to_species>|<opposite_side_species>...
    static String switchKey(ActiveBattlePokemon from, BattlePokemon to) {
        return String.format("s|%s|%s|%s",
            !from.isAlive() ? null : from.getBattlePokemon().getOriginalPokemon().getSpecies().getName(),
            to.getOriginalPokemon().getSpecies().getName(),
            from.getSide().getOppositeSide().getActivePokemon().stream().filter(p -> p.isAlive()).map(p -> p.getBattlePokemon().getOriginalPokemon().getSpecies().getName()).reduce("", (n1, n2) -> n2 + "," + n1));
    }

    // Format: i|<item_name>|<pkmn_name>|<health(11 states)>
    static String itemKey(BagItem item, BattlePokemon target) {
        return String.format("i|%s|%s|%.1f",
            item.getItemName(),
            target.getOriginalPokemon().getSpecies().getName(),
            target.getHealth()/(float)target.getMaxHealth());
    }

    // Format: m|<pkmn_name>|<move_id>|<target_pnx>|<opposite_side_species>...
    static String moveKey(ActiveBattlePokemon pkmn, InBattleMove move, Targetable target) {
        return String.format("m|%s|%s|%s|%s",
            pkmn.getBattlePokemon().getOriginalPokemon().getSpecies().getName(),
            move.id,
            target != null ? target.getPNX() : "null",
            pkmn.getSide().getOppositeSide().getActivePokemon().stream().map(p -> p.getBattlePokemon().getOriginalPokemon().getSpecies().getName()).reduce("", (n1, n2) -> n2 + "," + n1));
    }

    static String switchFuzzyKey(ActiveBattlePokemon from, BattlePokemon to) {
        var fromTypes = new StringBuilder();
        var toTypes = new StringBuilder();
        var oppositeTypes = new StringBuilder();

        if(!from.isGone()) {
            // TODO: deterministic order for multiple types (e.g. alphabetic)
            for(var t : from.getBattlePokemon().getOriginalPokemon().getTypes()) {
                if(!fromTypes.isEmpty()) {
                    fromTypes.append(',');
                }

                fromTypes.append(t.getName());
            }
        } else {
            fromTypes.append("null");
        }

        // TODO: deterministic order for multiple types (e.g. alphabetic)
        for(var t : to.getOriginalPokemon().getTypes()) {
            if(!toTypes.isEmpty()) {
                toTypes.append(',');
            }

            toTypes.append(t.getName());
        }

        from.getSide().getOppositeSide().getActivePokemon().stream().filter(p -> p.isAlive()).map(p -> p.getBattlePokemon()).forEach(pkmn -> {
            // TODO: deterministic order for multiple types (e.g. alphabetic)
            for(var t : pkmn.getOriginalPokemon().getTypes()) {
                if(!oppositeTypes.isEmpty()) {
                    oppositeTypes.append(',');
                }

                oppositeTypes.append(t.getName());
            }
        });

        return String.format("sf|%s|%s|%s",
            fromTypes.toString(),
            toTypes.toString(),
            oppositeTypes.toString());
    }

    static String itemFuzzyKey(BagItem item, BattlePokemon target) {
        return String.format("if|%s|%.1f",
            item.getItemName(),
            target.getHealth()/(float)target.getMaxHealth());
    }

    static String moveFuzzyKey(ActiveBattlePokemon pkmn, InBattleMove move, Targetable target) {
        var fromTypes = new StringBuilder();
        var targetTypes = new StringBuilder();

        if(!pkmn.isGone()) {
            // TODO: deterministic order for multiple types (e.g. alphabetic)
            for(var t : pkmn.getBattlePokemon().getOriginalPokemon().getTypes()) {
                if(!fromTypes.isEmpty()) {
                    fromTypes.append(',');
                }

                fromTypes.append(t.getName());
            }
        } else {
            fromTypes.append("null");
        }

        if(target != null) {
            for(var obj : target.getActorPokemon()) {
                var targetPkmn = (ActiveBattlePokemon)obj;

                // TODO: deterministic order for multiple types (e.g. alphabetic)
                for(var t : targetPkmn.getBattlePokemon().getOriginalPokemon().getTypes()) {
                    if(!targetTypes.isEmpty()) {
                        targetTypes.append(',');
                    }

                    targetTypes.append(t.getName());
                }
            }
        } else {
            targetTypes.append("null");
        }

        return String.format("mf|%s|%s|%s",
            fromTypes.toString(),
            Moves.INSTANCE.getByName(move.id).getElementalType().getName(),
            targetTypes.toString());
    }

    private ActionKeys() {}
}
