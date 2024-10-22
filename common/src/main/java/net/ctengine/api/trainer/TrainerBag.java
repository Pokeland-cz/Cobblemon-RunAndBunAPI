package net.ctengine.api.trainer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.cobblemon.mod.common.api.item.PokemonSelectingItem;
import com.cobblemon.mod.common.item.battle.BagItem;
import com.cobblemon.mod.common.item.battle.BagItemLike;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

/**
 * A collection of {@link BagItem}s to be used in battles.
 */
public class TrainerBag {
    private Map<BagItem, Integer> items = new HashMap<>();

    /**
     * Adds a new {@link BagItem} to the trainer bag.
     * 
     * @param itemId Resource location of the item (e.g. "cobblemon:potion").
     * @param quantity Quantity of the item to add.
     * @throws IllegalArgumentException If no such item exists.
     * @throws IllegalArgumentException If the item is neither a {@link BagItemLike} or {@link PokemonSelectingItem}.
     */
    public void add(String itemId, int quantity) {
        if(quantity > 0) {
            var itemLocation = ResourceLocation.parse(itemId);

            if(!BuiltInRegistries.ITEM.containsKey(itemLocation)) {
                throw new IllegalArgumentException("invalid item '" + itemId + "'");
            }

            var item = BuiltInRegistries.ITEM.get(itemLocation);

            if(item instanceof BagItemLike bagItemLike) {
                this.items.put(bagItemLike.getBagItem(item.getDefaultInstance()), quantity);
            } else if(item instanceof PokemonSelectingItem selectingItem) {
                this.items.put(selectingItem.getBagItem(), quantity);
            } else {
                throw new IllegalArgumentException("'" + itemId + "' is neither a BagItemLike or PokemonSelectingItem");
            }
        }
    }

    /**
     * Uses a {@link BagItem} from this bag effectively decrementing its quantity. If
     * the quantity reaches 0 or less the item is removed. Does nothing if the item is
     * not from this bag.
     * 
     * @param item {@link BagItem} to use.
     * @return The given {@link BagItem}.
     */
    public BagItem use(BagItem item) {
        this.items.computeIfPresent(item, (k, v) -> v > 1 ? v - 1 : null);
        return item;
    }

    /**
     * Retrieves a readonly set of all {@link BagItem}s within this bag.
     * 
     * @return Readonly set of {@link BagItem}.
     */
    public Set<BagItem> getItems() {
        return this.items.keySet();
    }

    /**
     * Retrieves the quantity of the given {@link BagItem} within this bag.
     * 
     * @return Quantity of the given {@link BagItem}.
     */
    public int getQuanity(BagItem item) {
        return this.items.getOrDefault(item, 0);
    }

    /**
     * Creates a new trainer bag that contains the same {@link BagItem}s and quantities
     * as this bag.
     * 
     * @return New cloned bag.
     */
    public TrainerBag clone() {
        var bag = new TrainerBag();
        bag.items.putAll(this.items);
        return bag;
    }
}
