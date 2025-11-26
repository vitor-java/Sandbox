package net.olokw.sandbox.utils;

import net.kyori.adventure.text.Component;
import net.olokw.sandbox.Sandbox;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ActionBarUtils {
    Map<UUID, BukkitTask> activeTasks;

    public ActionBarUtils() {
        this.activeTasks = new HashMap<>();
    }

    public void startActionBar(Player p, Component warning) {
        UUID uuid = p.getUniqueId();
        if (activeTasks.containsKey(uuid)) {
            activeTasks.get(uuid).cancel();
        }

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (isCancelled()) {
                    cancel();
                    return;
                }
                p.sendActionBar(warning);
            }
        }.runTaskTimerAsynchronously(Sandbox.instance, 0, 20);

        activeTasks.put(uuid, task);
    }



    public void stopActionBar(UUID uuid) {
        if (activeTasks.containsKey(uuid)) {
            activeTasks.get(uuid).cancel();
            activeTasks.remove(uuid);
        }
    }

    public void stopActionBarAndStartNew(UUID uuid, Component newWarning) {
        stopActionBar(uuid);
        startActionBar(Bukkit.getPlayer(uuid), newWarning);
    }

    public void stopActionBarAndSendFinalWarning(Player p, Component finalWarning) {
        UUID uuid = p.getUniqueId();
        if (activeTasks.containsKey(uuid)) {
            activeTasks.get(uuid).cancel();
            activeTasks.remove(uuid);
        }
        p.sendActionBar(finalWarning);
    }
}
