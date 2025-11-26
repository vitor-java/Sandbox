package net.olokw.sandbox.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Spawn implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player p) {
            Location loc = Bukkit.getWorld("spawn").getSpawnLocation().toCenterLocation();
            loc.setY(121.0625);
            p.teleport(loc);
            p.clearActivePotionEffects();
            p.setHealth(20);
            p.setFoodLevel(20);
            p.setSaturation(5);
            p.setFireTicks(0);
            p.getInventory().clear();
            p.playSound(p, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
        }
        return true;
    }
}
