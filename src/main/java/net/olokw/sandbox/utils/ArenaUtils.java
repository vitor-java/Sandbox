package net.olokw.sandbox.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class ArenaUtils {

    // basicamente checa se o cara NÃO está no spawn.

    public boolean isInsideArena(Location location) {
        if (location.getY() > 106) {
            Location spawnCenter = new Location(Bukkit.getWorld("spawn"), 0.5, 106, 0.5);
            int spawnRadius = 95;

            double dx = location.getX() - spawnCenter.getX();
            double dz = location.getZ() - spawnCenter.getZ();

            return (dx * dx + dz * dz) > (spawnRadius * spawnRadius);
        } else {
            return true;
        }
    }

}
