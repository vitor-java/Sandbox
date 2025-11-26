package net.olokw.sandbox.commands;

import net.olokw.sandbox.Sandbox;
import net.olokw.sandbox.configs.PlayerData;
import net.olokw.sandbox.utils.Message;
import net.olokw.sandbox.utils.TeleportUtils;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Back implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player p) {
            PlayerData data = Sandbox.instance.getPlayerDataManager().get(p.getUniqueId());
            Location loc = data.getBackLocation();
            if (loc == null) {
                p.sendMessage(Message.process("<red>⚠ › Não há nenhuma localização de morte!"));
            } else {
                TeleportUtils teleportUtils = Sandbox.instance.getTeleportUtils();
                p.teleportAsync(loc);
                teleportUtils.giveLastKit(p);
            }
        }
        return true;
    }
}
