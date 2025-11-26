package net.olokw.sandbox.managers;

import net.olokw.sandbox.Sandbox;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CombatManager {
    private final Map<UUID, BukkitTask> combatManager;

    public CombatManager() {
        this.combatManager = new HashMap<>();
    }

    public void add(UUID uuid) {

        if (combatManager.containsKey(uuid)) {
            combatManager.get(uuid).cancel();
        }

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (isCancelled()) {
                    return;
                }
                combatManager.remove(uuid);
                cancel();
            }
        }.runTaskLaterAsynchronously(Sandbox.instance, 20 * 10);

        combatManager.put(uuid, task);
    }

    public boolean isInCombat(UUID uuid){
        return combatManager.containsKey(uuid);
    }

    public void remove(UUID uuid){
        if (combatManager.containsKey(uuid)) {
            combatManager.get(uuid).cancel();
        }
        combatManager.remove(uuid);
    }
}
