package net.ctengine.api.models;

import java.util.Objects;

/**
 * A pojo class for parsing trainer bag items.
 */
public class BagItemModel {
    private String item = "";
    private int quantity = 1;

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
