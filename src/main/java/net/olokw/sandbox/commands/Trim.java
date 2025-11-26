package net.olokw.sandbox.commands;

import net.olokw.sandbox.Sandbox;
import net.olokw.sandbox.utils.Message;
import net.olokw.sandbox.utils.TrimUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Trim implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player p) {
            if (!p.hasPermission("balacobaco.vip")) {
                p.sendMessage(Message.process("<red>⚠ › Apenas jogadores MVPs podem usar esse comando."));
            } else {
                TrimUtils trimUtils = Sandbox.instance.getTrimUtils();
                trimUtils.openGui(p, Sandbox.instance.getPlayerDataManager().get(p.getUniqueId()));
            }
        }
        return true;
    }
}
