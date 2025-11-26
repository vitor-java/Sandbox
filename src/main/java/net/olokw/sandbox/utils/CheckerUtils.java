package net.olokw.sandbox.utils;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class CheckerUtils {
    public void checkAndRemoveBlast(ItemStack item) {
        if (item == null) return;
        String material = item.getType().toString();
        if (!material.endsWith("_HELMET")
                && !material.endsWith("_CHESTPLATE")
                && !material.endsWith("_BOOTS")) {
            return;
        }
        if (item.getEnchantments().containsKey(Enchantment.BLAST_PROTECTION)) {
            item.removeEnchantment(Enchantment.BLAST_PROTECTION);
            item.addEnchantment(Enchantment.PROTECTION, 4);
        }
    }
}
