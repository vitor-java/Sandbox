package net.olokw.sandbox.commands;

import net.olokw.sandbox.Sandbox;
import net.olokw.sandbox.utils.ArenaUtils;
import net.olokw.sandbox.utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Random;

public class Arena implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player p) {
            if (p.getWorld().getName().equalsIgnoreCase("spawn")) {
                ArenaUtils arenaUtils = Sandbox.instance.getArenaUtils();
                if (arenaUtils.isInsideArena(p.getLocation())) {
                    p.sendMessage(Message.process("<red>⚠ Você já está na arena!"));
                    return true;
                }
            }
            Location center = new Location(Bukkit.getWorld("spawn"),0.5, 81, 0.5);
            Random random = new Random();
            double angle = random.nextDouble() * Math.PI * 2;
            double x = center.getX() + (80 * Math.cos(angle));
            double z = center.getZ() + (80 * Math.sin(angle));

            Location loc = new Location(Bukkit.getWorld("spawn"), x, 81, z);
            loc.setPitch(p.getPitch());
            loc.setYaw(p.getYaw());
            p.teleport(loc);
            p.playSound(p, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1, 1);
            Sandbox.instance.getTeleportUtils().giveLastKit(p);
            p.clearActivePotionEffects();
            p.setHealth(20);
            p.setFoodLevel(20);
            p.setSaturation(5);
            p.setFireTicks(0);
        }
        return true;
    }
}
