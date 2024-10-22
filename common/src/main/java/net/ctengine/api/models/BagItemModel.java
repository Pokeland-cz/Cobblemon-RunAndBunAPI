package net.ctengine.api.models;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;

/**
 * A pojo class for parsing trainer {@link BagItem}s.
 */
public class BagItemModel {
    private String item = "";
    private int quantity = 1;

    @NotNull
    public String getItem() {
        return this.item;
    }

    public int getQuantity() {
        return this.quantity;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.item, this.quantity);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof BagItemModel other)
            && this.quantity == other.quantity
            && this.item.equals(other.item);
    }
}
