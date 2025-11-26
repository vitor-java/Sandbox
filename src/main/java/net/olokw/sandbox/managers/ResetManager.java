package net.olokw.sandbox.managers;

import net.olokw.sandbox.Sandbox;
import net.olokw.sandbox.configs.BlockPos;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

public class ResetManager {
    private final Set<BlockPos> resetBlocks;

    public ResetManager() {
        this.resetBlocks = new HashSet<>();
    }

    private BlockPos toBlockPos(Location location) {
        return new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public void addBlockLoc(Location location) {
        BlockPos blockPos = toBlockPos(location);
        resetBlocks.add(blockPos);

        new BukkitRunnable() {
            @Override
            public void run() {
                location.add(0.5, 0.5, 0.5);
                location.getWorld().spawnParticle(Particle.FLAME, location, 3, 0.1, 0.1, 0.1, 0);
                location.getBlock().setType(Material.AIR);
                resetBlocks.remove(blockPos);
                cancel();
            }
        }.runTaskLater(Sandbox.instance, 20 * 15);

    }

    public void removeBlockLoc(Location location) {
        resetBlocks.remove(toBlockPos(location));
    }

    public boolean checkBlockLoc(Location location) {
        return resetBlocks.contains(toBlockPos(location));
    }

    public boolean checkBlockLocAndRemove(Location location) {
        BlockPos pos = toBlockPos(location);
        boolean contains = resetBlocks.contains(pos);
        if (contains) resetBlocks.remove(pos);
        return contains;
    }

    public Set<Location> getBlocksLocationsAndClearSet() {
        Set<Location> locs = new HashSet<>();
        for (BlockPos pos : resetBlocks) {
            Location loc = new Location(Bukkit.getWorld("spawn"), pos.getX(), pos.getY(), pos.getZ());
            locs.add(loc);
        }
        resetBlocks.clear();
        return locs;
    }
}
