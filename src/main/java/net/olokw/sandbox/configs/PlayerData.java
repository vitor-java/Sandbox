package net.olokw.sandbox.configs;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PlayerData {
    private Set<UUID> ignoring;
    private String[] lastKit;
    private boolean lastTeleportWasQueue = false;
    private Location backLocation = null;
    private Map<ArmorType, ArmorTrim> trimMap;
    // para bloquear jogadores q ficam dando back depois de morrer na fq porque isso eh mt chato

    public PlayerData(Set<UUID> ignoring, String[] lastKit, Map<ArmorType, ArmorTrim> trimMap) {
        this.lastKit = lastKit;
        this.ignoring = ignoring;
        this.trimMap = trimMap;
    }


    public Set<UUID> getIgnoring() {
        return ignoring;
    }

    public boolean isIgnoring(UUID target) {
        return ignoring.contains(target);
    }

    public void addIgnored(UUID ignored) {
        ignoring.add(ignored);
    }

    public void removeIgnored(UUID ignored) {
        ignoring.remove(ignored);
    }

    public void setLastKit(String[] lastKit) {
        this.lastKit = lastKit;
    }

    public String[] getLastKit() {
        return lastKit;
    }

    public boolean isLastTeleportWasQueue() {
        return lastTeleportWasQueue;
    }

    public void setLastTeleportWasQueue(boolean lastTeleportWasQueue) {
        this.lastTeleportWasQueue = lastTeleportWasQueue;
    }

    public Location getBackLocation() {
        return backLocation;
    }

    public void setBackLocation(Location backLocation) {
        this.backLocation = backLocation;
    }

    public enum ArmorType {
        HELMET,
        CHESTPLATE,
        LEGGINGS,
        BOOTS;

        public static ArmorType fromItem(ItemStack item) {
            if (item == null || item.getType() == null) return null;

            String typeName = item.getType().toString();

            if (typeName.endsWith("_HELMET")) return HELMET;
            if (typeName.endsWith("_CHESTPLATE")) return CHESTPLATE;
            if (typeName.endsWith("_LEGGINGS")) return LEGGINGS;
            if (typeName.endsWith("_BOOTS")) return BOOTS;

            return null;
        }
    }

    public void setTrim(ArmorType armorType, ArmorTrim trim) {
        trimMap.put(armorType, trim);
    }

    public ArmorTrim getTrim(ArmorType armorType) {
        return trimMap.get(armorType);
    }

    public Map<ArmorType, ArmorTrim> getTrimMap() {
        return trimMap;
    }
}
