package moze_intel.projecte.emc;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;

public class SimpleStack {
    public final ResourceLocation id;
    public final int meta;

    public SimpleStack(ResourceLocation id, int meta) {
        this.id = id;
        this.meta = meta;
    }

    public SimpleStack(ItemStack stack) {
        if (stack.isEmpty()) {
            id = new ResourceLocation("minecraft", "air");
            meta = 0;
        } else {
            id = stack.getItem().getRegistryName();
            meta = stack.getItemDamage();
        }
    }

    public SimpleStack withMeta(int meta) {
        return new SimpleStack(id, meta);
    }

    public boolean isValid() {
        return !id.equals(new ResourceLocation("minecraft", "air"));
    }

    public ItemStack toItemStack() {
        if (isValid()) {
            Item item = Item.REGISTRY.getObject(id);

            if (item != null)
                return new ItemStack(item, 1, meta);
        }

        return ItemStack.EMPTY;
    }

    @Override
    public int hashCode() {
        int hash = 31 * id.hashCode();
        if (this.meta == OreDictionary.WILDCARD_VALUE)
            hash = hash * 57 ^ this.meta;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SimpleStack other) {

            if (this.meta == OreDictionary.WILDCARD_VALUE || other.meta == OreDictionary.WILDCARD_VALUE)
                return this.id.equals(other.id);

            return this.id.equals(other.id) && this.meta == other.meta;
        }

        return false;
    }

    @Override
    public String toString() {
        Item obj = Item.REGISTRY.getObject(id);

        if (obj != null)
            return id + " " + meta;

        return "id:" + id + " damage:" + meta;
    }
}
