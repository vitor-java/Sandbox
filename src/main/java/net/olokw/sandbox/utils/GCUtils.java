package net.olokw.sandbox.utils;

import net.olokw.sandbox.Sandbox;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class GCUtils { // garbage collector
    public void startGarbageCollector() {
        String[] worlds = {"rtp", "rtp1", "rtp2"};
        Bukkit.getScheduler().runTaskTimer(Sandbox.instance, () -> {
            for (String w : worlds) {
                World world = Bukkit.getWorld(w);
                if (world == null) continue;
                List<double[]> playerPositions = new ArrayList<>();
                for (Player player : world.getPlayers()) {
                    playerPositions.add(new double[]{player.getX(), player.getZ()});
                }

                for (EnderCrystal crystal : world.getEntitiesByClass(EnderCrystal.class)) {
                    if (!isPlayerNearbyHorizontal(crystal.getX(), crystal.getZ(), playerPositions, 64)) {
                        crystal.remove();
                    }
                }

                for (ArmorStand stand : world.getEntitiesByClass(ArmorStand.class)) {
                    if (!isPlayerNearbyHorizontal(stand.getX(), stand.getZ(), playerPositions, 64)) {
                        stand.remove();
                    }
                }
            }
        }, 20L * 30, 20L * 30);
    }


    private boolean isPlayerNearbyHorizontal(double x, double z, List<double[]> playerPositions, double radius) {
        double r2 = radius * radius;
        for (double[] pos : playerPositions) {
            double dx = pos[0] - x;
            double dz = pos[1] - z;
            if (dx * dx + dz * dz <= r2) {
                return true;
            }
        }
        return false;
    }

}
