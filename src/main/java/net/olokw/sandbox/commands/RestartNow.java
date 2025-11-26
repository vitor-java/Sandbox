package net.olokw.sandbox.commands;

import net.olokw.sandbox.Sandbox;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class RestartNow implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.isOp()) {
            Sandbox.instance.getRestartUtils().realRestart();
        }
        return true;
    }
}
