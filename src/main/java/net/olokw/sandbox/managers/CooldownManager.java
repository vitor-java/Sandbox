package net.olokw.sandbox.managers;

import net.olokw.sandbox.Sandbox;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class CooldownManager {
    private final Map<UUID, Set<String>> cooldownManager;

    public CooldownManager() {
        this.cooldownManager = new HashMap<>();
    }

    public void add(UUID uuid, int id, long seconds){
        if (!cooldownManager.containsKey(uuid)) cooldownManager.put(uuid, new HashSet<>());
        cooldownManager.get(uuid).add(id + ":" + Sandbox.instance.getServer().getCurrentTick() + ":" + seconds);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (String s : cooldownManager.get(uuid)){
                    if (s.startsWith(id + ":")){
                        cooldownManager.get(uuid).remove(s);
                        break;
                    }
                }
            }
        }.runTaskLaterAsynchronously(Sandbox.instance, seconds * 20);
    }

    public boolean isInCooldown(UUID uuid, int id){
        if (cooldownManager.containsKey(uuid)){
            return cooldownManager.get(uuid).stream().anyMatch(s -> s.startsWith(id + ":"));
        } else {
            return false;
        }
    }

    public String getRemainingTimeFormated(UUID uuid, int id){
        String[] parts = new String[0];
        for (String s : cooldownManager.get(uuid)){
            if (s.startsWith(id + ":")){
                parts = s.split(":");
                break;
            }
        }
        long finalTime = Long.parseLong(parts[1]) + (20 * Long.parseLong(parts[2]));
        long timeRemaining = (finalTime - Sandbox.instance.getServer().getCurrentTick()) / 20;
        String t;
        if (timeRemaining/60 >= 1){
            t = (timeRemaining/60) + "min " + ((timeRemaining % 60) + 1) + "s";
        } else {
            t = ((timeRemaining % 60) + 1) + "s";
        }
        return t;
    }
}
